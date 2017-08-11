package sample;
/*
  Enumerate through a Vector using Java Enumeration Example
  This Java Example shows how to enumerate through elements of a Vector
  using Java Enumeration.
*/

import java.util.Enumeration;
import java.util.Vector;

public class EnumerateThroughVectorExample {

  public static void main(String[] args) {
    //create a Vector object
    Vector v = new Vector();

    //populate the Vector
    v.add("One");
    v.add("Two");
    v.add("Three");
    v.add("Four");

    //Get Enumeration of Vector's elements using elements() method
    Enumeration e = v.elements();

    /*
      Enumeration provides two methods to enumerate through the elements.
      It's hasMoreElements method returns true if there are more elements to
      enumerate through otherwise it returns false. Its nextElement method returns
      the next element in enumeration.
    */

    System.out.println("Elements of the Vector are : ");

    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}
/*
Output would be
Elements of the Vector are :
One
Two
Three
Four
*/
