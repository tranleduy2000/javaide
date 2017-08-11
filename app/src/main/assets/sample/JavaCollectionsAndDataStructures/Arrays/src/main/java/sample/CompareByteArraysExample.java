package sample;
/*
  Compare Two Java byte Arrays Example
  This java example shows how to compare two byte arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareByteArraysExample {

  public static void main(String[] args) {
    //create byte arrays
    byte[] byteArray1 = new byte[]{7, 25, 12};
    byte[] byteArray2 = new byte[]{7, 25, 12};

    /*
      To compare two byte arrays use,
      static boolean equals(byte array1[], byte array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.
    */

    boolean blnResult = Arrays.equals(byteArray1, byteArray2);
    System.out.println("Are two byte arrays equal ? : " + blnResult);

    /*
      Please note that two byte array references pointing to null are
      considered as equal.
    */

  }
}

/*
 Output of the program would be
 Are two byte arrays equal ? : true
 */
