package sample;
/*
  Reverse order of all elements of Java Vector Example
  This java example shows how to reverse the order of all elements of Java Vector
  using reverse method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class ReverseOrderVectorExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("C");
    v.add("D");
    v.add("E");

    System.out.println("Before Reverse Order, Vector Contains : " + v);

    /*
      To reverse the order of all elements of Java Vector use,
      static void reverse(List list) method of Collections class.

      This method reverse the order of elements of specified list.
    */

    Collections.reverse(v);

    System.out.println("After Reverse Order, Vector Contains : " + v);
  }
}

/*
 Output would be
 Before Reverse Order, Vector Contains : [A, B, C, D, E]
 After Reverse Order, Vector Contains : [E, D, C, B, A]
 */
