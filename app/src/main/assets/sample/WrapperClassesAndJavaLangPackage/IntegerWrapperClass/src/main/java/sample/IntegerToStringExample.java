package sample;
/*
  Convert Integer object to String object
  This example shows how a Integer object can be converted into String object.
*/

public class IntegerToStringExample {

  public static void main(String[] args) {
    Integer intObj = new Integer(10);

    //use toString method of Integer class to conver Integer into String.
    String str = intObj.toString();
    System.out.println("Integer converted to String as " + str);
  }
}

/*
 Output of the program would be
 Integer converted to String as 10
 */
