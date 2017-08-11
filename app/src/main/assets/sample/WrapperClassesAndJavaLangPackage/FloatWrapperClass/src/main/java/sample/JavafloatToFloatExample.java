package sample;
/*
  Convert float to Float object Example
  This example shows how a float primitive value can be converted to a Float object
*/

public class JavafloatToFloatExample {

  public static void main(String[] args) {
    float f = 10.56f;

    /*
    Use Float constructor to convert float primitive type to a Float object.
    */

    Float fObj = new Float(f);
    System.out.println(fObj);
  }
}

/*
 Output of the program would be
 10.56
 */
