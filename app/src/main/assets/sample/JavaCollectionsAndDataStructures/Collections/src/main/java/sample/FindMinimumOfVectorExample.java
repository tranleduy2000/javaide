package sample;
/*
  Find Minimum element of Java Vector Example
  This java example shows how to find a minimum element of Java Vector using
  min method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class FindMinimumOfVectorExample {

  public static void main(String[] args) {

    //create a Vector object
    Vector v = new Vector();

    //Add elements to Vector
    v.add(new Double("324.4324"));
    v.add(new Double("345.3532"));
    v.add(new Double("342.342"));
    v.add(new Double("357.349"));
    v.add(new Double("23.32453"));

    /*
       To find minimum element of Java Vector use,
       static Object min(Collection c) method of Collections class.

       This method returns the minimum element of Java Vector according to
       its natural ordering.
    */

    Object obj = Collections.min(v);

    System.out.println("Minimum Element of Java Vector is : " + obj);
  }
}
/*
Output would be
Minimum Element of Java Vector is : 23.32453
*/
