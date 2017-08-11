package sample;
/*
  Swap elements of Java ArrayList example
  This java example shows how to swap elements of Java ArrayList object using
  swap method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class SwapElementsOfArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("A");
    arrayList.add("B");
    arrayList.add("C");
    arrayList.add("D");
    arrayList.add("E");

    System.out.println("Before swaping, ArrayList contains : " + arrayList);

    /*
      To swap elements of Java ArrayList use,
      static void swap(List list, int firstElement, int secondElement)
      method of Collections class. Where firstElement is the index of first
      element to be swapped and secondElement is the index of the second element
      to be swapped.

      If the specified positions are equal, list remains unchanged.

      Please note that, this method can throw IndexOutOfBoundsException if
      any of the index values is not in range.
    */

    Collections.swap(arrayList, 0, 4);

    System.out.println("After swaping, ArrayList contains : " + arrayList);
  }
}

/*
 Output would be
 Before swaping, ArrayList contains : [A, B, C, D, E]
 After swaping, ArrayList contains : [E, B, C, D, A]
 */
