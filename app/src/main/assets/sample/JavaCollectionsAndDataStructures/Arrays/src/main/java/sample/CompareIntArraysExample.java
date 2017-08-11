package sample;
/*
  Compare Two Java int Arrays Example
  This java example shows how to compare two integer arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareIntArraysExample {

  public static void main(String[] args) {
    //create int arrays
    int[] intArray1 = new int[]{27, 78, 1023};
    int[] intArray2 = new int[]{27, 78, 1023};

    /*
      To compare two int arrays use,
      static boolean equals(int array1[], int array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.
    */

    boolean blnResult = Arrays.equals(intArray1, intArray2);
    System.out.println("Are two int arrays equal ? : " + blnResult);

    /*
      Please note that two int array references pointing to null are
      considered as equal.
    */

  }
}

/*
 Output of the program would be
 Are two int arrays equal ? : true
 */
