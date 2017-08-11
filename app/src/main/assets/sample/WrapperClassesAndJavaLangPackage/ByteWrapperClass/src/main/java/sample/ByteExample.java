package sample;
/*
  Java Byte Example
  This example shows how an object of Byte can be declared and used.
  Byte is a wrapper class provided to wrap byte primitive value. It has a single
  field of type byte.
*/

public class ByteExample {

  public static void main(String[] args) {
    //create a Byte object using one of the below given constructors
    //1. Create a Byte object from byte
    byte b = 10;
    Byte bObj1 = new Byte(b);

    /*
    2. Create Byte object from String. Please note that this method can
    throw NumberFormatException if string doesnt contain parsable number.
    */
    Byte bObj2 = new Byte("4");

    //print value of Byte objects
    System.out.println(bObj1);
    System.out.println(bObj2);
  }
}

/*
  Output of the program would be
  10
  4
  */
