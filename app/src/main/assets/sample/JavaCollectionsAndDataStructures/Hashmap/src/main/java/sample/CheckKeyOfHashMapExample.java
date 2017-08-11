package sample;
/*
  Check if a particular key exists in Java HashMap example
  This Java Example shows how to check if HashMap object contains a particular
  key using containsKey method of HashMap class.
*/

import java.util.HashMap;

public class CheckKeyOfHashMapExample {

  public static void main(String[] args) {

    //create HashMap object
    HashMap hMap = new HashMap();

    //add key value pairs to HashMap
    hMap.put("1", "One");
    hMap.put("2", "Two");
    hMap.put("3", "Three");

    /*
      To check whether a particular key exists in HashMap use
      boolean containsKey(Object key) method of HashMap class.
      It returns true if the HashMap contains mapping for specified key
      otherwise false.
    */

    boolean blnExists = hMap.containsKey("3");
    System.out.println("3 exists in HashMap ? : " + blnExists);
  }
}

/*
 Output would be
 3 exists in HashMap ? : true
 */
