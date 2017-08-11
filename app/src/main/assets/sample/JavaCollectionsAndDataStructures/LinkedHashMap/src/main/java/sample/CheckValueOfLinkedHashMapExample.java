package sample;
/*
  Check if a particular value exists in Java LinkedHashMap example
  This Java Example shows how to check if LinkedHashMap object contains a particular
  value using containsValue method of LinkedHashMap class.
*/

import java.util.LinkedHashMap;

public class CheckValueOfLinkedHashMapExample {

  public static void main(String[] args) {

    //create LinkedHashMap object
    LinkedHashMap lHashMap = new LinkedHashMap();

    //add key value pairs to LinkedHashMap
    lHashMap.put("1", "One");
    lHashMap.put("2", "Two");
    lHashMap.put("3", "Three");

    /*
      To check whether a particular value exists in LinkedHashMap use
      boolean containsValue(Object key) method of LinkedHashMap class.
      It returns true if the value is mapped to one or more keys in the
      LinkedHashMap otherwise false.
    */

    boolean blnExists = lHashMap.containsValue("Two");
    System.out.println("Two exists in LinkedHashMap ? : " + blnExists);
  }
}

/*
 Output would be
 Two exists in LinkedHashMap ? : true
 */
