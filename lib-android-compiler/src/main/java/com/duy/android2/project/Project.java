package com.duy.android2.project;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.android2.Action;
import com.duy.android2.logging.Logger;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface Project {
    /**
     * The default project build file name.
     */
    public static final String DEFAULT_BUILD_FILE = "build.gradle";

    /**
     * The hierarchy separator for project and task path names.
     */
    public static final String PATH_SEPARATOR = ":";

    /**
     * The default build directory name.
     */
    public static final String DEFAULT_BUILD_DIR_NAME = "build";

    public static final String GRADLE_PROPERTIES = "gradle.properties";

    public static final String SYSTEM_PROP_PREFIX = "systemProp";

    public static final String DEFAULT_VERSION = "unspecified";

    public static final String DEFAULT_STATUS = "release";

    /**
     * <p>Returns the root project for the hierarchy that this project belongs to.  In the case of a single-project
     * build, this method returns this project.</p>
     *
     * @return The root project. Never returns null.
     */
    Project getRootProject();

    /**
     * <p>Returns the root directory of this project. The root directory is the project directory of the root
     * project.</p>
     *
     * @return The root directory. Never returns null.
     */
    File getRootDir();


    /**
     * <p>Returns the build directory of this project.  The build directory is the directory which all artifacts are
     * generated into.  The default value for the build directory is <code><i>projectDir</i>/build</code></p>
     *
     * @return The build directory. Never returns null.
     */
    @NonNull
    File getBuildDir();


    /**
     * <p>Sets the build directory of this project. The build directory is the directory which all artifacts are
     * generated into. The path parameter is evaluated as described for {@link #file(Object)}. This mean you can use,
     * amongst other things, a relative or absolute path or File object to specify the build directory.</p>
     *
     * @param path The build directory. This is evaluated as per {@link #file(Object)}
     */
    void setBuildDir(Object path);

    /**
     * <p>Returns the build file Gradle will evaluate against this project object. The default is <code> {@value
     * #DEFAULT_BUILD_FILE}</code>. If an embedded script is provided the build file will be null. </p>
     *
     * @return Current build file. May return null.
     */
    @Nullable
    File getBuildFile();

    /**
     * <p>Returns the parent project of this project, if any.</p>
     *
     * @return The parent project, or null if this is the root project.
     */
    @Nullable
    Project getParent();

    /**
     * <p>Returns the name of this project. The project's name is not necessarily unique within a project hierarchy. You
     * should use the {@link #getPath()} method for a unique identifier for the project.</p>
     *
     * @return The name of this project. Never return null.
     */
    @NonNull
    String getName();

    /**
     * Returns the description of this project.
     *
     * @return the description. May return null.
     */
    @Nullable
    String getDescription();

    /**
     * Sets a description for this project.
     *
     * @param description The description of the project. Might be null.
     */
    void setDescription(String description);

    /**
     * <p>Returns the direct children of this project.</p>
     *
     * @return A map from child project name to child project. Returns an empty map if this project does not have
     * any children.
     */
    Map<String, Project> getChildProjects();

    /**
     * <p>Returns this project. This method is useful in build files to explicitly access project properties and
     * methods. For example, using <code>project.name</code> can express your intent better than using
     * <code>name</code>. This method also allows you to access project properties from a scope where the property may
     * be hidden, such as, for example, from a method or closure. </p>
     *
     * @return This project. Never returns null.
     */
    Project getProject();


    /**
     * <p>Returns the set containing this project and its subprojects.</p>
     *
     * @return The set of projects.
     */
    Set<Project> getAllprojects();

    /**
     * <p>Returns the set containing the subprojects of this project.</p>
     *
     * @return The set of projects.  Returns an empty set if this project has no subprojects.
     */
    Set<Project> getSubprojects();

    /**
     * <p>Configures the sub-projects of this project</p>
     * <p>
     * <p>This method executes the given {@link Action} against the sub-projects of this project.</p>
     *
     * @param action The action to execute.
     */
    void subprojects(Action<? super Project> action);

    /**
     * <p>Returns the logger for this project. You can use this in your build file to write log messages.</p>
     *
     * @return The logger. Never returns null.
     */
    Logger getLogger();
}
