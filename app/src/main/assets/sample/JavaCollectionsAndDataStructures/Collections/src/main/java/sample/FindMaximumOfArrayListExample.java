package sample;
/*
  Find maxmimum element of Java ArrayList Example
  This java example shows how to find a maximum element of Java ArrayList using
  max method of Collections class.
*/

import java.util.ArrayList;
import java.util.Collections;

public class FindMaximumOfArrayListExample {

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
       To find maximum element of Java ArrayList use,
       static Object max(Collection c) method of Collections class.

       This method returns the maximum element of Java ArrayList according to
       its natural ordering.
    */

    Object obj = Collections.max(arrayList);

    System.out.println("Maximum Element of Java ArrayList is : " + obj);
  }
}

/*
 Output would be
 Maximum Element of Java ArrayList is : 802348
 */
