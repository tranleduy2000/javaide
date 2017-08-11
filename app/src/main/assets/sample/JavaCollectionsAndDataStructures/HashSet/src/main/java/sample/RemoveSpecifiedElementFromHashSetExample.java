package sample;
/*
  Remove specified element from Java HashSet example
  This Java Example shows how to remove a specified object or element
  contained in Java HashSet object using remove method.
*/

import java.util.HashSet;

public class RemoveSpecifiedElementFromHashSetExample {

  public static void main(String[] args) {

    //create object of HashSet
    HashSet hSet = new HashSet();

    //add elements to HashSet object
    hSet.add(new Integer("1"));
    hSet.add(new Integer("2"));
    hSet.add(new Integer("3"));

    System.out.println("HashSet before removal : " + hSet);

    /*
      To remove an element from Java HashSet object use,
      boolean remove(Object o) method.
      This method removes an element from HashSet if it is present and returns
      true. Otherwise remove method returns false.
    */

    boolean blnRemoved = hSet.remove(new Integer("2"));
    System.out.println("Was 2 removed from HashSet ? " + blnRemoved);

    System.out.println("HashSet after removal : " + hSet);
  }
}

/*
 Output would be
 HashSet before removal : [3, 2, 1]
 Was 2 removed from HashSet ? true
 HashSet after removal : [3, 1]
 */
