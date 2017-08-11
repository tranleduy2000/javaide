package sample;
/*
  Convert String to long Example
  This example shows how a String object can be converted into long primitive type
  using parseLong method of Long class.
*/

public class StringToPrimitiveLongExample {

  public static void main(String[] args) {
    //declare String object
    String str = new String("10");

    /*
    use parseLong method of Long class to convert String into long primitive
    data type. This is a static method.
    Please note that this method can throw a NumberFormatException if the string
    is not parsable to long.
    */
    long l = Long.parseLong(str);
    System.out.println(l);
  }
}

/*
 Output the program would be
 10
 */
