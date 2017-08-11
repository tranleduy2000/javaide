package sample;
/*
  Find Minimum element of Java HashSet Example
  This java example shows how to find a minimum element of Java HashSet using
  min method of Collections class.
*/

import java.util.Collections;
import java.util.HashSet;

public class FindMinimumOfHashSetExample {

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
       To find minimum element of Java HashSet use,
       static Object min(Collection c) method of Collections class.

       This method returns the minimum element of Java HashSet according to
       its natural ordering.
    */

    Object obj = Collections.min(hashSet);

    System.out.println("Minimum Element of Java HashSet is : " + obj);
  }
}
/*
Output would be
Minimum Element of Java HashSet is : 923740927
*/
