package sample;
/*
  Compare Two Java short Arrays Example
  This java example shows how to compare two short arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareShortArraysExample {

  public static void main(String[] args) {
    //create short arrays
    short[] shortArray1 = new short[]{107, 93, 58};
    short[] shortArray2 = new short[]{107, 93, 58};

    /*
      To compare two short arrays use,
      static boolean equals(short array1[], short array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.
    */

    boolean blnResult = Arrays.equals(shortArray1, shortArray2);
    System.out.println("Are two short arrays equal ? : " + blnResult);

    /*
      Please note that two short array references pointing to null are
      considered as equal.
    */

  }
}

/*
 Output of the program would be
 Are two short arrays equal ? : true
 */
