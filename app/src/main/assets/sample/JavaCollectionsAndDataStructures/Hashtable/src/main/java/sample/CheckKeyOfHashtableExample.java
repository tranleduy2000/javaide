package sample;
/*
  Check if a particular key exists in Java Hashtable example
  This Java Example shows how to check if Hashtable object contains a particular
  key using containsKey method of Hashtable class.
*/

import java.util.Hashtable;

public class CheckKeyOfHashtableExample {

  public static void main(String[] args) {

    //create Hashtable object
    Hashtable ht = new Hashtable();

    //add key value pairs to Hashtable
    ht.put("1", "One");
    ht.put("2", "Two");
    ht.put("3", "Three");

    /*
      To check whether a particular key exists in Hashtable use
      boolean containsKey(Object key) method of Hashtable class.
      It returns true if the Hashtable contains mapping for specified key
      otherwise false.
    */

    boolean blnExists = ht.containsKey("2");
    System.out.println("2 exists in Hashtable ? : " + blnExists);
  }
}

/*
 Output would be
 2 exists in Hashtable ? : true
 */
