package sample;
/*
  Find maxmimum element of Java Vector Example
  This java example shows how to find a maximum element of Java Vector using
  max method of Collections class.
*/

import java.util.Collections;
import java.util.Vector;

public class FindMaximumOfVectorExample {

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
       To find maximum element of Java Vector use,
       static Object max(Collection c) method of Collections class.

       This method returns the maximum element of Java Vector according to
       its natural ordering.
    */

    Object obj = Collections.max(v);

    System.out.println("Maximum Element of Java Vector is : " + obj);
  }
}
/*
Output would be
Maximum Element of Java Vector is : 357.349
*/
