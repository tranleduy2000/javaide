package sample;
/*
  Java Float isNaN method example
  This example shows how to use isNaN() method of the Float class.
*/

public class JavaFloatIsNaNExample {

  public static void main(String[] args) {

    /*
     boolean isNaN(float f) static method checks whether the agrument
     primitive float value is Not-a-Number value or not.
     It return true if the specified argument is Not-a-Number value, false otherwise.
    */
    float f = (float) Math.sqrt(-10);
    boolean b1 = Float.isNaN(f);
    System.out.println(b1);

    /*
     boolean isNaN() instance method checks whether the Float object is
     Not-a-Number value or not.
     It return true if the object is Not-a-Number value, false otherwise.
    */
    Float fObj = new Float(f);
    boolean b2 = fObj.isNaN();
    System.out.println(b2);
  }
}

/*
 Output would be
 true
 true
 */
