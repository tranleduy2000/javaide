package sample;
/*
  Java Long Example
  This example shows how an object of Long can be declared and used.
  Long is a wrapper class provided to wrap long primitive value. It has a single
  field of type long.
*/

public class LongExample {

  public static void main(String[] args) {
    //create a Long object using one the below given constructors
    //1. Create a Long object from long
    long l = 10;
    Long longObj1 = new Long(l);

    /*
    2. Create a Long object from String. Please note that this method can
    throw NumberFormatException if string doesnt contain parsable number.
    */
    Long longObj2 = new Long("5");

    //print value of Long objects
    System.out.println(longObj1);
    System.out.println(longObj2);
  }
}

/*
  Output of the program would be
  10
  5
  */
