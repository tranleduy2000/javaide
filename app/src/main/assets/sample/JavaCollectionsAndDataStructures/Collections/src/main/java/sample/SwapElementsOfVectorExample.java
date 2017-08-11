package sample;
/*
  Swap elements of Java Vector example
  This java example shows how to swap elements of Java Vector object using
  swap method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class SwapElementsOfVectorExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("1");
    v.add("2");
    v.add("3");
    v.add("4");
    v.add("5");

    System.out.println("Before swaping, Vector contains : " + v);

    /*
      To swap elements of Java Vector use,
      static void swap(List list, int firstElement, int secondElement)
      method of Collections class. Where firstElement is the index of first
      element to be swapped and secondElement is the index of the second element
      to be swapped.

      If the specified positions are equal, list remains unchanged.

      Please note that, this method can throw IndexOutOfBoundsException if
      any of the index values is not in range.
    */

    Collections.swap(v, 0, 4);

    System.out.println("After swaping, Vector contains : " + v);
  }
}

/*
 Output would be
 Before swaping, Vector contains : [1, 2, 3, 4, 5]
 After swaping, Vector contains : [5, 2, 3, 4, 1]
 */
