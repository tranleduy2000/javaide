package sample;
/*
  Iterate through values of Java Hashtable example
  This Java Example shows how to iterate through the values contained in the
  Hashtable object.
*/

import java.util.Enumeration;
import java.util.Hashtable;

public class IterateValuesOfHashtableExample {

  public static void main(String[] args) {

    //create Hashtable object
    Hashtable ht = new Hashtable();

    //add key value pairs to Hashtable
    ht.put("1", "One");
    ht.put("2", "Two");
    ht.put("3", "Three");

    /*
      get Enumeration of values contained in Hashtable using
      Enumeration elements() method of Hashtable class
    */
    Enumeration e = ht.elements();

    //iterate through Hashtable values Enumeration
    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}

/*
 Output would be
 Three
 Two
 One
 */
