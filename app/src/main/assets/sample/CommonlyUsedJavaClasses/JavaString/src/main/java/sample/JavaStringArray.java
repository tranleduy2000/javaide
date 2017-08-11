package sample;
/*
       Java String Array Example.
       This Java String Array example how to create and use String array or array of Strings in Java.

*/

public class JavaStringArray {
  public static void main(String args[]) {

    /*
     * Java String array can be created in below given ways.
     */

    /*
     * Declare and initialize String array in single statement as given below.
     * This method is particularly useful when we are dealing with very small size array.
     */
    String[] myFirstStringArray = new String[]{"String 1", "String 2", "String 3"};

    /*
     * Declaration and assignment can be done separately as given below.
     */

    //first declare String array
    String[] mySecondStringArray = new String[3];

    //Observe that giving size is mandatory here. While there was no size given in the first method.

    //Now Assign individual String array elements
    mySecondStringArray[0] = "String 1";
    mySecondStringArray[1] = "String 2";
    mySecondStringArray[2] = "String 3";

    //Note that, like every other arrays, String array starts with index 0 and not index 1.

    /*
     * Retrieve values from String Array:            *
     * String array elements can be retrieved by directly accessing using index. You may also iterate
     * String array using loop.
     */

    //this will retrieve second element of first String array
    System.out.println(myFirstStringArray[1]);

    //iterate the String array using loop
    for (int i = 0; i < mySecondStringArray.length; i++) {
      System.out.println(mySecondStringArray[i]);
    }
  }
}

/*
 Output of the above Java String Array Example program would be
 String 2
 String 1
 String 2
 String 3
 */
