package sample;
/*
  Check if a particular element exists in Java LinkedHashSet Example
  This Java Example shows how to check whether an elements is contained in Java
  LinkedHashSet using contains method.
*/

import java.util.LinkedHashSet;

public class CheckElementLinkedHashSetExample {

  public static void main(String[] args) {

    //create object of LinkedHashSet
    LinkedHashSet lhashSet = new LinkedHashSet();

    //add elements to LinkedHashSet object
    lhashSet.add(new Integer("1"));
    lhashSet.add(new Integer("2"));
    lhashSet.add(new Integer("3"));

    /*
      To check whether a particular value exists in LinkedHashSet use
      boolean contains(Object value) method of LinkedHashSet class.
      It returns true if the LinkedHashSet contains the value, otherwise false.
    */

    boolean blnExists = lhashSet.contains(new Integer("3"));
    System.out.println("3 exists in LinkedHashSet ? : " + blnExists);
  }
}

/*
 Output would be
 3 exists in LinkedHashSet ? : true
 */
