package sample;
/*
  Iterate through a Collection using Java Iterator Example
  This Java Example shows how to iterate through a Collection using Java Iterator.
*/

import java.util.ArrayList;
import java.util.Iterator;

public class JavaIteratorExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList aList = new ArrayList();

    //populate ArrayList object
    aList.add("1");
    aList.add("2");
    aList.add("3");
    aList.add("4");
    aList.add("5");

    /*
      Get Iterator object by invoking iterator method of collection.
      Iterator provides hasNext() method which returns true if has more
      elements. next() method returns the element in iteration.
    */
    Iterator itr = aList.iterator();

    //iterate through the ArrayList values using Iterator's hasNext and next methods
    while (itr.hasNext()) System.out.println(itr.next());

    /*
      Please note that next method may throw a java.util.NoSuchElementException
      if iteration has no more elements.
    */

  }
}

/*
 Output would be
 1
 2
 3
 4
 5
 */
