package sample;
/*
  Remove value from Java HashMap example
  This Java Example shows how to remove a key value pair from HashMap object using
  remove method.
*/

import java.util.HashMap;

public class RemoveValueFromHashMapExample {

  public static void main(String[] args) {

    //create HashMap object
    HashMap hMap = new HashMap();

    //add key value pairs to HashMap
    hMap.put("1", "One");
    hMap.put("2", "Two");
    hMap.put("3", "Three");

    /*
      To remove a key value pair from HashMap use
      Object remove(Object key) method of HashMap class.
      It returns either the value mapped with the key or null if no value
      was mapped.
    */

    Object obj = hMap.remove("2");
    System.out.println(obj + " Removed from HashMap");
  }
}
/*
Output would be
Two Removed from HashMap
*/
