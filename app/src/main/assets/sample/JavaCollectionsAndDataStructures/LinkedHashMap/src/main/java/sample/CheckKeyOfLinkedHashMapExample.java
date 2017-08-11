package sample;
/*
  Check if a particular key exists in Java LinkedHashMap example
  This Java Example shows how to check if LinkedHashMap object contains a particular
  key using containsKey method of LinkedHashMap class.
*/

import java.util.LinkedHashMap;

public class CheckKeyOfLinkedHashMapExample {

  public static void main(String[] args) {

    //create LinkedHashMap object
    LinkedHashMap lHashMap = new LinkedHashMap();

    //add key value pairs to LinkedHashMap
    lHashMap.put("1", "One");
    lHashMap.put("2", "Two");
    lHashMap.put("3", "Three");

    /*
      To check whether a particular key exists in LinkedHashMap use
      boolean containsKey(Object key) method of LinkedHashMap class.
      It returns true if the LinkedHashMap contains mapping for specified key
      otherwise false.
    */

    boolean blnExists = lHashMap.containsKey("3");
    System.out.println("3 exists in LinkedHashMap ? : " + blnExists);
  }
}

/*
 Output would be
 3 exists in LinkedHashMap ? : true
 */
