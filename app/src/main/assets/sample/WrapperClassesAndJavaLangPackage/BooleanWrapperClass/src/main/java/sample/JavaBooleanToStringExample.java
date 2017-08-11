package sample;
/*
  Convert java Boolean object to String object Example
  This example shows how to convert java Boolean object into String object.
*/

public class JavaBooleanToStringExample {

  public static void main(String[] args) {
    //construct Boolean object
    Boolean blnObj = new Boolean("true");

    //use toString method of Boolean class to convert it into String
    String str = blnObj.toString();
    System.out.println(str);
  }
}

/*
 Output would be
 true
 */
