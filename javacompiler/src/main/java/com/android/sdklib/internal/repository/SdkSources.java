/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.sdklib.internal.repository;

import com.android.prefs.AndroidLocation;
import com.android.prefs.AndroidLocation.AndroidLocationException;
import com.android.sdklib.ISdkLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * A list of sdk-repository and sdk-addon sources, sorted by {@link SdkSourceCategory}.
 */
public class SdkSources {

    private static final String KEY_COUNT = "count";

    private static final String KEY_SRC = "src";

    private static final String SRC_FILENAME = "repositories.cfg"; //$NON-NLS-1$

    private final EnumMap<SdkSourceCategory, ArrayList<SdkSource>> mSources =
        new EnumMap<SdkSourceCategory, ArrayList<SdkSource>>(SdkSourceCategory.class);

    public SdkSources() {
    }

    /**
     * Adds a new source to the Sources list.
     */
    public void add(SdkSourceCategory category, SdkSource source) {
        synchronized (mSources) {
            ArrayList<SdkSource> list = mSources.get(category);
            if (list == null) {
                list = new ArrayList<SdkSource>();
                mSources.put(category, list);
            }

            list.add(source);
        }
    }

    /**
     * Removes a source from the Sources list.
     */
    public void remove(SdkSource source) {
        synchronized (mSources) {
            Iterator<Entry<SdkSourceCategory, ArrayList<SdkSource>>> it =
                mSources.entrySet().iterator();
            while (it.hasNext()) {
                Entry<SdkSourceCategory, ArrayList<SdkSource>> entry = it.next();
                ArrayList<SdkSource> list = entry.getValue();

                if (list.remove(source)) {
                    if (list.isEmpty()) {
                        // remove the entry since the source list became empty
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * Removes all the sources in the given category.
     */
    public void removeAll(SdkSourceCategory category) {
        synchronized (mSources) {
            mSources.remove(category);
        }
    }

    /**
     * Returns a set of all categories that must be displayed. This includes all
     * categories that are to be always displayed as well as all categories which
     * have at least one source.
     * Might return a empty array, but never returns null.
     */
    public SdkSourceCategory[] getCategories() {
        ArrayList<SdkSourceCategory> cats = new ArrayList<SdkSourceCategory>();

        for (SdkSourceCategory cat : SdkSourceCategory.values()) {
            if (cat.getAlwaysDisplay()) {
                cats.add(cat);
            } else {
                synchronized (mSources) {
                    ArrayList<SdkSource> list = mSources.get(cat);
                    if (list != null && !list.isEmpty()) {
                        cats.add(cat);
                    }
                }
            }
        }

        return cats.toArray(new SdkSourceCategory[cats.size()]);
    }

    /**
     * Returns a new array of sources attached to the given category.
     * Might return an empty array, but never returns null.
     */
    public SdkSource[] getSources(SdkSourceCategory category) {
        synchronized (mSources) {
            ArrayList<SdkSource> list = mSources.get(category);
            if (list == null) {
                return new SdkSource[0];
            } else {
                return list.toArray(new SdkSource[list.size()]);
            }
        }
    }

    /**
     * Returns an array of the sources across all categories. This is never null.
     */
    public SdkSource[] getAllSources() {
        synchronized (mSources) {
            int n = 0;

            for (ArrayList<SdkSource> list : mSources.values()) {
                n += list.size();
            }

            SdkSource[] sources = new SdkSource[n];

            int i = 0;
            for (ArrayList<SdkSource> list : mSources.values()) {
                for (SdkSource source : list) {
                    sources[i++] = source;
                }
            }

            return sources;
        }
    }

    /**
     * Each source keeps a local cache of whatever it loaded recently.
     * This calls {@link SdkSource#clearPackages()} on all the available sources,
     * and the next call to {@link SdkSource#getPackages()} will actually reload
     * the remote package list.
     */
    public void clearAllPackages() {
        synchronized (mSources) {
            for (ArrayList<SdkSource> list : mSources.values()) {
                for (SdkSource source : list) {
                    source.clearPackages();
                }
            }
        }
    }

    /**
     * Returns the category of a given source, or null if the source is unknown.
     * <p/>
     * Note that this method uses object identity to find a given source, and does
     * not identify sources by their URL like {@link #hasSourceUrl(SdkSource)} does.
     * <p/>
     * The search is O(N), which should be acceptable on the expectedly small source list.
     */
    public SdkSourceCategory getCategory(SdkSource source) {
        if (source != null) {
            synchronized (mSources) {
                for (Entry<SdkSourceCategory, ArrayList<SdkSource>> entry : mSources.entrySet()) {
                    if (entry.getValue().contains(source)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns true if there's already a similar source in the sources list
     * under any category.
     * <p/>
     * Important: The match is NOT done on object identity.
     * Instead, this searches for a <em>similar</em> source, based on
     * {@link SdkSource#equals(Object)} which compares the source URLs.
     * <p/>
     * The search is O(N), which should be acceptable on the expectedly small source list.
     */
    public boolean hasSourceUrl(SdkSource source) {
        synchronized (mSources) {
            for (ArrayList<SdkSource> list : mSources.values()) {
                for (SdkSource s : list) {
                    if (s.equals(source)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Returns true if there's already a similar source in the sources list
     * under the specified category.
     * <p/>
     * Important: The match is NOT done on object identity.
     * Instead, this searches for a <em>similar</em> source, based on
     * {@link SdkSource#equals(Object)} which compares the source URLs.
     * <p/>
     * The search is O(N), which should be acceptable on the expectedly small source list.
     */
    public boolean hasSourceUrl(SdkSourceCategory category, SdkSource source) {
        synchronized (mSources) {
            ArrayList<SdkSource> list = mSources.get(category);
            if (list != null) {
                for (SdkSource s : list) {
                    if (s.equals(source)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Loads all user sources. This <em>replaces</em> all existing user sources
     * by the ones from the property file.
     */
    public void loadUserAddons(ISdkLog log) {

        // Remove all existing user sources
        removeAll(SdkSourceCategory.USER_ADDONS);

        // Load new user sources from property file
        FileInputStream fis = null;
        try {
            String folder = AndroidLocation.getFolder();
            File f = new File(folder, SRC_FILENAME);
            if (f.exists()) {
                fis = new FileInputStream(f);

                Properties props = new Properties();
                props.load(fis);

                int count = Integer.parseInt(props.getProperty(KEY_COUNT, "0"));

                for (int i = 0; i < count; i++) {
                    String url = props.getProperty(String.format("%s%02d", KEY_SRC, i));  //$NON-NLS-1$
                    if (url != null) {
                        SdkSource s = new SdkAddonSource(url, null/*uiName*/);
                        if (!hasSourceUrl(s)) {
                            add(SdkSourceCategory.USER_ADDONS, s);
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            log.error(e, null);

        } catch (AndroidLocationException e) {
            log.error(e, null);

        } catch (IOException e) {
            log.error(e, null);

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Saves all the user sources.
     * @param log Logger. Cannot be null.
     */
    public void saveUserAddons(ISdkLog log) {
        FileOutputStream fos = null;
        try {
            String folder = AndroidLocation.getFolder();
            File f = new File(folder, SRC_FILENAME);

            fos = new FileOutputStream(f);

            Properties props = new Properties();

            int count = 0;
            for (SdkSource s : getSources(SdkSourceCategory.USER_ADDONS)) {
                props.setProperty(String.format("%s%02d", KEY_SRC, count), s.getUrl());  //$NON-NLS-1$
                count++;
            }
            props.setProperty(KEY_COUNT, Integer.toString(count));

            props.store( fos, "## User Sources for Android tool");  //$NON-NLS-1$

        } catch (AndroidLocationException e) {
            log.error(e, null);

        } catch (IOException e) {
            log.error(e, null);

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

    }
}
