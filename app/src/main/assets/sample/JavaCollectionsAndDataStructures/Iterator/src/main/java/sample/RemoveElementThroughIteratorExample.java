package sample;
/*
  Remove an element from Collection using Java Iterator Example
  This Java Example shows how to remove an element from underlying Collection using
  Java Iterator's remove method.
*/

import java.util.ArrayList;
import java.util.Iterator;

public class RemoveElementThroughIteratorExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList aList = new ArrayList();

    //populate ArrayList object
    aList.add("1");
    aList.add("2");
    aList.add("3");
    aList.add("4");
    aList.add("5");

    System.out.println("ArrayList before removal : ");
    for (int i = 0; i < aList.size(); i++) System.out.println(aList.get(i));

    //get an Iterator
    Iterator itr = aList.iterator();

    //remove 2 from ArrayList using Iterator's remove method.
    String strElement = "";
    while (itr.hasNext()) {

      /*
        Iterator's next method returns an Object so we need to cast it into
        appropriate class before using it.
      */
      strElement = (String) itr.next();
      if (strElement.equals("2")) {
        /*
          Remove an element using remove() method of Iterator
          Remove method removes an element from underlying collection and
          it may throw a UnsupportedOperationException if the remove
          operation is not supported.
        */
        itr.remove();
        break;
      }
    }

    System.out.println("ArrayList after removal : ");
    for (int i = 0; i < aList.size(); i++) System.out.println(aList.get(i));
  }
}

/*
 Output would be
 ArrayList before removal :
 1
 2
 3
 4
 5
 ArrayList after removal :
 1
 3
 4
 5
 */
