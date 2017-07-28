package sample;
/*
  Get Synchronized Map from Java TreeMap example
  This java example shows how to get a synchronized Map from Java TreeMap using
  synchronizedMap method of Collections class.
*/

import java.util.TreeMap;
import java.util.Collections;
import java.util.Map;

public class GetSynchronizedMapFromTreeMapExample {

    public static void main(String[] args) {
        //create TreeMap object
        TreeMap treeMap = new TreeMap();
   
    /*
      Java TreeMap is NOT synchronized. To get synchronized Map from
      TreeMap use
      static void synchronizedMap(Map map) method of Collections class.
    */

        Map map = Collections.synchronizedMap(treeMap);
   
    /*
       Use this map object to prevent any unsynchronized access to original
       TreeMap object.
    */

    }
}