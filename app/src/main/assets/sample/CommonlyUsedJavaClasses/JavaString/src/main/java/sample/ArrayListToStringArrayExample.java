package sample;
/*
       Java ArrayList to String Array Example
       This Java ArrayList to String Array example shows how to convert ArrayList to String array
       in Java.
*/

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayListToStringArrayExample {

  public static void main(String args[]) {

    //ArrayList containing string objects
    ArrayList<String> aListDays = new ArrayList<String>();
    aListDays.add("Sunday");
    aListDays.add("Monday");
    aListDays.add("Tuesday");

    /*
     * To convert ArrayList containing String elements to String array, use
     * Object[] toArray() method of ArrayList class.
     *
     * Please note that toArray method returns Object array, not String array.
     */

    //First Step: convert ArrayList to an Object array.
    Object[] objDays = aListDays.toArray();

    //Second Step: convert Object array to String array
    String[] strDays = Arrays.copyOf(objDays, objDays.length, String[].class);

    System.out.println("ArrayList converted to String array");

    //print elements of String array
    for (int i = 0; i < strDays.length; i++) {
      System.out.println(strDays[i]);
    }
  }
}

/*
 Output of above given ArrayList to String Array example would be
 ArrayList converted to String array
 Sunday
 Monday
 Tuesday
 */
