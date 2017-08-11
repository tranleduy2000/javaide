package sample;
/*
  Convert Long to numeric primitive data types example
  This example shows how a Long object can be converted into below given numeric
  data types
  - Convert Long to byte
  - Convert Long to short
  - Convert Long to int
  - Convert Long to float
  - Convert Long to double
*/

public class LongToNumericPrimitiveTypesExample {

  public static void main(String[] args) {
    Long lObj = new Long("10");
    //use byteValue method of Long class to convert it into byte type.
    byte b = lObj.byteValue();
    System.out.println(b);

    //use shortValue method of Long class to convert it into short type.
    short s = lObj.shortValue();
    System.out.println(s);

    //use intValue method of Long class to convert it into int type.
    int i = lObj.intValue();
    System.out.println(i);

    //use floatValue method of Long class to convert it into float type.
    float f = lObj.floatValue();
    System.out.println(f);

    //use doubleValue method of Long class to convert it longo double type.
    double d = lObj.doubleValue();
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
