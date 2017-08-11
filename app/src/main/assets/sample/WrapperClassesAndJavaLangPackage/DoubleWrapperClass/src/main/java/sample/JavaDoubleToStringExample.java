package sample;
/*
  Convert Double object to String object
  This example shows how a Double object can be converted into a String object.
*/

public class JavaDoubleToStringExample {

  public static void main(String[] args) {
    Double dObj = new Double(10.25);

    //use toString method of Double class to convert Double into String.
    String str = dObj.toString();
    System.out.println("Double converted to String as " + str);
  }
}

/*
 Output of the program would be
 Double converted to String as 10.25
 */
