package sample;
/*
  Convert String to primitive byte Example
  This example shows how a String object can be converted into byte primitive type
  using parseByte method of Byte class.
*/

public class StringToPrimitiveByteExample {

  public static void main(String[] args) {
    //declare String object
    String str = new String("10");

    /*
    use parseInt method of Byte class to convert String into byte primitive
    data type. This is a static method.
    Please note that this method can throw a NumberFormatException if the string
    is not parsable to byte.
    */
    byte b = Byte.parseByte(str);
    System.out.println(b);
  }
}

/*
 Output the program would be
 10
 */
