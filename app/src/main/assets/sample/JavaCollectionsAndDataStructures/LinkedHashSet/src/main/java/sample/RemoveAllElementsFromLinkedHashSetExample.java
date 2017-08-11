package sample;
/*
  Remove all elements from Java LinkedHashSet example
  This Java Example shows how to remove all elements contained in Java LinkedHashSet
  or clear LinkedHashSet object using clear or removeAll methods. It also shows how
  to check whether LinkedHashSet object is empty or not using isEmpty method.
*/

import java.util.LinkedHashSet;

public class RemoveAllElementsFromLinkedHashSetExample {

  public static void main(String[] args) {

    //create object of LinkedHashSet
    LinkedHashSet lhashSet = new LinkedHashSet();

    //add elements to LinkedHashSet object
    lhashSet.add(new Integer("1"));
    lhashSet.add(new Integer("2"));
    lhashSet.add(new Integer("3"));

    System.out.println("LinkedHashSet before removal : " + lhashSet);

    /*
      To remove all elements from Java LinkedHashSet or to clear LinkedHashSet
      object use,
      void clear() method.
      This method removes all elements from LinkedHashSet.
    */

    lhashSet.clear();
    System.out.println("LinkedHashSet after removal : " + lhashSet);

    /*
      To check whether LinkedHashSet contains any elements or not
      use
      boolean isEmpty() method.
      This method returns true if the LinkedHashSet does not contains any elements
      otherwise false.
    */

    System.out.println("Is LinkedHashSet empty ? " + lhashSet.isEmpty());

    /*
      Please note that removeAll method of Java LinkedHashSet class can
      also be used to remove all elements from LinkedHashSet object.
    */
  }
}

/*
 Output would be
 LinkedHashSet before removal : [1, 2, 3]
 LinkedHashSet after removal : []
 Is LinkedHashSet empty ? true
 */
