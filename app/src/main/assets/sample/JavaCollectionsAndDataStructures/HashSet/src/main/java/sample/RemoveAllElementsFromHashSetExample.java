package sample;
/*
  Remove all elements from Java HashSet example
  This Java Example shows how to remove all elements contained in Java HashSet
  or clear HashSet object using clear or removeAll methods. It also shows how
  to check whether HashSet object is empty or not using isEmpty method.
*/

import java.util.HashSet;

public class RemoveAllElementsFromHashSetExample {

  public static void main(String[] args) {

    //create object of HashSet
    HashSet hSet = new HashSet();

    //add elements to HashSet object
    hSet.add(new Integer("1"));
    hSet.add(new Integer("2"));
    hSet.add(new Integer("3"));

    System.out.println("HashSet before removal : " + hSet);

    /*
      To remove all elements from Java HashSet or to clear HashSet object use,
      void clear() method.
      This method removes all elements from HashSet.
    */

    hSet.clear();
    System.out.println("HashSet after removal : " + hSet);

    /*
      To check whether HashSet contains any elements or not use
      boolean isEmpty() method.
      This method returns true if the HashSet does not contains any elements
      otherwise false.
    */

    System.out.println("Is HashSet empty ? " + hSet.isEmpty());

    /*
      Please note that removeAll method of Java HashSet class can also be
      used to remove all elements from HashSet object.
    */
  }
}

/*
 Output would be
 HashSet before removal : [3, 2, 1]
 HashSet after removal : []
 Is HashSet empty ? true
 */
