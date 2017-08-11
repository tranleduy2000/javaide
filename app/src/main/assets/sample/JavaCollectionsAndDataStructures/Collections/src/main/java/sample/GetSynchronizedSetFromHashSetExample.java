package sample;
/*
  Get Synchronized Set from Java HashSet example
  This java example shows how to get a synchronized Set from Java HashSet using
  synchronizedSet method of Collections class.
*/

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GetSynchronizedSetFromHashSetExample {

  public static void main(String[] args) {
    //create HashSet object
    HashSet hashSet = new HashSet();

    /*
      Java HashSet is NOT synchronized. To get synchronized Set from
      HashSet use
      static void synchronizedSet(Set set) method of Collections class.
    */

    Set set = Collections.synchronizedSet(hashSet);

    /*
       Use this set object to prevent any unsynchronized access to original
       HashSet object.
    */

  }
}
