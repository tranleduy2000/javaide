package sample;
/*
  Get Set view of Keys from Java Hashtable example
  This Java Example shows how to get a Set of keys contained in Hashtable
  using keySet method of Java Hashtable class.
*/

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class GetSetViewOfKeysFromHashtableExample {

  public static void main(String[] args) {

    //create Hashtable object
    Hashtable ht = new Hashtable();

    //add key value pairs to Hashtable
    ht.put("1", "One");
    ht.put("2", "Two");
    ht.put("3", "Three");

    /*
      get Set of keys contained in Hashtable using
      Set keySet() method of Hashtable class
    */

    Set st = ht.keySet();

    System.out.println("Set created from Hashtable Keys contains :");
    //iterate through the Set of keys
    Iterator itr = st.iterator();
    while (itr.hasNext()) System.out.println(itr.next());

    /*
       Please note that resultant Set object is backed by the Hashtable.
       Any key that is removed from Set will also be removed from
       original Hashtable object. The same is not the case with the element
       addition.
    */

    //remove 2 from Set
    st.remove("2");

    //print keys of original Hashtable
    System.out.println("Hashtable keys after removal from Set are :");
    Enumeration e = ht.keys();
    while (e.hasMoreElements()) System.out.println(e.nextElement());
  }
}

/*
 Output would be
 Set created from Hashtable Keys contains :
 3
 2
 1
 Hashtable keys after removal from Set are :
 3
 1
 */
