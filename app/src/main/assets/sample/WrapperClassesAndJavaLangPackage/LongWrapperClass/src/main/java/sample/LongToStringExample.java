package sample;
/*
  Convert Long object to String object
  This example shows how a Long object can be converted into String object.
*/

public class LongToStringExample {

  public static void main(String[] args) {
    Long lObj = new Long(10);

    //use toString method of Long class to convert Long into String.
    String str = lObj.toString();
    System.out.println("Long converted to String as " + str);
  }
}

/*
 Output of the program would be
 Long converted to String as 10
 */
