package sample;
/*
  Compare Two Java float Arrays Example
  This java example shows how to compare two float arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareFloatArraysExample {

  public static void main(String[] args) {
    //create float arrays
    float[] floatArray1 = new float[]{459.432f, 348.43f, 5832.327f};
    float[] floatArray2 = new float[]{459.432f, 348.43f, 5832.327f};

    /*
      To compare two float arrays use,
      static boolean equals(float array1[], float array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.

      Two float elements are considered as equal if
      new Float(d1).equals(new Float(d2))
    */

    boolean blnResult = Arrays.equals(floatArray1, floatArray2);
    System.out.println("Are two float arrays equal ? : " + blnResult);

    /*
      Please note that two float array references pointing to null are
      considered as equal.
      Also, two NaN values are considerd equal. But 0.0f and -0.0f are
      considered as unequal.
    */

  }
}

/*
 Output of the program would be
 Are two float arrays equal ? : true
 */
