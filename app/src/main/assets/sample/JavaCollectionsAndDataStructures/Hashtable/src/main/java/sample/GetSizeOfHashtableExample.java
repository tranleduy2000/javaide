package sample;
/*
  Get Size of Java Hashtable Example
  This Java Example shows how to get the size or nubmer of key value pairs
  stored in Hashtable using size method.
*/

import java.util.Hashtable;

public class GetSizeOfHashtableExample {

  public static void main(String[] args) {

    //create Hashtable object
    Hashtable ht = new Hashtable();

    /*
      To get the size of Hashtable use
      int size() method of Hashtable class. It returns the number of key values
      pairs stored in Hashtable object.
    */
    System.out.println("Size of Hashtable : " + ht.size());

    //add key value pairs to Hashtable using put method
    ht.put("1", "One");
    ht.put("2", "Two");
    ht.put("3", "Three");
    System.out.println("Size of Hashtable after addition : " + ht.size());

    //remove one element from Hashtable using remove method
    Object obj = ht.remove("2");
    System.out.println("Size of Hashtable after removal : " + ht.size());
  }
}

/*
 Output would be
 Size of Hashtable : 0
 Size of Hashtable after addition : 3
 Size of Hashtable after removal : 2
 */
