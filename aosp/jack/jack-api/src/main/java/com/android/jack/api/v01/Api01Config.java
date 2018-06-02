/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.api.v01;

import com.android.jack.api.JackConfig;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A configuration for API level 01 of the Jack compiler.
 */
public interface Api01Config extends JackConfig {

  /**
   * Sets an {@link OutputStream} where Jack will write errors, warnings and other information.
   * @param reporterKind The type of reporter
   * @param reporterStream The stream where to write
   * @throws ConfigurationException
   */
  void setReporter(@Nonnull ReporterKind reporterKind, @Nonnull OutputStream reporterStream)
      throws ConfigurationException;

  /**
   * Sets the policy to follow when there is a collision between imported types.
   * @param typeImportCollisionPolicy the collision policy for imported types
   * @throws ConfigurationException
   */
  void setTypeImportCollisionPolicy(@Nonnull TypeCollisionPolicy typeImportCollisionPolicy)
      throws ConfigurationException;

  /**
   * Sets the policy to follow when there is a collision between imported resources.
   * @param resourceImportCollisionPolicy the collision policy for imported resources
   * @throws ConfigurationException
   */
  void setResourceImportCollisionPolicy(
      @Nonnull ResourceCollisionPolicy resourceImportCollisionPolicy) throws ConfigurationException;

  /**
   * Sets the Java source version.
   * @param javaSourceVersion the Java source version
   * @throws ConfigurationException
   */
  void setJavaSourceVersion(@Nonnull JavaSourceVersion javaSourceVersion)
      throws ConfigurationException;

  /**
   * Sets the file where to write the obfuscation mapping. The file may already exist and will be
   * overwritten.
   * @param obfuscationMappingOutputFile the obfuscation mapping output file
   * @throws ConfigurationException
   */
  void setObfuscationMappingOutputFile(@Nonnull File obfuscationMappingOutputFile)
      throws ConfigurationException;

  /**
   * Sets the classpath.
   * @param classpath The classpath as a list
   * @throws ConfigurationException
   */
  void setClasspath(@Nonnull List<File> classpath) throws ConfigurationException;

  /**
   * Sets the Jack library files that will be imported into the output.
   * @param importedJackLibraryFiles The Jack library files to import
   * @throws ConfigurationException
   */
  void setImportedJackLibraryFiles(@Nonnull List<File> importedJackLibraryFiles)
      throws ConfigurationException;

  /**
   * Sets the directories containing files to import into the output as meta-files.
   * @param metaDirs The directories containing the meta-files
   * @throws ConfigurationException
   */
  void setMetaDirs(@Nonnull List<File> metaDirs) throws ConfigurationException;

  /**
   * Sets the directories containing files to import into the output as resources.
   * @param resourceDirs The directories containing the resources
   * @throws ConfigurationException
   */
  void setResourceDirs(@Nonnull List<File> resourceDirs) throws ConfigurationException;

  /**
   * Sets the directory that will be used to store data for incremental support. This directory must
   * already exist.
   * @param incrementalDir The directory used for incremental data
   * @throws ConfigurationException
   */
  void setIncrementalDir(@Nonnull File incrementalDir) throws ConfigurationException;

  /**
   * Sets the directory that will be used to write dex files and resources. This directory must
   * already exist.
   * @param outputDexDir The output directory for dex files and resources
   * @throws ConfigurationException
   */
  void setOutputDexDir(@Nonnull File outputDexDir) throws ConfigurationException;

  /**
   * Sets the file where the output Jack library will be written. The file may already exist and
   * will be overwritten.
   * @param outputJackFile The output Jack library file
   * @throws ConfigurationException
   */
  void setOutputJackFile(@Nonnull File outputJackFile) throws ConfigurationException;

  /**
   * Sets JarJar configuration files to use for repackaging.
   * @param jarjarConfigFiles The JarJar configuration files
   * @throws ConfigurationException
   */
  void setJarJarConfigFiles(@Nonnull List<File> jarjarConfigFiles) throws ConfigurationException;

  /**
   * Sets ProGuard configuration files.
   * @param proguardConfigFiles The ProGuard configuration files
   * @throws ConfigurationException
   */
  void setProguardConfigFiles(@Nonnull List<File> proguardConfigFiles)
      throws ConfigurationException;

  /**
   * Set how much debug info should be emitted.
   * @param debugInfoLevel The level of debug info to emit
   * @throws ConfigurationException
   */
  void setDebugInfoLevel(@Nonnull DebugInfoLevel debugInfoLevel) throws ConfigurationException;

  /**
   * Sets whether to allow splitting the output in several dex files, and which method to use.
   * @param multiDexKind the multidex kind
   * @throws ConfigurationException
   */
  void setMultiDexKind(@Nonnull MultiDexKind multiDexKind) throws ConfigurationException;

  /**
   * Sets the verbosity level.
   * @param verbosityLevel the verbosity level
   * @throws ConfigurationException
   */
  void setVerbosityLevel(@Nonnull VerbosityLevel verbosityLevel) throws ConfigurationException;

  /**
   * Sets the class names of the annotation processors to run.
   * @param processorNames Annotation processor class names
   * @throws ConfigurationException
   */
  void setProcessorNames(@Nonnull List<String> processorNames) throws ConfigurationException;

  /**
   * Sets the path where to find annotation processors.
   * @param processorPath The processor path as a list
   * @throws ConfigurationException
   */
  void setProcessorPath(@Nonnull List<File> processorPath) throws ConfigurationException;

  /**
   * Sets options for the annotation processors.
   * @param processorOptions The processor options as a map
   * @throws ConfigurationException
   */
  void setProcessorOptions(@Nonnull Map<String, String> processorOptions)
      throws ConfigurationException;

  /**
   * Sets the Java source files entries to compile.
   * @param sourceEntries The source entries
   * @throws ConfigurationException
   */
  void setSourceEntries(@Nonnull Collection<File> sourceEntries) throws ConfigurationException;

  /**
   * Sets the value for the given property.
   * @param key The name of the property
   * @param value The value to set the property to
   * @throws ConfigurationException
   */
  void setProperty(@Nonnull String key, @Nonnull String value) throws ConfigurationException;

  /**
   * Creates an instance of the {@link Api01CompilationTask} according to this configuration.
   * @return The {@link Api01CompilationTask}
   * @throws ConfigurationException
   */
  @Nonnull
  Api01CompilationTask getTask() throws ConfigurationException;
}
