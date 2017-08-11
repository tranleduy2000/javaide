package sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
       Java String Array To List Example
       This Java String Array To List Example shows how to convert Java String array to java.util.List object
       using Arrays.asList method.
*/
public class JavaStringArrayToListExample {

  public static void main(String args[]) {

    //create String array
    String[] numbers = new String[]{"one", "two", "three"};

    /*
     * To covert String array to java.util.List object, use
     * List asList(String[] strArray) method of Arrays class.
     */

    List list = (List) Arrays.asList(numbers);

    //display elements of List
    System.out.println("String array converted to List");
    for (int i = 0; i < list.size(); i++) {
      System.out.println(list.get(i));
    }

    /*
     * Please note that list object created this way can not be modified.
     * Any attempt to call add or delete method would throw UnsupportedOperationException exception.
     *
     * If you want modifiable list object, then use
     *
     * ArrayList list = (ArrayList) Arrays.asList(numbers);
     */

    /* Alternate Method to covert String array to List */
    List anotherList = new ArrayList();

    Collections.addAll(anotherList, numbers);
  }
}

/*
 Output of this example would be
 String array converted to List
 one
 two
 three
 */
