package sample;
/*
  Get Size of Java HashMap Example
  This Java Example shows how to get the size or nubmer of key value pairs
  stored in HashMap using size method.
*/

import java.util.HashMap;

public class GetSizeOfHashMapExample {

  public static void main(String[] args) {

    //create HashMap object
    HashMap hMap = new HashMap();

    /*
      To get the size of HashMap use
      int size() method of HashMap class. It returns the number of key value
      pairs stored in HashMap object.
    */
    System.out.println("Size of HashMap : " + hMap.size());

    //add key value pairs to HashMap using put method
    hMap.put("1", "One");
    hMap.put("2", "Two");
    hMap.put("3", "Three");
    System.out.println("Size of HashMap after addition : " + hMap.size());

    //remove one element from HashMap using remove method
    Object obj = hMap.remove("2");
    System.out.println("Size of HashMap after removal : " + hMap.size());
  }
}

/*
 Output would be
 Size of HashMap : 0
 Size of HashMap after addition : 3
 Size of HashMap after removal : 2
 */
