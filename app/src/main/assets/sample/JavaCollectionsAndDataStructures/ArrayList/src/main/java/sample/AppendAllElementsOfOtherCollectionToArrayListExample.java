package sample;
/*
  Append all elements of other Collection to Java ArrayList Example
  This Java Example shows how to append all elements of other Collection object
  at the end of Java ArrayList object using addAll method. This program shows
  how to append all elements of Java Vector to Java ArrayList object.
*/

import java.util.ArrayList;
import java.util.Vector;

public class AppendAllElementsOfOtherCollectionToArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("1");
    arrayList.add("2");
    arrayList.add("3");

    //create a new Vector object
    Vector v = new Vector();
    v.add("4");
    v.add("5");
   
    /*
      To append all elements of another Collection to ArrayList use
      boolean addAll(Collection c) method.
      It returns true if the ArrayList was changed by the method call.
    */

    //append all elements of Vector to ArrayList
    arrayList.addAll(v);

    //display elements of ArrayList
    System.out.println("After appending all elements of Vector,
            ArrayList contains.. ");
    for (int i = 0; i < arrayList.size(); i++)
      System.out.println(arrayList.get(i));

  }
}
 
/*
Output would be
After appending all elements of Vector, ArrayList contains..
1
2
3
4
5
*/