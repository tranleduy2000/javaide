package sample;
/*
  Convert String to short primitive Example
  This example shows how a String object can be converted into short primitive type
  using parseShort method of Short class.
*/

public class StringToShortPrimitiveExample {

  public static void main(String[] args) {
    //declare String object
    String str = new String("10");

    /*
    use parseShort method of Short class to convert String into short primitive
    data type. This is a static method.
    Please note that this method can throw a NumberFormatException if the string
    is not parsable to short.
    */
    short s = Short.parseShort(str);
    System.out.println(s);
  }
}

/*
 Output the program would be
 10
 */
