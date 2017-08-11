package sample;
/*
  Convert Java String to Double example
  This example shows how we can convert String object to a Double object.
*/

public class JavaStringToDoubleExample {

  public static void main(String[] args) {

    //We can convert String to Double using one of the following ways.
    //1. Construct Double using constructor.
    Double dObj1 = new Double("100.564");
    System.out.println(dObj1);

    //2. Use valueOf method of Double class. This method is static.
    String str1 = "100.476";
    Double dObj2 = Double.valueOf(str1);
    System.out.println(dObj2);

    /*
    To convert a String object to a double primitive value parseDouble method
    of Double class. This is a static method.
    */
    String str2 = "76.39";
    double d = Double.parseDouble(str2);
    System.out.println(d);

    //Please note that these methods can throw a NumberFormatException if
    //string can not be parsed.

  }
}

/*
 Output of the program would be :
 100.564
 100.476
 76.39
 */
