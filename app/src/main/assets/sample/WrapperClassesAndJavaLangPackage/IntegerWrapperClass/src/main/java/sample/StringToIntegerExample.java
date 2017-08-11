package sample;
/*
  Convert String to Integer example
  This example shows how we can convert String object to Integer object.
*/

public class StringToIntegerExample {

  public static void main(String[] args) {

    //We can convert String to Integer using following ways.
    //1. Construct Integer using constructor.
    Integer intObj1 = new Integer("100");
    System.out.println(intObj1);

    //2. Use valueOf method of Integer class. This method is static.
    String str = "100";
    Integer intObj2 = Integer.valueOf(str);
    System.out.println(intObj2);

    //Please note that both method can throw a NumberFormatException if
    //string can not be parsed.

  }
}

/*
 Output of the program would be :
 100
 100
 */
