package sample;
/*
  Simple Java LinkedHashSet example
  This simple Java Example shows how to use Java LinkedHashSet.
  It also describes how to add something to LinkedHashSet object
  using add method.
*/

import java.util.LinkedHashSet;

public class SimpleLinkedHashSetExample {

  public static void main(String[] args) {
    //create object of LinkedHashSet
    LinkedHashSet lhashSet = new LinkedHashSet();

    /*
      Add an Object to LinkedHashSet using
      boolean add(Object obj) method of Java LinkedHashSet class.
      This method adds an element to LinkedHashSet if it is not
      already present in LinkedHashSet.
      It returns true if the element was added to LinkedHashSet, false otherwise.
    */

    lhashSet.add(new Integer("1"));
    lhashSet.add(new Integer("2"));
    lhashSet.add(new Integer("3"));

    /*
      Please note that add method accepts Objects. Java Primitive values CAN NOT
      be added directly to LinkedHashSet. It must be converted to corrosponding
      wrapper class first.
    */

    System.out.println("LinkedHashSet contains.." + lhashSet);
  }
}

/*
 Output of the program would be
 LinkedHashSet contains..[1, 2, 3]
 */
