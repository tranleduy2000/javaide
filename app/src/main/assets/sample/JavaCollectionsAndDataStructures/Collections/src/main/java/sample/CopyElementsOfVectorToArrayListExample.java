package sample;
/*
  Copy Elements of Vector to Java ArrayList Example
  This java example shows how to copy elements of Java Vector to Java ArrayList using
  copy method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class CopyElementsOfVectorToArrayListExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("1");
    v.add("2");
    v.add("3");

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("One");
    arrayList.add("Two");
    arrayList.add("Three");
    arrayList.add("Four");
    arrayList.add("Five");

    /*
      To copy elements of Java Vector to Java ArrayList use,
      static void copy(List dstList, List sourceList) method of Collections class.

      This method copies all elements of source list to destination list. After copy
      index of the elements in both source and destination lists would be identical.

      The destination list must be long enough to hold all copied elements. If it is
      longer than that, the rest of the destination list's elments would remain
      unaffected.
    */

    System.out.println("Before copy ArrayList Contains : " + arrayList);

    //copy all elements of Vector to ArrayList using copy method of Collections class
    Collections.copy(arrayList, v);

    /*
      Please note that, If ArrayList is not long enough to hold all elements of
      Vector, it throws IndexOutOfBoundsException.
    */

    System.out.println("After Copy ArrayList Contains : " + arrayList);
  }
}

/*
 Output would be
 Before copy ArrayList Contains : [One, Two, Three, Four, Five]
 After Copy ArrayList Contains : [1, 2, 3, Four, Five]
 */
