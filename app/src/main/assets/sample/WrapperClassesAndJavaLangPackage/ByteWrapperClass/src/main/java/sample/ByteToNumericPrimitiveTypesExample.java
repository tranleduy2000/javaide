package sample;
/*
  Convert Byte to numeric primitive data types example
  This example shows how a Byte object can be converted into below given numeric
  data types
  - Convert Byte to byte
  - Convert Byte to short
  - Convert Byte to int
  - Convert Byte to float
  - Convert Byte to double
  - Convert Byte to long
*/

public class ByteToNumericPrimitiveTypesExample {

  public static void main(String[] args) {
    Byte bObj = new Byte("10");
    //use byteValue method of Byte class to convert it into byte type.
    byte b = bObj.byteValue();
    System.out.println(b);

    //use shortValue method of Byte class to convert it into short type.
    short s = bObj.shortValue();
    System.out.println(s);

    //use intValue method of Byte class to convert it into int type.
    int i = bObj.intValue();
    System.out.println(i);

    //use floatValue method of Byte class to convert it into float type.
    float f = bObj.floatValue();
    System.out.println(f);

    //use doubleValue method of Byte class to convert it into double type.
    double d = bObj.doubleValue();
    System.out.println(d);

    //use longValue method of Byte class to convert it into long type.
    long l = bObj.longValue();
    System.out.println(l);
  }
}

/*
 Output of the program would be :
 10
 10
 10
 10.0
 10.0
 10
 */
