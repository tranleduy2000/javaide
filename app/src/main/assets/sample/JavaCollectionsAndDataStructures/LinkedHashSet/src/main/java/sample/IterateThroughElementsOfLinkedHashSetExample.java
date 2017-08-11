package sample;
/*
  Iterate through elements of Java LinkedHashSet example
  This Java Example shows how to iterate through elements Java LinkedHashSet object.
*/

import java.util.Iterator;
import java.util.LinkedHashSet;

public class IterateThroughElementsOfLinkedHashSetExample {

  public static void main(String[] args) {

    //create object of LinkedHashSet
    LinkedHashSet lhashSet = new LinkedHashSet();

    //add elements to LinkedHashSet object
    lhashSet.add(new Integer("1"));
    lhashSet.add(new Integer("2"));
    lhashSet.add(new Integer("3"));

    //get the Iterator
    Iterator itr = lhashSet.iterator();

    System.out.println("LinkedHashSet contains : ");
    while (itr.hasNext()) System.out.println(itr.next());
  }
}

/*
 Output would be
 LinkedHashSet contains :
 1
 2
 3
 */
