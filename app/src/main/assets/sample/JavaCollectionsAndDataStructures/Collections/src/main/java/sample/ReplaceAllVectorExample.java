package sample;
/*
  Replace all occurrences of specified element of Java Vector Example
  This java example shows how to replace all occurrences of a specified element
  of Java Vector using replaceAll method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class ReplaceAllVectorExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("A");
    v.add("C");
    v.add("D");

    System.out.println("Vector Contains : " + v);

    /*
      To replace all occurrences of specified element of Java Vector use,
      static boolean replaceAll(List list, Object oldVal, Object newVal) method
      of Collections class.

      This method returns true if the list contained one more elements replaced.

    */

    Collections.replaceAll(v, "A", "Replace All");

    System.out.println("After Replace All, Vector Contains : " + v);
  }
}

/*
 Output would be
 Vector Contains : [A, B, A, C, D]
 After Replace All, Vector Contains : [Replace All, B, Replace All, C, D]
 */
