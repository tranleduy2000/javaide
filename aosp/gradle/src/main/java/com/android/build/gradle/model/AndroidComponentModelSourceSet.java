/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.build.gradle.model;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.AbstractNamedDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.language.base.FunctionalSourceSet;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.ProjectSourceSet;
import org.gradle.language.base.internal.DefaultFunctionalSourceSet;
import org.gradle.language.base.internal.registry.LanguageRegistration;

/**
 * Collection of source sets for each build type, product flavor or variant.
 *
 * Until Gradle provide a way to create and store source sets to use between multiple binaries, we
 * need to create a container for such source sets.
 */
// TODO: Remove dependencies on internal Gradle class.
public class AndroidComponentModelSourceSet
        extends AbstractNamedDomainObjectContainer<FunctionalSourceSet>
        implements NamedDomainObjectContainer<FunctionalSourceSet> {
    ProjectSourceSet projectSourceSet;

    public AndroidComponentModelSourceSet (Instantiator instantiator) {
        super(FunctionalSourceSet.class, instantiator);
    }

    public <T extends LanguageSourceSet> void registerLanguage(final LanguageRegistration<T> languageRegistration) {
        // Hardcoding registered language sets and default source sets for now.
        all(new Action<FunctionalSourceSet>() {
            @Override
            public void execute(final FunctionalSourceSet functionalSourceSet) {
                functionalSourceSet.registerFactory(
                        languageRegistration.getSourceSetType(),
                        languageRegistration.getSourceSetFactory(functionalSourceSet.getName()));
            }
        });
    }

    /**
     * Setter for projectSourceSet.
     * Having a setter avoid the need for ProjectSourceSet to be part of the constructor, which can cause 
     * cyclic rule dependenecy issues.
     */
    public void setProjectSourceSet(ProjectSourceSet projectSourceSet) {
        this.projectSourceSet = projectSourceSet;
    }

    @Override
    protected FunctionalSourceSet doCreate(String name) {
        return getInstantiator().newInstance(
                DefaultFunctionalSourceSet.class,
                name,
                getInstantiator(),
                projectSourceSet);
    }

    public void addDefaultSourceSet(final String sourceSetName, final Class<? extends LanguageSourceSet> type) {
        all(new Action<FunctionalSourceSet>() {
            @Override
            public void execute(FunctionalSourceSet functionalSourceSet) {
                functionalSourceSet.maybeCreate(sourceSetName, type);
            }
        });
    }

    /**
     * Set the default directory for each source sets if it is empty.
     */
    public void setDefaultSrcDir() {
        all(new Action<FunctionalSourceSet>() {
            @Override
            public void execute(final FunctionalSourceSet functionalSourceSet) {
                functionalSourceSet.all(
                        new Action<LanguageSourceSet>() {
                            @Override
                            public void execute(LanguageSourceSet languageSourceSet) {
                                SourceDirectorySet source = languageSourceSet.getSource();
                                if (source.getSrcDirs().isEmpty()) {
                                    source.srcDir("src/" + functionalSourceSet.getName() + "/" + languageSourceSet.getName());
                                }
                            }
                        });
            }
        });
    }
}