package sample;
/*
  Compare Two Java long Arrays Example
  This java example shows how to compare two long arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareLongArraysExample {

  public static void main(String[] args) {
    //create long arrays
    long[] longArray1 = new long[]{213873, 87210, 320918};
    long[] longArray2 = new long[]{213873, 87210, 320918};

    /*
      To compare two long arrays use,
      static boolean equals(long array1[], long array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.
    */

    boolean blnResult = Arrays.equals(longArray1, longArray2);
    System.out.println("Are two long arrays equal ? : " + blnResult);

    /*
      Please note that two long array references pointing to null are
      considered as equal.
    */

  }
}

/*
 Output of the program would be
 Are two long arrays equal ? : true
 */
