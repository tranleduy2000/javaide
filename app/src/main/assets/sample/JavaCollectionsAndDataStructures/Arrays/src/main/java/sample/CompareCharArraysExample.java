package sample;
/*
  Compare Two Java char Arrays Example
  This java example shows how to compare two char arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareCharArraysExample {

  public static void main(String[] args) {
    //create character arrays
    char[] charArray1 = new char[]{'d', 'h', 'r', 'f'};
    char[] charArray2 = new char[]{'d', 'h', 'r', 'f'};

    /*
      To compare two char arrays use,
      static boolean equals(char array1[], char array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.
    */

    boolean blnResult = Arrays.equals(charArray1, charArray2);
    System.out.println("Are two char arrays equal ? : " + blnResult);

    /*
      Please note that two char array references pointing to null are
      considered as equal.
    */

  }
}

/*
 Output of the program would be
 Are two char arrays equal ? : true
 */
