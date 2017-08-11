package sample;
/*
  Java ArrayList Example
  This Java ArrayList Example shows how to create an object of Java ArrayList. It also
  shows how to add elements to ArrayList and how get the same from ArrayList.
*/

import java.util.ArrayList;

public class SimpleArrayListExample {

  public static void main(String[] args) {

    //create an ArrayList object
    ArrayList arrayList = new ArrayList();

    /*
       Add elements to Arraylist using
       boolean add(Object o) method. It returns true as a general behavior
       of Collection.add method. The specified object is appended at the end
       of the ArrayList.
    */
    arrayList.add("1");
    arrayList.add("2");
    arrayList.add("3");

    /*
      Use get method of Java ArrayList class to display elements of ArrayList.
      Object get(int index) returns and element at the specified index in
      the ArrayList
    */
    System.out.println("Getting elements of ArrayList");
    System.out.println(arrayList.get(0));
    System.out.println(arrayList.get(1));
    System.out.println(arrayList.get(2));
  }
}

/*
 Output would be
 Getting elements of ArrayList
 1
 2
 3
 */
