package sample;
/*
  Remove all elements from Java ArrayList Example
  This Java Example shows how to remove all elements from java ArrayList object
  using clear method.
*/

import java.util.ArrayList;

public class RemoveAllElementsOfArrayListExample {

  public static void main(String[] args) {
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("1");
    arrayList.add("2");
    arrayList.add("3");

    System.out.println("Size of ArrayList before removing elements : " + arrayList.size());
    /*
      To remove all elements from the ArrayList use
      void clear() method.
    */
    arrayList.clear();
    System.out.println("Size of ArrayList after removing elements : " + arrayList.size());
  }
}
/*
Output would be
Size of ArrayList before removing elements : 3
Size of ArrayList after removing elements : 0
*/
