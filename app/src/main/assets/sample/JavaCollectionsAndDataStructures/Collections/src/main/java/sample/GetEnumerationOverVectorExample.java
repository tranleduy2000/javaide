package sample;
/*
  Get Enumeration over Java Vector Example
  This java example shows how to get Enumeration over Java Vector using enumeration
  method of Collections class. This example also shows how to enumerate through
  elements of Java Vector.
*/

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

public class GetEnumerationOverVectorExample {

  public static void main(String[] args) {
    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("D");
    v.add("E");
    v.add("F");

    /*
       Get Enumeration over Java Vector object using,
       static Enumeration enumeration(Collection c) method of Collections class.

       This method returns the enumeration object over the specified Collection.

    */

    //get the Enumeration object
    Enumeration e = Collections.enumeration(v);

    //enumerate through the Vector elements
    System.out.println("Enumerating through Java Vector");
    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}

/*
 Output would be
 Enumerating through Java Vector
 A
 B
 D
 E
 F
 */
