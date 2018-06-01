/**
 * Copyright [2009] Marc-Andre Houle
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.duy.android.compiler.repo.maven;

import android.util.Log;

import com.duy.android.compiler.env.Environment;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.DefaultArchiverManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This mojo is designed to download a maven artifact from the repository and
 * download them in the specified path. The maven artifact downloaded can also
 * download it's dependency or not, based on a parameter.
 *
 * @author Marc-Andre Houle
 */
public class ArtifactDownloader {
    private static final String TAG = "Artifact";
    private final Set<Artifact> artifactToCopy = new HashSet<>();

    private final String artifactId;
    private final String groupId;
    private final String version;
    private String type;
    private String classifier;

    /**
     * Location of the file.
     */
    private File outputDirectory;
    /**
     * Will set the output file name to the specified name.  Valid only when the dependency depth
     * is set to 0.
     */
    private String outputFileName;
    /**
     * Whether to unpack the artifact
     */
    private boolean unpack;

    private List<String> remoteRepositories;
    private ArtifactResolver resolver;
    private ArtifactMetadataSource metadatSource;
    private MavenProjectBuilder mavenProjectBuilder;
    private ArchiverManager archiverManager;
    private ArtifactRepository localRepository;
    private ArtifactFactory artifactFactory;

    public ArtifactDownloader(android.content.Context context, MavenProject mavenProject) {
        this.artifactId = mavenProject.getArtifactId();
        System.out.println("mavenProject = " + mavenProject);
        this.groupId = mavenProject.getGroupId();
        System.out.println("mavenProject = " + mavenProject);
        this.version = mavenProject.getVersion();
        System.out.println("mavenProject = " + mavenProject);
        this.type = "pom";
        this.classifier = "";

        this.resolver = new LocalArtifactResolver();
        this.artifactFactory = new LocalArtifactFactory();
        this.archiverManager = new DefaultArchiverManager();
        this.mavenProjectBuilder = new DefaultMavenProjectBuilder();
        this.metadatSource = new MavenMetadataSource();

        this.localRepository = new DefaultArtifactRepository(
                context.getPackageName(),
                Environment.getLocalRepositoryDir(context).getAbsolutePath(),
                new DefaultRepositoryLayout());

        remoteRepositories = new ArrayList<>();
        remoteRepositories.add("https://dl.bintray.com/ppartisan/maven/");
        remoteRepositories.add("https://clojars.org/repo/");
        remoteRepositories.add("https://jitpack.io");
        remoteRepositories.add("https://maven.google.com");
    }

    /**
     * Will download the specified artifact in the specified directory.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
        downloadAndAddArtifact(artifact);
        for (Artifact copy : this.artifactToCopy) {
            if (this.unpack) {
                this.unpackFileToDirectory(copy);
            } else {
                this.copyFileToDirectory(copy);
            }
        }
    }

    /**
     * Download the artifact when possible and copy it to the target directory
     * and will fetch the dependency until the specified depth is reached.
     *
     * @param artifact The artifact to download and set.
     */
    private void downloadAndAddArtifact(Artifact artifact) throws MojoFailureException {
        this.downloadArtifact(artifact);
        this.artifactToCopy.add(artifact);

        Set<Artifact> dependencies = getTransitiveDependency(artifact);
        debug("Nummber dependencies : " + dependencies.size());
        for (Artifact dependency : dependencies) {
            downloadAndAddArtifact(dependency);
        }
    }

    /**
     * Will check if the artifact is in the local repository and download it if
     * it is not.
     *
     * @param artifact The artifact to check if it is present in the local directory.
     * @throws MojoFailureException If an error happen while resolving the artifact.
     */
    private void downloadArtifact(Artifact artifact) throws MojoFailureException {
        try {
            resolver.resolve(artifact, remoteRepositories, localRepository);
        } catch (ArtifactResolutionException e) {
            debug("Artifact could not be resolved.", e);
            throw new MojoFailureException("Artifact could not be resolved.");
        } catch (ArtifactNotFoundException e) {
            debug("Artifact could not be found.", e);
            throw new MojoFailureException("Artifact could not be found.");
        }
    }

    /**
     * Will copy the specified artifact into the output directory.
     *
     * @param artifact The artifact already resolved to be copied.
     * @throws MojoFailureException If an error hapen while copying the file.
     */
    private void copyFileToDirectory(Artifact artifact) throws MojoFailureException {
        File toCopy = artifact.getFile();
        if (toCopy != null && toCopy.exists() && toCopy.isFile()) {
            try {
                info("Copying file " + toCopy.getName() + " to directory " + outputDirectory);
                File outputFile = null;
                if (this.outputFileName == null) {
                    outputFile = new File(outputDirectory, toCopy.getName());
                } else {
                    outputFile = new File(outputDirectory, this.outputFileName);
                }
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                FileOutputStream output = new FileOutputStream(outputFile);
                FileInputStream input = new FileInputStream(toCopy);
                org.apache.commons.io.IOUtils.copy(input, output);
                output.close();
                input.close();
            } catch (IOException e) {
                debug("Error while copying file", e);
                throw new MojoFailureException("Error copying the file : " + e.getMessage());
            }
        } else {
            throw new MojoFailureException("Artifact file not present : " + toCopy);
        }
    }


    private void unpackFileToDirectory(Artifact artifact) throws MojoExecutionException {
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        File toUnpack = artifact.getFile();
        if (toUnpack != null && toUnpack.exists() && toUnpack.isFile()) {
            try {
                UnArchiver unarchiver = this.archiverManager.getUnArchiver(toUnpack);
                unarchiver.setSourceFile(toUnpack);
                unarchiver.setDestDirectory(this.outputDirectory);
                unarchiver.extract();
            } catch (Exception ex) {
                throw new MojoExecutionException("Issue while unarchiving", ex);
            }
        }
    }

    /**
     * Will fetch a list of all the transitive dependencies for an artifact and
     * return a set of those artifacts.
     *
     * @param artifact The artifact for which transitive dependencies need to be
     *                 downloaded.
     * @return The set of dependencies that was dependant.
     * @throws MojoFailureException If anything goes wrong when getting transitive dependency.
     *                              Note : Suppress warning used for the uncheck cast of artifact
     *                              set.
     */
    @SuppressWarnings("unchecked")
    private Set<Artifact> getTransitiveDependency(Artifact artifact) throws MojoFailureException {
        try {
            Artifact pomArtifact = artifactFactory.createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact
                    .getVersion(), artifact.getClassifier(), "pom");
            MavenProject pomProject = mavenProjectBuilder.buildFromRepository(pomArtifact, this.remoteRepositories, this.localRepository);
            Set<Artifact> dependents = pomProject.createArtifacts(this.artifactFactory, null, null);
            ArtifactResolutionResult result = resolver.resolveTransitively(dependents, pomArtifact, this.localRepository, this.remoteRepositories,
                    this.metadatSource, null);
            if (result != null) {
                debug("Found transitive dependency : " + result);
                return result.getArtifacts();
            }
        } catch (ArtifactResolutionException e) {
            debug("Could not resolved the dependency", e);
            throw new MojoFailureException("Could not resolved the dependency : " + e.getMessage());
        } catch (ArtifactNotFoundException e) {
            debug("Could not find the dependency", e);
            throw new MojoFailureException("Could not find the dependency : " + e.getMessage());
        } catch (ProjectBuildingException e) {
            debug("Error Creating the pom project for artifact : " + artifact, e);
            throw new MojoFailureException("Error getting transitive dependencies : " + e.getMessage());
        } catch (InvalidDependencyVersionException e) {
            debug("Error Creating the pom project for artifact : " + artifact, e);
            throw new MojoFailureException("Error getting transitive dependencies : " + e.getMessage());
        }
        return null;
    }

    private void debug(String s, Exception e) {
        Log.d(TAG, s, e);
    }

    private void debug(String message) {
        Log.d(TAG, message);
    }

    private void info(String message) {
        Log.i(TAG, message);
    }
}
