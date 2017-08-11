package sample;
/*
  Convert Integer to numeric primitive data types example
  This example shows how an Integer object can be converted into below given numeric
  data types
  - Convert Integer to byte
  - Convert Integer to short
  - Convert Integer to int
  - Convert Integer to float
  - Convert Integer to double
*/

public class IntegerToNumericPrimitiveTypesExample {

  public static void main(String[] args) {
    Integer intObj = new Integer("10");
    //use byteValue method of Integer class to convert it into byte type.
    byte b = intObj.byteValue();
    System.out.println(b);

    //use shortValue method of Integer class to convert it into short type.
    short s = intObj.shortValue();
    System.out.println(s);

    //use intValue method of Integer class to convert it into int type.
    int i = intObj.intValue();
    System.out.println(i);

    //use floatValue method of Integer class to convert it into float type.
    float f = intObj.floatValue();
    System.out.println(f);

    //use doubleValue method of Integer class to convert it into double type.
    double d = intObj.doubleValue();
    System.out.println(d);
  }
}

/*
 Output of the program would be :
 10
 10
 10
 10.0
 10.0
 */
