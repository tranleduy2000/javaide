package sample;
/*
  Convert Java String to Short example
  This example shows how we can convert String object to Short object.
*/

public class StringToShortExample {

  public static void main(String[] args) {

    //We can convert String to Short using following ways.
    //1. Construct Short using constructor.
    Short sObj1 = new Short("100");
    System.out.println(sObj1);

    //2. Use valueOf method of Short class. This method is static.
    String str = "100";
    Short sObj2 = Short.valueOf(str);
    System.out.println(sObj2);

    //Please note that both method can throw a NumberFormatException if
    //string can not be parsed.

  }
}

/*
 Output of the program would be :
 100
 100
 */
