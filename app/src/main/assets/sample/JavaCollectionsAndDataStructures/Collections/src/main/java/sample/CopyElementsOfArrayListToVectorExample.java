package sample;
/*
  Copy Elements of ArrayList to Java Vector Example
  This java example shows how to copy elements of Java ArrayList to Java Vector using
  copy method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class CopyElementsOfArrayListToVectorExample {

  public static void main(String[] args) {
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("1");
    arrayList.add("4");
    arrayList.add("2");
    arrayList.add("5");
    arrayList.add("3");

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("D");
    v.add("E");
    v.add("F");
    v.add("G");
    v.add("H");

    /*
      To copy elements of Java ArrayList to Java Vector use,
      static void copy(List dstList, List sourceList) method of Collections class.

      This method copies all elements of source list to destination list. After copy
      index of the elements in both source and destination lists would be identical.

      The destination list must be long enough to hold all copied elements. If it is
      longer than that, the rest of the destination list's elments would remain
      unaffected.
    */

    System.out.println("Before copy, Vector Contains : " + v);

    //copy all elements of ArrayList to Vector using copy method of Collections class
    Collections.copy(v, arrayList);

    /*
       Please note that, If Vector is not long enough to hold all elements of
       ArrayList, it throws IndexOutOfBoundsException.
    */

    System.out.println("After Copy, Vector Contains : " + v);
  }
}

/*
 Output would be
 Before copy Vector Contains : [A, B, D, E, F, G, H]
 After Copy Vector Contains : [1, 4, 2, 5, 3, G, H]
 */
