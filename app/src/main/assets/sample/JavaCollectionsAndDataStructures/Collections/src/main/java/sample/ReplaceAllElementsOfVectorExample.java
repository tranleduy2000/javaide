package sample;
/*
  Replace All Elements Of Java Vector Example
  This java example shows how to replace all elements of Java Vector object with
  the specified element using fill method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class ReplaceAllElementsOfVectorExample {

  public static void main(String[] args) {
    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("D");

    /*
      To replace all elements of Java Vector with specified element use,
      static void fill(List list, Object element) method of Collections class.
    */

    System.out.println("Before replacement, Vector contains : " + v);

    Collections.fill(v, "REPLACED");

    System.out.println("After replacement, Vector contains : " + v);
  }
}

/*
 Output would be
 Before replacement, Vector contains : [A, B, D]
 After replacement, Vector contains : [REPLACED, REPLACED, REPLACED]
 */
