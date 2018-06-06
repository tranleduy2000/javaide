package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.annotations.concurrency.GuardedBy;
import com.android.utils.FileUtils;
import com.google.common.collect.Maps;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.RelativePath;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.android.SdkConstants.FD_JARS;

/**
 * Cache to library prepareTask.
 * <p>
 * Each project creates its own version of LibraryDependencyImpl, but they all represent the
 * same library. This creates a single task that will unarchive the aar so that this is done only
 * once even for multi-module projects where 2+ modules depend on the same library.
 * <p>
 * The prepareTask is created in the root project always.
 */
public class LibraryCache {
    @NonNull
    private static final LibraryCache sCache = new LibraryCache();
    @GuardedBy("this")
    private final Map<String, CountDownLatch> bundleLatches = Maps.newHashMap();

    @NonNull
    public static LibraryCache getCache() {
        return sCache;
    }

    public static void unzipAar(final File bundle, final File folderOut, final Project project) throws IOException {
        FileUtils.deleteFolder(folderOut);
        folderOut.mkdirs();
        project.copy(new Action<CopySpec>() {
            @Override
            public void execute(CopySpec copySpec) {
                copySpec.from(project.zipTree(bundle));
                copySpec.into(new Object[]{folderOut});
                copySpec.filesMatching("**/*.jar", new Action<FileCopyDetails>() {
                    @Override
                    public void execute(FileCopyDetails details) {
                        setRelativePath(details, new RelativePath(false, FD_JARS).plus(details.getRelativePath()));
                    }
                });
            }
        });
    }

    private static <Value extends RelativePath> Value setRelativePath(FileCopyDetails propOwner, Value var1) {
        propOwner.setRelativePath(var1);
        return var1;
    }

    public synchronized void unload() {
        bundleLatches.clear();
    }

    public void unzipLibrary(@NonNull String taskName, @NonNull Project project, @NonNull final File bundle, @NonNull final File folderOut) throws IOException, InterruptedException {

        // only synchronize access to the latch so that unzipping 2+ different
        // libraries in parallel will work.
        boolean newItem = false;
        CountDownLatch latch;
        synchronized (this) {
            String path = bundle.getCanonicalPath();
            latch = bundleLatches.get(path);
            if (latch == null) {
                latch = new CountDownLatch(1);
                bundleLatches.put(path, latch);
                newItem = true;
            }

        }


        if (newItem) {
            try {
                project.getLogger().debug(taskName + ": ERASE " + folderOut.getPath());

                unzipAar(bundle, folderOut, project);

                project.getLogger().debug(taskName + ": UNZIP " + bundle.getPath() + " -> " + folderOut.getPath());
            } finally {
                latch.countDown();
            }

        } else {
            latch.await();
        }

    }
}
