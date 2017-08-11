package sample;
/*
  Check if a particular value exists in Java HashMap example
  This Java Example shows how to check if HashMap object contains a particular
  value using containsValue method of HashMap class.
*/

import java.util.HashMap;

public class CheckValueOfHashMapExample {

  public static void main(String[] args) {

    //create HashMap object
    HashMap hMap = new HashMap();

    //add key value pairs to HashMap
    hMap.put("1", "One");
    hMap.put("2", "Two");
    hMap.put("3", "Three");

    /*
      To check whether a particular value exists in HashMap use
      boolean containsValue(Object key) method of HashMap class.
      It returns true if the value is mapped to one or more keys in the
      HashMap otherwise false.
    */

    boolean blnExists = hMap.containsValue("Two");
    System.out.println("Two exists in HashMap ? : " + blnExists);
  }
}

/*
 Output would be
 Two exists in HashMap ? : true
 */
