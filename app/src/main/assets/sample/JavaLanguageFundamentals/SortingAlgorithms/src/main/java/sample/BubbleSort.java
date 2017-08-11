package sample;
/*
        Java Bubble Sort Example
        This Java bubble sort example shows how to sort an array of int using bubble
        sort algorithm. Bubble sort is the simplest sorting algorithm.
*/

public class BubbleSort {

  public static void main(String[] args) {

    //create an int array we want to sort using bubble sort algorithm
    int intArray[] = new int[]{5, 90, 35, 45, 150, 3};

    //print array before sorting using bubble sort algorithm
    System.out.println("Array Before Bubble Sort");
    for (int i = 0; i < intArray.length; i++) {
      System.out.print(intArray[i] + " ");
    }

    //sort an array using bubble sort algorithm
    bubbleSort(intArray);

    System.out.println("");

    //print array after sorting using bubble sort algorithm
    System.out.println("Array After Bubble Sort");
    for (int i = 0; i < intArray.length; i++) {
      System.out.print(intArray[i] + " ");
    }
  }

  private static void bubbleSort(int[] intArray) {

    /*
     * In bubble sort, we basically traverse the array from first
     * to array_length - 1 position and compare the element with the next one.
     * Element is swapped with the next element if the next element is greater.
     *
     * Bubble sort steps are as follows.
     *
     * 1. Compare array[0] & array[1]
     * 2. If array[0] > array [1] swap it.
     * 3. Compare array[1] & array[2]
     * 4. If array[1] > array[2] swap it.
     * ...
     * 5. Compare array[n-1] & array[n]
     * 6. if [n-1] > array[n] then swap it.
     *
     * After this step we will have largest element at the last index.
     *
     * Repeat the same steps for array[1] to array[n-1]
     *
     */

    int n = intArray.length;
    int temp = 0;

    for (int i = 0; i < n; i++) {
      for (int j = 1; j < (n - i); j++) {

        if (intArray[j - 1] > intArray[j]) {
          //swap the elements!
          temp = intArray[j - 1];
          intArray[j - 1] = intArray[j];
          intArray[j] = temp;
        }
      }
    }
  }
}

/*
 Output of the Bubble Sort Example would be

 Array Before Bubble Sort
 5 90 35 45 150 3
 Array After Bubble Sort
 3 5 35 45 90 150

 */
