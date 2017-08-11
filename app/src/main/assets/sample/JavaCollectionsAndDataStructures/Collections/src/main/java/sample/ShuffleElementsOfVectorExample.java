package sample;
/*
  Shuffle elements of Java Vector example
  This java example shows how to shuffle elements of Java Vector object using
  shuffle method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class ShuffleElementsOfVectorExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add("1");
    v.add("2");
    v.add("3");
    v.add("4");
    v.add("5");

    System.out.println("Before shuffling, Vector contains : " + v);

    /*
      To shuffle elements of Java Vector use,
      static void shuffle(List list) method of Collections class.
    */

    Collections.shuffle(v);

    System.out.println("After shuffling, Vector contains : " + v);
  }
}

/*
 Output would be
 Before shuffling, Vector contains : [1, 2, 3, 4, 5]
 After shuffling, Vector contains : [4, 3, 2, 1, 5]
 */
