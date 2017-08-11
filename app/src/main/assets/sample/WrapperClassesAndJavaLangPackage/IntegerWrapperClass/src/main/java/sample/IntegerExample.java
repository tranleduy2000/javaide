package sample;
/*
  Java Integer Example
  This example shows how object of Integer can be declared and used.
  Integer is a wrapper class provided to wrap int primitive value. It has a single
  field of type int.
*/

public class IntegerExample {

  public static void main(String[] args) {
    //create an Integer object using one the below given constructors
    //1. Create an Integer object from int
    Integer intObj1 = new Integer(10);

    /*
    2. Create and Integer object from String. Please note that this method can
    throw NumberFormatException if string doesnt contain parsable number.
    */
    Integer intObj2 = new Integer("10");

    //print value of Integer objects
    System.out.println(intObj1);
    System.out.println(intObj2);
  }
}

/*
  Output of the program would be
  10
  10
  */
