package sample;
/*
  Java Short Example
  This example shows how an object of Short can be declared and used.
  Short is a wrapper class provided to wrap short primitive value. It has a single
  field of type short.
*/

public class ShortExample {

  public static void main(String[] args) {
    //create a Short object using one of the below given constructors
    //1. Create a Short object from short
    short s = 10;
    Short sObj1 = new Short(s);

    /*
    2. Create Short object from String. Please note that this method can
    throw NumberFormatException if string doesnt contain parsable number.
    */
    Short sObj2 = new Short("10");

    //print value of Short objects
    System.out.println(sObj1);
    System.out.println(sObj2);
  }
}

/*
  Output of the program would be
  10
  10
  */
