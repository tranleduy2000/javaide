package sample;
/*
  Convert double to Double object Example
  This example shows how a double primitive value can be converted to a Double object
*/

public class JavadoubleToDoubleExample {

  public static void main(String[] args) {
    double d = 10.56;

    /*
    Use Double constructor to convert double primitive type to a Double object.
    */

    Double dObj = new Double(d);
    System.out.println(dObj);
  }
}

/*
 Output of the program would be
 10.56
 */
