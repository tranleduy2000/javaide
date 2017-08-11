package sample;
/*
  Convert Short to numeric primitive data types example
  This example shows how a Short object can be converted into below given numeric
  data types
  - Convert Short to byte
  - Convert Short to short
  - Convert Short to int
  - Convert Short to float
  - Convert Short to double
  - Convert Short to long
*/

public class ShortToNumericPrimitiveTypesExample {

  public static void main(String[] args) {
    Short sObj = new Short("10");
    //use byteValue method of Short class to convert it into byte type.
    byte b = sObj.byteValue();
    System.out.println(b);

    //use shortValue method of Short class to convert it into short type.
    short s = sObj.shortValue();
    System.out.println(s);

    //use intValue method of Short class to convert it into int type.
    int i = sObj.intValue();
    System.out.println(i);

    //use floatValue method of Short class to convert it into float type.
    float f = sObj.floatValue();
    System.out.println(f);

    //use doubleValue method of Short class to convert it into double type.
    double d = sObj.doubleValue();
    System.out.println(d);

    //use longValue method of Short class to convert it into long type.
    long l = sObj.longValue();
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
