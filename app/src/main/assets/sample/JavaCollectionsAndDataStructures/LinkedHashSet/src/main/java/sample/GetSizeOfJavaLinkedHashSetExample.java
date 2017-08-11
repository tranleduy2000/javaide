package sample;
/*
  Get Size of Java LinkedHashSet Example
  This Java Example shows how to get the size or nubmer of elements stored in
  Java LinkedHashSet object using size method.
*/

import java.util.LinkedHashSet;

public class GetSizeOfJavaLinkedHashSetExample {

  public static void main(String[] args) {

    //create LinkedHashSet object
    LinkedHashSet lhashSet = new LinkedHashSet();

    /*
      To get the size of LinkedHashSet use
      int size() method of LinkedHashSet class. It returns the number of elements
      stored in LinkedHashSet object.
    */
    System.out.println("Size of LinkedHashSet : " + lhashSet.size());

    //add elements to LinkedHashSet object
    lhashSet.add(new Integer("1"));
    lhashSet.add(new Integer("2"));
    lhashSet.add(new Integer("3"));

    System.out.println("Size of LinkedHashSet after addition : " + lhashSet.size());

    //remove one element from LinkedHashSet using remove method
    lhashSet.remove(new Integer("1"));
    System.out.println("Size of LinkedHashSet after removal : " + lhashSet.size());
  }
}

/*
 Output would be
 Size of LinkedHashSet : 0
 Size of LinkedHashSet after addition : 3
 Size of LinkedHashSet after removal : 2
 */
