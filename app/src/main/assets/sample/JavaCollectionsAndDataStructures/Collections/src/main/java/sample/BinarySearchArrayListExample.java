package sample;
/*
  Perform Binary Search on Java ArrayList Example
  This java example shows how to search an element of Java ArrayList
  using binarySearch method of Collections class. binarySearch method uses
  binary search algorithm to search an element.
*/

import java.util.ArrayList;
import java.util.Collections;

public class BinarySearchArrayListExample {

  public static void main(String[] args) {
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("1");
    arrayList.add("4");
    arrayList.add("2");
    arrayList.add("5");
    arrayList.add("3");

    /*
      To Search an element of Java ArrayList using binary search algorithm use,
      static int binarySearch(List list, Object element) method of Collections class.

      This method returns the index of the value to be searched, if found in the
      ArrayList.
      Otherwise it returns (- (X) - 1)
      where X is the index where the the search value would be inserted.
      i.e. index of first element that is grater than the search value
      or ArrayList.size(), if all elements of an ArrayList are less
      than the search value.

      Please note that the ArrayList MUST BE SORTED before it can be searched
      using binarySearch method.
    */

    //First sort an ArrayList using sort method of Collections class
    Collections.sort(arrayList);
    System.out.println("Sorted ArrayList contains : " + arrayList);

    //search an element using binarySearch method of Collections class
    int index = Collections.binarySearch(arrayList, "4");

    System.out.println("Element found at : " + index);
  }
}

/*
 Output would be
 Sorted ArrayList contains : [1, 2, 3, 4, 5]
 Element found at : 3
 */
