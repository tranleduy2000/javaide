package sample;
/*
  Replace all occurrences of specified element of Java ArrayList Example
  This java example shows how to replace all occurrences of a specified element
  of Java ArrayList using replaceAll method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class ReplaceAllArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("A");
    arrayList.add("B");
    arrayList.add("A");
    arrayList.add("C");
    arrayList.add("D");

    System.out.println("ArrayList Contains : " + arrayList);

    /*
      To replace all occurrences of specified element of Java ArrayList use,
      static boolean replaceAll(List list, Object oldVal, Object newVal) method
      of Collections class.

      This method returns true if the list contained one more elements replaced.

    */

    Collections.replaceAll(arrayList, "A", "Replace All");

    System.out.println("After Replace All, ArrayList Contains : " + arrayList);
  }
}

/*
 Output would be
 ArrayList Contains : [A, B, A, C, D]
 After Replace All, ArrayList Contains : [Replace All, B, Replace All, C, D]
 */
