package sample;
/*
  Java Float isInfinite example
  This example shows how to use isInfinite() method of the Float class.
*/

public class JavaFloatIsInfiniteExample {

  public static void main(String[] args) {

    /*
     boolean isInfinite(float) static method checks whether the agrument primitive
     float value is infinite number or not.
     It return true if the specified argument is infinite value, false otherwise.
    */
    float f = (float) 1 / 0;
    boolean b1 = Float.isInfinite(f);
    System.out.println(b1);

    /*
     boolean isInfinite() instance method checks whether the Float object
     is infinite number or not.
     It return true if the object is infinite value, false otherwise.
    */
    Float fObj = new Float(f);
    boolean b2 = fObj.isInfinite();
    System.out.println(b2);
  }
}

/*
 Output would be
 true
 true
 */
