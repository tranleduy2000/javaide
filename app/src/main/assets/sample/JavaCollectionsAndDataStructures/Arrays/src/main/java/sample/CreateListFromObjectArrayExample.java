package sample;
/*
  Create List from Java Object Array Example
  This java example shows how to create a List from an array of type Object using
  asList method of Arrays class.
*/

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CreateListFromObjectArrayExample {

  public static void main(String[] args) {
    //create an array of type Object, in this case we will create String array
    String[] strArray = new String[]{"Object", "Array", "Converted", "To", "List"};

    /*
      To create List from an array of type Object use,
      static List asList(Object[] objArray) method of Arrays class.

      This method returns a fixed sized list backed by original array.
    */

    List list = Arrays.asList(strArray);

    //get an iterator
    Iterator itr = list.iterator();

    //iterate through list created from Array
    System.out.println("List created from an Array of type Object contains,");
    while (itr.hasNext()) System.out.println(itr.next());
  }
}

/*
 Output would be
 List created from an Array of type Object contains,
 Object
 Array
 Converted
 To
 List
 */
