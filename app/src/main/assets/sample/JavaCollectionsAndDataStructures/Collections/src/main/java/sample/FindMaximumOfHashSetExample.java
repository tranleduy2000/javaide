package sample;
/*
  Find maxmimum element of Java HashSet Example
  This java example shows how to find a maximum element of Java HashSet using
  max method of Collections class.
*/

import java.util.Collections;
import java.util.HashSet;

public class FindMaximumOfHashSetExample {

  public static void main(String[] args) {

    //create a HashSet object
    HashSet hashSet = new HashSet();

    //Add elements to HashSet
    hashSet.add(new Long("923740927"));
    hashSet.add(new Long("4298748382"));
    hashSet.add(new Long("2374324832"));
    hashSet.add(new Long("2473483643"));
    hashSet.add(new Long("32987432984"));

    /*
       To find maximum element of Java HashSet use,
       static Object max(Collection c) method of Collections class.

       This method returns the maximum element of Java HashSet according to
       its natural ordering.
    */

    Object obj = Collections.max(hashSet);

    System.out.println("Maximum Element of Java HashSet is : " + obj);
  }
}
/*
Output would be
Maximum Element of Java HashSet is : 32987432984
*/
