package sample;
/*
  Insert all elements of other Collection to Specified Index of Java
  ArrayList Example
  This Java Example shows how to insert all elements of other Collection object
  at specified index of Java ArrayList object using addAll method.
*/

import java.util.ArrayList;
import java.util.Vector;

public class InsertAllElementsOfOtherCollectionToArrayListExample {

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
      To insert all elements of another Collection to sepcified index of ArrayList
      use
      boolean addAll(int index, Collection c) method.
      It returns true if the ArrayList was changed by the method call.
    */

    //insert all elements of Vector to ArrayList at index 1
    arrayList.addAll(1, v);

    //display elements of ArrayList
    System.out.println("After inserting all elements of Vector at index 1,
            ArrayList contains.. ");
    for (int i = 0; i < arrayList.size(); i++)
      System.out.println(arrayList.get(i));

  }
}
 
/*
Output would be
After inserting all elements of Vector at index 1, ArrayList contains..
1
4
5
2
3
*/