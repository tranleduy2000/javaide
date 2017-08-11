package sample;
/*
  Convert Double to numeric primitive data types example
  This example shows how a Double object can be converted into below given numeric
  primitive data types
  - Convert Double to byte
  - Convert Double to short
  - Convert Double to int
  - Convert Double to float
  - Convert Double to double
*/

public class JavaDoubleToNumericPrimitiveTypesExample {

  public static void main(String[] args) {

    Double dObj = new Double("10.50");
    //use byteValue method of Double class to convert it into byte type.
    byte b = dObj.byteValue();
    System.out.println(b);

    //use shortValue method of Double class to convert it into short type.
    short s = dObj.shortValue();
    System.out.println(s);

    //use intValue method of Double class to convert it into int type.
    int i = dObj.intValue();
    System.out.println(i);

    //use floatValue method of Double class to convert it into float type.
    float f = dObj.floatValue();
    System.out.println(f);

    //use doubleValue method of Double class to convert it into double type.
    double d = dObj.doubleValue();
    System.out.println(d);
  }
}

/*
 Output of the program would be :
 10
 10
 10
 10.5
 10.5
 */
