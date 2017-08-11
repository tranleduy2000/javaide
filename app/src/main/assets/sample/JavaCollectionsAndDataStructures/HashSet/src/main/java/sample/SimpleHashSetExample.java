package sample;
/*
  Simple Java HashSet example
  This simple Java Example shows how to use Java HashSet. It also describes how to
  add something to HashSet object using add method.
*/

import java.util.HashSet;

public class SimpleHashSetExample {

  public static void main(String[] args) {
    //create object of HashSet
    HashSet hSet = new HashSet();

    /*
      Add an Object to HashSet using
      boolean add(Object obj) method of Java HashSet class.
      This method adds an element to HashSet if it is not already present in HashSet.
      It returns true if the element was added to HashSet, false otherwise.
    */

    hSet.add(new Integer("1"));
    hSet.add(new Integer("2"));
    hSet.add(new Integer("3"));

    /*
      Please note that add method accepts Objects. Java Primitive values CAN NOT
      be added directly to HashSet. It must be converted to corrosponding
      wrapper class first.
    */

    System.out.println("HashSet contains.." + hSet);
  }
}

/*
 Output of the program would be
 HashSet contains..[3, 2, 1]
 */
