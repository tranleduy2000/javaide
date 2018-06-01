/*
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/**
 * Sort: a class that uses the quicksort algorithm to sort an
 *       array of objects.
 *
 * @author Sunita Mani
 */

package sun.misc;

public class Sort {

    private static void swap(Object arr[], int i, int j) {
        Object tmp;

        tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * quicksort the array of objects.
     *
     * @param arr[] - an array of objects
     * @param left - the start index - from where to begin sorting
     * @param right - the last index.
     * @param comp - an object that implemnts the Compare interface to resolve thecomparison.
     */
    public static void quicksort(Object arr[], int left, int right, Compare comp) {
        int i, last;

        if (left >= right) { /* do nothing if array contains fewer than two */
            return;          /* two elements */
        }
        swap(arr, left, (left+right) / 2);
        last = left;
        for (i = left+1; i <= right; i++) {
            if (comp.doCompare(arr[i], arr[left]) < 0) {
                swap(arr, ++last, i);
            }
        }
        swap(arr, left, last);
        quicksort(arr, left, last-1, comp);
        quicksort(arr, last+1, right, comp);
    }

    public static void quicksort(Object arr[], Compare comp) {
        quicksort(arr, 0, arr.length-1, comp);
    }
}
