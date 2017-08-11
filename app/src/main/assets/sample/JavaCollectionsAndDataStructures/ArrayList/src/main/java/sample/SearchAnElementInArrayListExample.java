package sample;
/*
  Search an element of Java ArrayList Example
  This Java Example shows how to search an element of java
  ArrayList object using contains, indexOf and lastIndexOf methods.
*/

import java.util.ArrayList;

public class SearchAnElementInArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("1");
    arrayList.add("2");
    arrayList.add("3");
    arrayList.add("4");
    arrayList.add("5");
    arrayList.add("1");
    arrayList.add("2");

    /*
      To check whether the specified element exists in Java ArrayList use
      boolean contains(Object element) method.
      It returns true if the ArrayList contains the specified objct, false
      otherwise.
    */

    boolean blnFound = arrayList.contains("2");
    System.out.println("Does arrayList contain 2 ? " + blnFound);

    /*
      To get an index of specified element in ArrayList use
      int indexOf(Object element) method.
      This method returns the index of the specified element in ArrayList.
      It returns -1 if not found.
    */

    int index = arrayList.indexOf("4");
    if (index == -1) System.out.println("ArrayList does not contain 4");
    else System.out.println("ArrayList contains 4 at index :" + index);

    /*
      To get last index of specified element in ArrayList use
      int lastIndexOf(Object element) method.
      This method returns index of the last occurrence of the
      specified element in ArrayList. It returns -1 if not found.
    */

    int lastIndex = arrayList.lastIndexOf("1");
    if (lastIndex == -1) System.out.println("ArrayList does not contain 1");
    else System.out.println("Last occurrence of 1 in ArrayList is at index :" + lastIndex);
  }
}
/*
Output would be
Does arrayList contain 2 ? true
ArrayList contains 4 at index :3
Last occurrence of 1 in ArrayList is at index :5
*/
