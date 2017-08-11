package sample;
/*
  Simple Java HashMap example
  This simple Java Example shows how to use Java HashMap. It also describes how to
  add something to HashMap and how to retrieve the value added from HashMap.
*/

import java.util.HashMap;

public class JavaHashMapExample {

  public static void main(String[] args) {

    //create object of HashMap
    HashMap hMap = new HashMap();

    /*
      Add key value pair to HashMap using
      Object put(Object key, Object value) method of Java HashMap class,
      where key and value both are objects
      put method returns Object which is either the value previously tied
      to the key or null if no value mapped to the key.
    */

    hMap.put("One", new Integer(1));
    hMap.put("Two", new Integer(2));

    /*
      Please note that put method accepts Objects. Java Primitive values CAN NOT
      be added directly to HashMap. It must be converted to corrosponding
      wrapper class first.
    */

    //retrieve value using Object get(Object key) method of Java HashMap class
    Object obj = hMap.get("One");
    System.out.println(obj);

    /*
      Please note that the return type of get method is an Object. The value must
      be casted to the original class.
    */

  }
}
/*
Output of the program would be
1
*/
