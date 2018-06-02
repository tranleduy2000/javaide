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

package com.android.jill.api;

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides instances of {@link JillConfig}.
 */
public interface JillProvider {
  /**
   * Creates a {@link JillConfig} instance for an interface representing a {@link JillConfig} API
   * version.
   * @param cls the {@link JillConfig} API interface
   * @return the {@link JillConfig} instance
   * @throws ConfigNotSupportedException If no implementation is found for the given interface.
   */
  @Nonnull
  <T extends JillConfig> T createConfig(@Nonnull Class<T> cls) throws ConfigNotSupportedException;

  /**
   * Returns whether an interface representing a {@link JillConfig} API version is supported.
   *
   * @param cls the {@link JillConfig} API interface
   * @return <code>true</true> if the config is supported
   */
  @Nonnull
  <T extends JillConfig> boolean isConfigSupported(@Nonnull Class<T> cls);

  /**
   * Gives a {@link Collection} containing supported {@link JillConfig} API versions.
   * @return the supported {@link JillConfig} API versions
   */
  @Nonnull
  Collection<Class<? extends JillConfig>> getSupportedConfigs();

  /**
   * Gives the version of this Jill, summarized in one string (e.g. "1.1-rc1", "2.0-a2",
   * ...).
   *
   * @return the version
   */
  @Nonnull
  String getTranslatorVersion();

  /**
   * Gives the release name of this Jill (e.g. Arzon, Brest, ...).
   *
   * @return the release name
   */
  @Nonnull
  String getTranslatorReleaseName();

  /**
   * Gives an integer value that represents the release of this Jill, relative to other
   * releases.
   *
   * @return the release code
   */
  @Nonnegative
  int getTranslatorReleaseCode();

  /**
   * Gives an integer value that represents the sub-release of this Jill, relative to other
   * sub-releases of the same release.
   *
   * @return the sub-release code
   */
  @Nonnegative
  int getTranslatorSubReleaseCode();

  /**
   * Gives the kind of sub-release of this Jill.
   *
   * @return the sub-release kind
   */
  @Nonnull
  SubReleaseKind getTranslatorSubReleaseKind();

  /**
   * The kind of sub-release.
   */
  public enum SubReleaseKind {
    /**
     * A sub-release from an engineering development, not tested, not in the code base repository.
     */
    ENGINEERING,
    /**
     * A sub-release that is not feature complete, not tested.
     */
    PRE_ALPHA,
    /**
     * A sub-release that is not feature complete, tested.
     */
    ALPHA,
    /**
     * A sub-release that is feature complete, tested, but likely contains known or unknown bugs.
     */
    BETA,
    /**
     * A pre-production sub-release, tested.
     */
    CANDIDATE,
    /**
     * A production and stable sub-release.
     */
    RELEASE;
  }

  /**
   * The build ID of this Jill.
   * @return the build ID, or null if not available
   */
  @CheckForNull
  String getTranslatorBuildId();

  /**
   * Identify the source code base of this Jill.
   * @return the source code base, or null if not available
   */
  @CheckForNull
  String getTranslatorSourceCodeBase();
}
