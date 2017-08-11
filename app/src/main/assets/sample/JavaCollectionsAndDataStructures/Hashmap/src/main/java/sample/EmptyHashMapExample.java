package sample;
/*
  Remove all values from Java HashMap example
  This Java Example shows how to remove all values from HashMap object or empty
  HashMap or clear HashMap using clear method.
*/

import java.util.HashMap;

public class EmptyHashMapExample {

  public static void main(String[] args) {

    //create HashMap object
    HashMap hMap = new HashMap();

    //add key value pairs to HashMap
    hMap.put("1", "One");
    hMap.put("2", "Two");
    hMap.put("3", "Three");

    /*
      To remove all values or clear HashMap use
      void clear method() of HashMap class. Clear method removes all
      key value pairs contained in HashMap.
    */

    hMap.clear();

    System.out.println("Total key value pairs in HashMap are : " + hMap.size());
  }
}

/*
 Output would be
 Total key value pairs in HashMap are : 0
 */
