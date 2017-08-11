package sample;

/*
  Java break statement example.
  This example shows how to use java break statement to terminate the loop.
*/
public class JavaBreakExample {

  public static void main(String[] args) {
    /*
     * break statement is used to terminate the loop in java. The following example
     * breaks the loop if the array element is equal to true.
     *
     * After break statement is executed, the control goes to the statement
     * immediately after the loop containing break statement.
     */

    int intArray[] = new int[]{1, 2, 3, 4, 5};

    System.out.println("Elements less than 3 are : ");
    for (int i = 0; i < intArray.length; i++) {
      if (intArray[i] == 3) break;
      else System.out.println(intArray[i]);
    }
  }
}

/*
 Output would be
 Elements less than 3 are :
 1
 2
 */
