package sample;
/*
  Convert Java String to Long example
  This example shows how we can convert String object to Long object.
*/

public class StringToLongExample {

  public static void main(String[] args) {

    //We can convert String to Long using following ways.
    //1. Construct Long using constructor.
    Long lObj1 = new Long("100");
    System.out.println(lObj1);

    //2. Use valueOf method of Long class. This method is static.
    String str = "100";
    Long lObj2 = Long.valueOf(str);
    System.out.println(lObj2);

    //Please note that both method can throw a NumberFormatException if
    //string can not be parsed.

  }
}

/*
 Output of the program would be :
 100
 100
 */
