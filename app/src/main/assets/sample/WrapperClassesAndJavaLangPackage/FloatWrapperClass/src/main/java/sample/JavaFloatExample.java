package sample;
/*
  Java Float Example
  This example shows how an object of Float can be declared and used.
  Float is a wrapper class provided to wrap float primitive value. It has a single
  field of type float.
*/

public class JavaFloatExample {

  public static void main(String[] args) {

    //create a Float object using one of the below given constructors

    //1. Create an Float object from float primitive type
    float f = 10.10f;
    Float fObj1 = new Float(f);
    System.out.println(fObj1);

    //2. Create an Float object from double primitive type
    double d = 10.10;
    Float fObj2 = new Float(d);
    System.out.println(fObj2);

    /*
    3. Create and Float object from String. Please note that this method can
    throw NumberFormatException if string doesnt contain parsable number.
    */
    Float fObj3 = new Float("25.34");
    System.out.println(fObj3);
  }
}

/*
  Output of the program would be
  10.1
  10.1
  25.34
  */
