package sample;
/*
  Remove an element from specified index of Java ArrayList Example
  This Java Example shows how to remove an element at specified index of java
  ArrayList object using remove method.
*/

import java.util.ArrayList;

public class RemoveElementFromArrayListExample {

  public static void main(String[] args) {
    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    //Add elements to Arraylist
    arrayList.add("1");
    arrayList.add("2");
    arrayList.add("3");

    /*
      To remove an element from the specified index of ArrayList use
      Object remove(int index) method.
      It returns the element that was removed from the ArrayList.
    */
    Object obj = arrayList.remove(1);
    System.out.println(obj + " is removed from ArrayList");

    System.out.println("ArrayList contains...");
    //display elements of ArrayList
    for (int index = 0; index < arrayList.size(); index++) System.out.println(arrayList.get(index));
  }
}

/*
 Output would be
 2 is removed from ArrayList
 ArrayList contains...
 1
 3
 */
