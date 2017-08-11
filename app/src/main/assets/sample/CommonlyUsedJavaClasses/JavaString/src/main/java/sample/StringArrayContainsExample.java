package sample;
/*
       Java String Array Contains Example
       This Java String Array Contains example shows how to find a String in
       String array in Java.
*/

import java.util.Arrays;

public class StringArrayContainsExample {

  public static void main(String args[]) {

    //String array
    String[] strMonths = new String[]{"January", "February", "March", "April", "May"};

    //Strings to find
    String strFind1 = "March";
    String strFind2 = "December";

    /*
     * There are several ways we can check whether a String array
     * contains a particular string.
     *
     * First of them is iterating the array elements and check as given below.
     */

    boolean contains = false;

    //iterate the String array
    for (int i = 0; i < strMonths.length; i++) {

      //check if string array contains the string
      if (strMonths[i].equals(strFind1)) {

        //string found
        contains = true;
        break;
      }
    }

    if (contains) {
      System.out.println("String array contains String " + strFind1);
    } else {
      System.out.println("String array does not contain String " + strFind1);
    }

    /*
     * Second way to check whether String array contains a string is to use
     * Arrays class as given below.
     */

    contains = Arrays.asList(strMonths).contains(strFind1);
    System.out.println("Does String array contain " + strFind1 + "? " + contains);

    contains = Arrays.asList(strMonths).contains(strFind2);
    System.out.println("Does String array contain " + strFind2 + "? " + contains);
  }
}

/*
 Output of above given Java String Array Contains example would be
 String array contains String March
 Does String array contain March? true
 Does String array contain December? false
 */
