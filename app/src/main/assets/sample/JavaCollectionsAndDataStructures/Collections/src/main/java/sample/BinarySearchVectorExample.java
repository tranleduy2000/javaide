package sample;
/*
  Perform Binary Search on Java Vector Example
  This java example shows how to search an element of Java Vector using
  binarySearch method of Collections class. binarySearch method uses binary
  search algorithm to search an element.
*/

import java.util.Collections;
import java.util.Vector;

public class BinarySearchVectorExample {

  public static void main(String[] args) {
    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("A");
    v.add("B");
    v.add("D");
    v.add("E");
    v.add("F");

    /*
      To Search an element of Java Vector using binary search algorithm use,
      static int binarySearch(List list, Object element) method of Collections class.

      This method returns the index of the value to be searched, if found in the
      Vector.
      Otherwise it returns (- (X) - 1)
      where X is the index where the the search value would be inserted.
      i.e. index of first element that is grater than the search value
      or Vector.size(), if all elements of an Vector are less than the search value.

      Please note that the Vector MUST BE SORTED before it can be searched
      using binarySearch method.
    */

    //First sort Vector using sort method of Collections class
    Collections.sort(v);
    System.out.println("Sorted Vector contains : " + v);

    //search an element using binarySearch method of Collections class
    int index = Collections.binarySearch(v, "D");

    System.out.println("Element found at : " + index);
  }
}

/*
 Output would be
 Sorted Vector contains : [A, B, D, E, F]
 Element found at : 2
 */
