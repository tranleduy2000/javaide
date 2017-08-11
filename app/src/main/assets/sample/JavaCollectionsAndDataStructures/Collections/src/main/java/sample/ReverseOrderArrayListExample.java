package sample;
/*
  Reverse order of all elements of Java ArrayList Example
  This java example shows how to reverse the order of all elements of Java ArrayList
  using reverse method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class ReverseOrderArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("A");
    arrayList.add("B");
    arrayList.add("C");
    arrayList.add("D");
    arrayList.add("E");

    System.out.println("Before Reverse Order, ArrayList Contains : " + arrayList);

    /*
      To reverse the order of all elements of Java ArrayList use,
      static void reverse(List list) method of Collections class.

      This method reverse the order of elements of specified list.
    */

    Collections.reverse(arrayList);

    System.out.println("After Reverse Order, ArrayList Contains : " + arrayList);
  }
}

/*
 Output would be
 Before Reverse Order, ArrayList Contains : [A, B, C, D, E]
 After Reverse Order, ArrayList Contains : [E, D, C, B, A]
 */
