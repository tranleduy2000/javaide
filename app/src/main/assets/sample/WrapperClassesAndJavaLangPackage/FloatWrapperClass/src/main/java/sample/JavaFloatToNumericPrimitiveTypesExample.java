package sample;
/*
  Convert Float to numeric primitive data types example
  This example shows how a Float object can be converted into below given numeric
  primitive data types
  - Convert Float to byte
  - Convert Float to short
  - Convert Float to int
  - Convert Float to float
  - Convert Float to double
*/

public class JavaFloatToNumericPrimitiveTypesExample {

  public static void main(String[] args) {

    Float fObj = new Float("10.50");
    //use byteValue method of Float class to convert it into byte type.
    byte b = fObj.byteValue();
    System.out.println(b);

    //use shortValue method of Float class to convert it into short type.
    short s = fObj.shortValue();
    System.out.println(s);

    //use intValue method of Float class to convert it into int type.
    int i = fObj.intValue();
    System.out.println(i);

    //use floatValue method of Float class to convert it into float type.
    float f = fObj.floatValue();
    System.out.println(f);

    //use doubleValue method of Float class to convert it into double type.
    double d = fObj.doubleValue();
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
