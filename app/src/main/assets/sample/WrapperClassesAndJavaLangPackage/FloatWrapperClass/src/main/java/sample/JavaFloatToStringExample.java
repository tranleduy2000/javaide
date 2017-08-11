package sample;
/*
  Convert Float object to String object
  This example shows how a Float object can be converted into a String object.
*/

public class JavaFloatToStringExample {

  public static void main(String[] args) {

    Float fObj = new Float(10.25);
    //use toString method of Float class to convert Float into String.
    String str = fObj.toString();
    System.out.println("Float converted to String as " + str);
  }
}

/*
 Output of the program would be
 Float converted to String as 10.25
 */
