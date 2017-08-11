package sample;
/*
  Iterate through the values of Java LinkedHashMap example
  This Java Example shows how to iterate through the values contained in the
  LinkedHashMap object.
*/

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class IterateValuesOfLinkedHashMapExample {

  public static void main(String[] args) {

    //create LinkedHashMap object
    LinkedHashMap lHashMap = new LinkedHashMap();

    //add key value pairs to LinkedHashMap
    lHashMap.put("1", "One");
    lHashMap.put("2", "Two");
    lHashMap.put("3", "Three");

    /*
      get Collection of values contained in LinkedHashMap using
      Collection values() method of LinkedHashMap class
    */
    Collection c = lHashMap.values();

    //obtain an Iterator for Collection
    Iterator itr = c.iterator();

    //iterate through LinkedHashMap values iterator
    while (itr.hasNext()) System.out.println(itr.next());
  }
}

/*
 Output would be
 One
 Two
 Three
 */
