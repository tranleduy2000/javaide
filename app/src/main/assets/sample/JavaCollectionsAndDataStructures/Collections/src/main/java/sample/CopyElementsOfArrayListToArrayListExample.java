package sample;
/*
  Copy Elements of One Java ArrayList to Another Java ArrayList Example
  This java example shows how to copy all elements of one Java ArrayList object to
  another Java ArrayList object using copy method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class CopyElementsOfArrayListToArrayListExample {

  public static void main(String[] args) {

    //create first ArrayList object
    ArrayList arrayList1 = new ArrayList();

    //Add elements to ArrayList
    arrayList1.add("1");
    arrayList1.add("2");
    arrayList1.add("3");

    //create another ArrayList object
    ArrayList arrayList2 = new ArrayList();

    //Add elements to Arraylist
    arrayList2.add("One");
    arrayList2.add("Two");
    arrayList2.add("Three");
    arrayList2.add("Four");
    arrayList2.add("Five");

    /*
      To copy elements of one Java ArrayList to another use,
      static void copy(List dstList, List sourceList) method of Collections class.

      This method copies all elements of source list to destination list. After copy
      index of the elements in both source and destination lists would be identical.

      The destination list must be long enough to hold all copied elements. If it is
      longer than that, the rest of the destination list's elments would remain
      unaffected.
    */

    System.out.println("Before copy, Second ArrayList Contains : " + arrayList2);

    //copy all elements of ArrayList to another ArrayList using copy
    //method of Collections class
    Collections.copy(arrayList2, arrayList1);

    /*
      Please note that, If destination ArrayList object is not long
      enough to hold all elements of source ArrayList,
      it throws IndexOutOfBoundsException.
    */

    System.out.println("After copy, Second ArrayList Contains : " + arrayList2);
  }
}

/*
 Output would be
 Before copy, Second ArrayList Contains : [One, Two, Three, Four, Five]
 After copy, Second ArrayList Contains : [1, 2, 3, Four, Five]
 */
