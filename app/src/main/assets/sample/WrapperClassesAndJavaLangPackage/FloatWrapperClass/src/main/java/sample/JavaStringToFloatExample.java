package sample;
/*
  Convert Java String to Float example
  This example shows how we can convert String object to Float object.
*/

public class JavaStringToFloatExample {

  public static void main(String[] args) {

    //We can convert String to Float using one of the following ways.
    //1. Construct Float using constructor.
    Float fObj1 = new Float("100.564");
    System.out.println(fObj1);

    //2. Use valueOf method of Float class. This method is static.
    String str1 = "100.476";
    Float fObj2 = Float.valueOf(str1);
    System.out.println(fObj2);

    /*
    To convert a String object to a float primitive value parseFloat method
    of Float class. This is a static method.
    */
    String str2 = "76.39";
    float f = Float.parseFloat(str2);
    System.out.println(f);

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
