package com.duy.android.compiler.repo.maven.cache;

import java.net.URI;

/**
 * Convenient map to search for the path where file is locally stored
 * by uri of the resource the file was downloaded from.
 * Implementations should not read/write file bodies using stored paths.
 *
 * @author Paul Polishchuk
 * @since 1.3.1
 */
interface FileIndex {

    /**
     * Adds given path to the index using uri parameter as a key.
     * @param uri index key
     * @param path index value
     */
    void put(URI uri, String path);

    /**
     * Check if a path associated with the uri key in the index.
     * <p>Use this method before actually trying to get value.
     * @param uri index key
     * @return true if some path associated with given key
     */
    boolean contains(URI uri);

    /**
     * Gets stored value by the key.
     * @param uri index key
     * @return path by given uri key; never NULL
     * @throws IllegalStateException in case key is not found
     */
    String get(URI uri);
}
