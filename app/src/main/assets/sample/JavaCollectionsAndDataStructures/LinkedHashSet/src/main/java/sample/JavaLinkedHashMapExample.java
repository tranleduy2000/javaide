package sample;
/*
  Simple Java LinkedHashMap example
  This simple Java Example shows how to use Java LinkedHashMap.
  It also describes how to add something to LinkedHashMap and how to
  retrieve the value added from LinkedHashMap.
*/

import java.util.LinkedHashMap;

public class JavaLinkedHashMapExample {

  public static void main(String[] args) {

    //create object of LinkedHashMap
    LinkedHashMap lHashMap = new LinkedHashMap();

    /*
      Add key value pair to LinkedHashMap using
      Object put(Object key, Object value) method of Java LinkedHashMap class,
      where key and value both are objects
      put method returns Object which is either the value previously tied
      to the key or null if no value mapped to the key.
    */

    lHashMap.put("One", new Integer(1));
    lHashMap.put("Two", new Integer(2));

    /*
      Please note that put method accepts Objects. Java Primitive values CAN NOT
      be added directly to LinkedHashMap. It must be converted to corrosponding
      wrapper class first.
    */

    //retrieve value using Object get(Object key) method of Java LinkedHashMap class
    Object obj = lHashMap.get("One");
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
