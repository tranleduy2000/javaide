package sample;
/*
  Compare Two Java boolean Arrays Example
  This java example shows how to compare two boolean arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareBooleanArraysExample {

  public static void main(String[] args) {
    //create boolean arrays
    boolean[] blnArray1 = new boolean[]{true, false, true};
    boolean[] blnArray2 = new boolean[]{true, false, true};

    /*
      To compare two boolean arrays use,
      static boolean equals(boolean array1[], boolean array2[])
      method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.
    */

    boolean blnResult = Arrays.equals(blnArray1, blnArray2);
    System.out.println("Are two boolean arrays equal ? : " + blnResult);

    /*
      Please note that two boolean array references pointing to null are
      considered as equal.
    */

  }
}

/*
 Output of the program would be
 Are two boolean arrays equal ? : true
 */
