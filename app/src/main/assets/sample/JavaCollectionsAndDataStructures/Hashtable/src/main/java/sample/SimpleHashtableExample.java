package sample;
/*
  Simple Java Hashtable example
  This simple Java Example shows how to use Java Hashtable. It also describes how to
  add something to Hashtable and how to retrieve the value added from Hashtable.
*/

import java.util.Hashtable;

public class SimpleHashtableExample {

  public static void main(String[] args) {
    //create object of Hashtable
    Hashtable ht = new Hashtable();

    /*
      Add key value pair to Hashtable using
      Object put(Object key, Object value) method of Java Hashtable class,
      where key and value both are objects
      and can not be null.
      put method returns Object which is either the value previously tied
      to the key or null if no value mapped to the key.
    */

    ht.put("One", new Integer(1));
    ht.put("Two", new Integer(2));

    /*
      Please note that put method accepts Objects. Java Primitive values CAN NOT
      be added directly to Hashtable. It must be converted to corrosponding
      wrapper class first.
    */

    //retrieve value using Object get(Object key) method of Java Hashtable class
    Object obj = ht.get("One");
    System.out.println(obj);

    /*
       Please note that the return type of get method is Object. The value must
       be casted to the original class.
    */
  }
}

/*
 Output of the program would be
 1
 */
