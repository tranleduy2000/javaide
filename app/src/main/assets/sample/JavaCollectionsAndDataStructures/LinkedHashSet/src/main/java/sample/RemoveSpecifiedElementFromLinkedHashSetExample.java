package sample;
/*
  Remove specified element from Java LinkedHashSet example
  This Java Example shows how to remove a specified object or element
  contained in Java LinkedHashSet object using remove method.
*/

import java.util.LinkedHashSet;

public class RemoveSpecifiedElementFromLinkedHashSetExample {

  public static void main(String[] args) {

    //create object of LinkedHashSet
    LinkedHashSet lhashSet = new LinkedHashSet();

    //add elements to LinkedHashSet object
    lhashSet.add(new Integer("1"));
    lhashSet.add(new Integer("2"));
    lhashSet.add(new Integer("3"));

    System.out.println("LinkedHashSet before removal : " + lhashSet);

    /*
      To remove an element from Java LinkedHashSet object use,
      boolean remove(Object o) method.
      This method removes an element from LinkedHashSet if it is present and returns
      true. Otherwise remove method returns false.
    */

    boolean blnRemoved = lhashSet.remove(new Integer("2"));
    System.out.println("Was 2 removed from LinkedHashSet ? " + blnRemoved);

    System.out.println("LinkedHashSet after removal : " + lhashSet);
  }
}

/*
 Output would be
 LinkedHashSet before removal : [1, 2, 3]
 Was 2 removed from LinkedHashSet ? true
 LinkedHashSet after removal : [1, 3]
 */
