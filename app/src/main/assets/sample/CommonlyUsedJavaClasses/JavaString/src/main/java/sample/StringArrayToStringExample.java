package sample;
/*
       Java String Array to String Example
       This Java String Array to String Length example shows how to convert String array to
       Java String object.
*/

import java.util.Arrays;

public class StringArrayToStringExample {

  public static void main(String args[]) {

    //String array
    String[] strArray = new String[]{"Java", "String", "Array", "To", "String", "Example"};

    /*
     * There are several in which we can convert String array to
     * String.
     */

    /*
     * 1. Using Arrays.toString method
     * This method returns string like [element1, element2..]
     */

    String str1 = Arrays.toString(strArray);
    //replace starting "[" and ending "]" and ","
    str1 = str1.substring(1, str1.length() - 1).replaceAll(",", "");

    System.out.println("String 1: " + str1);
    /*
     * 2. Using Arrays.asList method followed by toString.
     * This method also returns string like [element1, element2..]
     */

    String str2 = Arrays.asList(strArray).toString();
    //replace starting "[" and ending "]" and ","
    str2 = str2.substring(1, str2.length() - 1).replaceAll(",", "");
    System.out.println("String 2: " + str2);

    /*
     * PLEASE NOTE that if the any of the array elements contain ",", it will be
     * replaced too. So above both methods does not work 100%.
     */

    //3. Using StringBuffer.append method
    StringBuffer sbf = new StringBuffer();

    if (strArray.length > 0) {

      sbf.append(strArray[0]);
      for (int i = 1; i < strArray.length; i++) {
        sbf.append(" ").append(strArray[i]);
      }
    }
    System.out.println("String 3: " + sbf.toString());
  }
}

/*
 Output of above example would be
 String 1: Java String Array To String Example
 String 2: Java String Array To String Example
 String 3:Java String Array To String Example
 */
