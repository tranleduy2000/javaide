package sample;
/*
       Java Convert int Array To String Example
       This Java Convert int Array To String example shows how to find convert an array of int
       to a String in Java.
*/

import java.util.Arrays;

public class ConvertIntArrayToStringExample {

  public static void main(String args[]) {

    //int array
    int[] intNumbers = new int[]{1, 2, 3, 4, 5};

    /*
     * First approach is to loop through all elements of an int array
     * and append them to StringBuffer object one by one. At the end,
     * use toString method to convert it to String.
     */

    //create new StringBuffer object
    StringBuffer sbfNumbers = new StringBuffer();

    //define the separator you want in the string. This example uses space.
    String strSeparator = " ";

    if (intNumbers.length > 0) {

      //we do not want leading space for first element
      sbfNumbers.append(intNumbers[0]);

      /*
       * Loop through the elements of an int array. Please
       * note that loop starts from 1 not from 0 because we
       * already appended the first element without leading space.s
       */
      for (int i = 1; i < intNumbers.length; i++) {
        sbfNumbers.append(strSeparator).append(intNumbers[i]);
      }
    }

    System.out.println("int array converted to String using for loop");

    //finally convert StringBuffer to String using toString method
    System.out.println(sbfNumbers.toString());

    /*
     * Second options is to use Arrays class as given below.
     * Use Arrays.toString method to convert int array to String.
     *
     * However, it will return String like [1, 2, 3, 4, 5]
     */

    String strNumbers = Arrays.toString(intNumbers);

    System.out.println("String generated from Arrays.toString method: " + strNumbers);

    //you can use replaceAll method to replace brackets and commas
    strNumbers = strNumbers.replaceAll(", ", strSeparator).replace("[", "").replace("]", "");

    System.out.println("Final String: " + strNumbers);
  }
}

/*
 Output of above given convert int array to String example would be
 int array converted to String using for loop
 1 2 3 4 5
 String generated from Arrays.toString method: [1, 2, 3, 4, 5]
 Final String: 1 2 3 4 5
 */
