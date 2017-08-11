package sample;
/*
  Compare Two Java double Arrays Example
  This java example shows how to compare two double arrays for equality using
  Arrays.equals method.
*/

import java.util.Arrays;

public class CompareDoubleArraysExample {

  public static void main(String[] args) {
    //create double arrays
    double[] dblArray1 = new double[]{10.3221, 789.23, 427.213};
    double[] dblArray2 = new double[]{10.3221, 789.23, 427.213};

    /*
      To compare two double arrays use,
      static boolean equals(double array1[], double array2[]) method of Arrays class.

      It returns true if both arrays are equal. Arrays are considered as equal
      if they contain same elements in same order.

      Two double elements are considered as equal if
      new Double(d1).equals(new Double(d2))
    */

    boolean blnResult = Arrays.equals(dblArray1, dblArray2);
    System.out.println("Are two double arrays equal ? : " + blnResult);

    /*
      Please note that two double array references pointing to null are
      considered as equal.
      Also, two NaN values are considerd equal. But 0.0d and -0.0d are
      considered as unequal.
    */

  }
}

/*
 Output of the program would be
 Are two double arrays equal ? : true
 */
