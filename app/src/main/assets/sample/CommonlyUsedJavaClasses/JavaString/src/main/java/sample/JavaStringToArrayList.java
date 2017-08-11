package sample;

import java.util.ArrayList;
import java.util.Arrays;

/*
       Java String to ArrayList Example.
       This Java String to ArrayList example shows how to convert Java String containing values to
       Java ArrayList.

*/
public class JavaStringToArrayList {
  public static void main(String args[]) {

    //String object having values, separated by ","
    String strNumbers = "1,2,3,4,5";

    /*
     * To convert Java String to ArrayList, first split the string and then
     * use asList method of Arrays class to convert it to ArrayList.
     */

    //split the string using separator, in this case it is ","
    String[] strValues = strNumbers.split(",");

    /*
     * Use asList method of Arrays class to convert Java String array to ArrayList
     */
    ArrayList<String> aListNumbers = new ArrayList<String>(Arrays.asList(strValues));

    System.out.println("Java String converted to ArrayList: " + aListNumbers);
  }
}

/*
 Output of Java String to ArrayList example would be
 Java String converted to ArrayList: [1, 2, 3, 4, 5]
 */
