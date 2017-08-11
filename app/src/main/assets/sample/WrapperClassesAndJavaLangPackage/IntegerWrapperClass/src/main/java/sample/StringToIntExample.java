package sample;
/*
  Convert String to int Example
  This Convert String to int Java example shows how a String object can be converted into int primitive type
  using parseInt method of Integer class.
*/

public class StringToIntExample {

  public static void main(String[] args) {
    //declare String object
    String str = new String("10");

    /*
    use parseInt method of Integer class to convert String into int primitive
    data type. This is a static method.
    Please note that this method can throw a NumberFormatException if the string
    is not parsable to int.
    */
    int i = Integer.parseInt(str);
    System.out.println(i);
  }
}

/*
 Output the program would be
 10
 */
