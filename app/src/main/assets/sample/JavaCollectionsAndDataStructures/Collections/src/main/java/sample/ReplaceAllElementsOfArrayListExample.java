package sample;
/*
  Replace All Elements Of Java ArrayList Example
  This java example shows how to replace all elements of Java ArrayList object with
  the specified element using fill method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class ReplaceAllElementsOfArrayListExample {

  public static void main(String[] args) {
    //create a ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to ArrayList
    arrayList.add("A");
    arrayList.add("B");
    arrayList.add("D");

    /*
      To replace all elements of Java ArrayList with specified element use,
      static void fill(List list, Object element) method of Collections class.
    */

    System.out.println("Before replacement, ArrayList contains : " + arrayList);

    Collections.fill(arrayList, "REPLACED");

    System.out.println("After replacement, ArrayList contains : " + arrayList);
  }
}

/*
 Output would be
 Before replacement, ArrayList contains : [A, B, D]
 After replacement, ArrayList contains : [REPLACED, REPLACED, REPLACED]
 */
