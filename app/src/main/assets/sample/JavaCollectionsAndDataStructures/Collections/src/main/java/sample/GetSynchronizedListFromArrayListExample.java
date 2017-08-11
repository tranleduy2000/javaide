package sample;
/*
  Get Synchronized List from Java ArrayList example
  This java example shows how to get a synchronized list from Java ArrayList using
  synchronizedList method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetSynchronizedListFromArrayListExample {

  public static void main(String[] args) {
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    /*
      Java ArrayList is NOT synchronized. To get synchronized list from
      ArrayList use
      static void synchronizedList(List list) method of Collections class.
    */

    List list = Collections.synchronizedList(arrayList);

    /*
       Use this list object to prevent any unsynchronized access to original
       ArrayList object.
    */

  }
}
