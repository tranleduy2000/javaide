package sample;
/*
  Create Java ArrayList From Enumeration Example
  This java example shows how to create an ArrayList from any Enumeration object
  using list method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

public class CreateArrayListFromEnumerationExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("D");
    v.add("E");
    v.add("F");

    System.out.println("Vector contains : " + v);

    /*
      To create ArrayList from any Enumeration, use
      static ArrayList list(Enumeration e) method of Collections class.

      This method returns the ArrayList containing the elements returned by
      specified Enumeration object in order they are returned.
    */

    //Get Enumeration over Vector
    Enumeration e = v.elements();

    //Create ArrayList from Enumeration of Vector
    ArrayList aList = Collections.list(e);

    System.out.println("Arraylist contains : " + aList);
  }
}

/*
 Output would be
 Vector Contains : [A, B, D, E, F]
 Arraylist contains : [A, B, D, E, F]
 */
