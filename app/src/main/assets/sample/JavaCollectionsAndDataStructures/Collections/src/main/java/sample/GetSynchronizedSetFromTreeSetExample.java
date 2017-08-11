package sample;
/*
  Get Synchronized Set from Java TreeSet example
  This java example shows how to get a synchronized Set from Java TreeSet using
  synchronizedSet method of Collections class.
*/

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class GetSynchronizedSetFromTreeSetExample {

  public static void main(String[] args) {
    //create TreeSet object
    TreeSet treeSet = new TreeSet();

    /*
      Java TreeSet is NOT synchronized. To get synchronized Set from
      TreeSet use
      static void synchronizedSet(Set set) method of Collections class.
    */

    Set set = Collections.synchronizedSet(treeSet);

    /*
       Use this set object to prevent any unsynchronized access to original
       TreeSet object.
    */

  }
}
