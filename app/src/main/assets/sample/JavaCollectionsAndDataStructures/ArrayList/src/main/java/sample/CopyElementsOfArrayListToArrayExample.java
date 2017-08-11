package sample;
/*
  Copy all elements of Java ArrayList to an Object Array Example
  This Java Example shows how to copy all elements of Java ArrayList object to an
  array of Objects using toArray method.
*/

import java.util.ArrayList;

public class CopyElementsOfArrayListToArrayExample {

  public static void main(String[] args) {
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to ArrayList
    arrayList.add("1");
    arrayList.add("2");
    arrayList.add("3");
    arrayList.add("4");
    arrayList.add("5");
   
    /*
      To copy all elements of java ArrayList object into array use
      Object[] toArray() method.
    */

    Object[] objArray = arrayList.toArray();

    //display contents of Object array
    System.out.println("ArrayList elements are copied into an Array.
            Now Array Contains.. ");
    for (int index = 0; index < objArray.length; index++)
      System.out.println(objArray[index]);
  }
}
 
/*
Output would be
ArrayList elements are copied into an Array. Now Array Contains..
1
2
3
4
5
*/