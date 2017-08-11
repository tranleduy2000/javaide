package sample;
/*
  Java Double Example
  This example shows how an object of Double can be declared and used.
  Double is a wrapper class provided to wrap double primitive value. It has a single
  field of type double.
*/

public class JavaDoubleExample {

  public static void main(String[] args) {

    //create a Double object using one of the below given constructors

    //1. Create an Double object from double primitive type
    double d = 10.10;
    Double dObj1 = new Double(d);
    System.out.println(dObj1);

    /*
    2. Create Double object from String. Please note that this method can
    throw NumberFormatException if string doesnt contain parsable number.
    */
    Double dObj3 = new Double("25.34");
    System.out.println(dObj3);
  }
}

/*
  Output of the program would be
  10.1
  25.34
  */
