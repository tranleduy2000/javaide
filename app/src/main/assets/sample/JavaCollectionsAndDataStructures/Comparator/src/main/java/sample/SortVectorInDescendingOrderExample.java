package sample;
/*
  Sort Java Vector in descending order using comparator example
  This java example shows how to sort elements of Java Vector in descending order
  using comparator and reverseOrder method of Collections class.
*/

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class SortVectorInDescendingOrderExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("1");
    v.add("2");
    v.add("3");
    v.add("4");
    v.add("5");

    /*
      To get comparator that imposes reverse order on a Collection use
      static Comparator reverseOrder() method of Collections class
    */

    Comparator comparator = Collections.reverseOrder();

    System.out.println("Before sorting Vector in descending order : " + v);

    /*
      To sort an Vector using comparator use,
      static void sort(List list, Comparator c) method of Collections class.
    */

    Collections.sort(v, comparator);
    System.out.println("After sorting Vector in descending order : " + v);
  }
}

/*
 Output would be
 Before sorting Vector in descending order : [1, 2, 3, 4, 5]
 After sorting Vector in descending order : [5, 4, 3, 2, 1]
 */
