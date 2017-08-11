package sample;
/*
  Get Synchronized Map from Java HashMap example
  This java example shows how to get a synchronized Map from Java HashMap using
  synchronizedMap method of Collections class.
*/

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetSynchronizedMapFromHashMapExample {

  public static void main(String[] args) {
    //create HashMap object
    HashMap hashMap = new HashMap();

    /*
      Java HashMap is NOT synchronized. To get synchronized Map from
      HashMap use
      static void synchronizedMap(Map map) method of Collections class.
    */

    Map map = Collections.synchronizedMap(hashMap);

    /*
       Use this map object to prevent any unsynchronized access to original
       HashMap object.
    */

  }
}
