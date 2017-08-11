package sample;
/*
  Convert long primitive to Long object Example
  This example shows how a long primitive value can be converted to Long object
*/

public class longToLongExample {

  public static void main(String[] args) {
    long i = 10;

    /*
    Use Long constructor to convert long primitive type to Long object.
    */

    Long lObj = new Long(i);
    System.out.println(lObj);
  }
}

/*
 Output of the program would be
 10
 */
