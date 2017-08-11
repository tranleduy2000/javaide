package sample;
/*
  Remove value from Java Hashtable example
  This Java Example shows how to remove a key value pair from Hashtable object using
  remove method.
*/

import java.util.Enumeration;
import java.util.Hashtable;

public class RemoveValueFromHashtableExample {

  public static void main(String[] args) {

    //create Hashtable object
    Hashtable ht = new Hashtable();

    //add key value pairs to Hashtable
    ht.put("1", "One");
    ht.put("2", "Two");
    ht.put("3", "Three");

    /*
      To remove a key value pair from Hashtable use
      Object remove(Object key) method of Hashtable class.
      It returns either the value mapped with the key or null if no value
      was mapped.
    */

    Object obj = ht.remove("2");
    System.out.println(obj + " Removed from Hashtable");

    //print remaining Hashtable values
    Enumeration e = ht.elements();

    //iterate through Hashtable values Enumeration
    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}

/*
 Output would be
 Two Removed from Hashtable
 Three
 One
 */
