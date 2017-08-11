package sample;
/*
  Get Enumeration over Java HashSet Example
  This java example shows how to get Enumeration over Java HashSet using enumeration
  method of Collections class. This example also shows how to enumerate through
  elements of Java HashSet.
*/

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

public class GetEnumerationOverHashSetExample {

  public static void main(String[] args) {
    //create a HashSet object
    HashSet hashSet = new HashSet();

    //Add elements to HashSet
    hashSet.add("A");
    hashSet.add("B");
    hashSet.add("D");
    hashSet.add("E");
    hashSet.add("F");

    /*
       Get Enumeration over Java HashSet object using,
       static Enumeration enumeration(Collection c) method of Collections class.

       This method returns the enumeration object over the specified Collection.

    */

    //get the Enumeration object
    Enumeration e = Collections.enumeration(hashSet);

    //enumerate through the HashSet elements
    System.out.println("Enumerating through Java HashSet");
    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}

/*
 Output would be
 Enumerating through Java HashSet
 A
 B
 D
 E
 F
 */
