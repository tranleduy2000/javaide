package sample;
/*
  Convert short primitive to Short object Example
  This example shows how a short primitive value can be converted to Short object
*/

public class shortToShortExample {

  public static void main(String[] args) {
    short s = 10;

    /*
    Use Short constructor to convert short primitive type to Short object.
    */

    Short sObj = new Short(s);
    System.out.println(sObj);
  }
}

/*
 Output of the program would be
 10
 */
