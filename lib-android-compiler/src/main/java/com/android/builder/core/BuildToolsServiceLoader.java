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

package com.android.builder.core;

import com.android.annotations.NonNull;
import com.android.jack.api.JackProvider;
import com.android.jill.api.JillProvider;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * {@link ServiceLoader} helpers for tools located in the SDK's build-tools folders.
 *
 * This utility will cache {@link ServiceLoader} instances per build-tools version and per target
 * service type.
 */
public enum BuildToolsServiceLoader {

    /**
     * Singleton instance to request {@link ServiceLoader} instances from.
     */
    INSTANCE;

    /**
     * private cache data for a particular build-tool version.
     */
    private static final class LoadedBuildTool {
        private final FullRevision version;
        private final BuildToolServiceLoader serviceLoader;

        private LoadedBuildTool(FullRevision version,
                BuildToolServiceLoader serviceLoader) {
            this.version = version;
            this.serviceLoader = serviceLoader;
        }
    }

    private final List<LoadedBuildTool> loadedBuildTools = new ArrayList<LoadedBuildTool>();

    /**
     * Load a built-tools version specific {@link ServiceLoader} helper.
     * @param buildToolInfo the requested build-tools information
     * @return an initialized {@link BuildToolsServiceLoader.BuildToolServiceLoader} to get
     * instances of {@link ServiceLoader} from.
     */
    @NonNull
    public synchronized BuildToolServiceLoader forVersion(BuildToolInfo buildToolInfo) {

        Optional<LoadedBuildTool> loadedBuildToolOptional =
                findVersion(buildToolInfo.getRevision());

        if (loadedBuildToolOptional.isPresent()) {
            return loadedBuildToolOptional.get().serviceLoader;
        }

        LoadedBuildTool loadedBuildTool = new LoadedBuildTool(buildToolInfo.getRevision(),
                    new BuildToolServiceLoader(buildToolInfo));
        loadedBuildTools.add(loadedBuildTool);
        return loadedBuildTool.serviceLoader;
    }

    @NonNull
    private Optional<LoadedBuildTool> findVersion(FullRevision version) {
        for (LoadedBuildTool loadedBuildTool : loadedBuildTools) {
            if (loadedBuildTool.version.equals(version)) {
                return Optional.of(loadedBuildTool);
            }
        }
        return Optional.absent();
    }

    /**
     * Abstract notion of what a service is. A service must be declared in one of the classpath
     * provided jar files. The service declaration must conforms to {@link ServiceLoader} contract.
     *
     * @param <T> the type of service.
     */
    public static class Service<T> {

        private final Collection<String> classpath;
        private final Class<T> serviceClass;

        protected Service(Collection<String> classpath, Class<T> serviceClass) {
            this.classpath = classpath;
            this.serviceClass = serviceClass;
        }

        public Collection<String> getClasspath() {
            return classpath;
        }
        public Class<T> getServiceClass() {
            return serviceClass;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("serviceClass", serviceClass)
                    .add("classpath", Joiner.on(",").join(classpath))
                    .toString();
        }
    }

    /**
     * Jack service description.
     */
    public static final Service<JackProvider> JACK =
            new Service<JackProvider>(ImmutableList.of("jack.jar"), JackProvider.class);

    /**
     * Jill service description.
     */
    public static final Service<JillProvider> JILL =
            new Service<JillProvider>(ImmutableList.of("jill.jar"), JillProvider.class);

    /**
     * build-tools version specific {@link ServiceLoader} helper.
     */
    public static final class BuildToolServiceLoader {

        /**
         * private cache data for a single {@link ServiceLoader} instance.
         * @param <T> the service loader type.
         */
        private static final class LoadedServiceLoader<T> {
            private final Class<T> serviceType;
            private final ServiceLoader<T> serviceLoader;

            private LoadedServiceLoader(Class<T> serviceType, ServiceLoader<T> serviceLoader) {
                this.serviceType = serviceType;
                this.serviceLoader = serviceLoader;
            }
        }

        private final BuildToolInfo buildToolInfo;
        private final List<LoadedServiceLoader> loadedServicesLoaders =
                new ArrayList<LoadedServiceLoader>();

        private BuildToolServiceLoader(BuildToolInfo buildToolInfo) {
            this.buildToolInfo = buildToolInfo;
        }

        /**
         * Returns a newly allocated or existing {@link ServiceLoader} instance for the passed
         * {@link com.android.builder.core.BuildToolsServiceLoader.Service} type in the context
         * of the build-tools version this instance was created for.
         *
         * @param serviceType the requested service type encapsulation.
         * @param <T> the type of service
         * @return a {@link ServiceLoader} instance for the T service type.
         * @throws ClassNotFoundException
         */
        @NonNull
        public synchronized  <T> ServiceLoader<T> getServiceLoader(Service<T> serviceType)
                throws ClassNotFoundException {

            Optional<ServiceLoader<T>> serviceLoaderOptional =
                    getLoadedServiceLoader(serviceType.getServiceClass());
            if (serviceLoaderOptional.isPresent()) {
                return serviceLoaderOptional.get();
            }

            File buildToolLocation = buildToolInfo.getLocation();
            if (System.getenv("USE_JACK_LOCATION") != null) {
                buildToolLocation = new File(System.getenv("USE_JACK_LOCATION"));
            }
            URL[] urls = new URL[serviceType.classpath.size()];
            int i = 0;
            for (String classpathItem : serviceType.getClasspath()) {
                File jarFile = new File(buildToolLocation, classpathItem);
                try {
                    urls[i++] = jarFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            ClassLoader cl = new URLClassLoader(urls, serviceType.getServiceClass().getClassLoader());
            ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType.getServiceClass(), cl);
            loadedServicesLoaders.add(new LoadedServiceLoader<T>(
                    serviceType.getServiceClass(), serviceLoader));
            return serviceLoader;
        }

        /**
         * Return the first service instance for the requested service type or
         * {@link Optional#absent()} if none exist.
         * @param logger to log resolution.
         * @param serviceType the requested service type encapsulation.
         * @param <T> the requested service class type.
         * @return the instance of T or null of none exist in this context.
         * @throws ClassNotFoundException
         */
        @NonNull
        public synchronized <T> Optional<T> getSingleService(
                ILogger logger,
                Service<T> serviceType) throws ClassNotFoundException {
            logger.verbose("Looking for %1$s", serviceType);
            ServiceLoader<T> serviceLoader = getServiceLoader(serviceType);
            logger.verbose("Got a serviceLoader %1$d",
                    Integer.toHexString(System.identityHashCode(serviceLoader)));
            Iterator<T> serviceIterator = serviceLoader.iterator();
            logger.verbose("Service Iterator =  %1$s ", serviceIterator);
            if (serviceIterator.hasNext()) {
                T service = serviceIterator.next();
                logger.verbose("Got it from %1$s, loaded service = %2$s, type = %3$s",
                        serviceIterator, service, service.getClass());
                return Optional.of(service);
            } else {
                logger.info("Cannot find service implementation %1$s" + serviceType);
                return Optional.absent();
            }
        }

        @NonNull
        @SuppressWarnings("unchecked")
        private <T> Optional<ServiceLoader<T>> getLoadedServiceLoader(Class<T> serviceType) {
            for (LoadedServiceLoader<?> loadedServiceLoader : loadedServicesLoaders) {
                if (loadedServiceLoader.serviceType.equals(serviceType)) {
                    return Optional.of((ServiceLoader<T>) loadedServiceLoader.serviceLoader);
                }
            }
            return Optional.absent();
        }
    }
}
