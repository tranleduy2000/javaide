package sample;
/*
  Convert int to Integer object Example
  This example shows how a int primitive value can be converted to Integer object
*/

public class IntToIntegerExample {

  public static void main(String[] args) {
    int i = 10;

    /*
    Use Integer constructor to convert int primitive type to Integer object.
    */

    Integer intObj = new Integer(i);
    System.out.println(intObj);
  }
}

/*
 Output of the program would be
 10
 */
