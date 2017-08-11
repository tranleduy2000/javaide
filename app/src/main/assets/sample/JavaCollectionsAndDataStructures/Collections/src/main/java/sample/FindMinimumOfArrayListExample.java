package sample;
/*
  Find Minimum element of Java ArrayList Example
  This java example shows how to find a minimum element of Java ArrayList using
  min method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class FindMinimumOfArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add(new Integer("327482"));
    arrayList.add(new Integer("13408"));
    arrayList.add(new Integer("802348"));
    arrayList.add(new Integer("345308"));
    arrayList.add(new Integer("509324"));

    /*
       To find minimum element of Java ArrayList use,
       static Object min(Collection c) method of Collections class.

       This method returns the minimum element of Java ArrayList according to
       its natural ordering.
    */

    Object obj = Collections.min(arrayList);

    System.out.println("Minimum Element of Java ArrayList is : " + obj);
  }
}
/*
Output would be
Minimum Element of Java ArrayList is : 13408
*/
