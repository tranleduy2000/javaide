package sample;
/*
  Get Collection of Values from Java Hashtable example
  This Java Example shows how to get a Collection of values contained in Hashtable
  using values method of Java Hashtable class.
*/

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class GetCollectionOfValuesFromHashtableExample {

  public static void main(String[] args) {

    //create Hashtable object
    Hashtable ht = new Hashtable();

    //add key value pairs to Hashtable
    ht.put("1", "One");
    ht.put("2", "Two");
    ht.put("3", "Three");

    /*
      get Collection of values contained in Hashtable using
      Collection values() method of Hashtable class
    */

    Collection c = ht.values();

    System.out.println("Values of Collection created from Hashtable are :");
    //iterate through the collection
    Iterator itr = c.iterator();
    while (itr.hasNext()) System.out.println(itr.next());

    /*
       Please note that resultant Collection object is backed by the Hashtable.
       Any value that is removed from Collection will also be removed from
       original Hashtable object. The same is not the case with the element
       addition.
    */

    //remove One from collection
    c.remove("One");

    //print values of original values of Hashtable
    System.out.println("Hashtable values after removal from Collection are :");
    Enumeration e = ht.elements();
    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}

/*
 Output would be
 Values of Collection created from Hashtable are :
 Three
 Two
 One
 Hashtable values after removal from Collection are :
 Three
 Two
 */
