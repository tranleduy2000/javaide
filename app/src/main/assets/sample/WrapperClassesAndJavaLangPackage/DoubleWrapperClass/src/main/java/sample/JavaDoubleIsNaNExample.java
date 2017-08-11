package sample;
/*
  Java Double isNaN method example
  This example shows how to use isNaN() method of the Double class.
*/

public class JavaDoubleIsNaNExample {

  public static void main(String[] args) {

    /*
     boolean isNaN(double f) static method checks whether the agrument
     primitive double value is Not-a-Number value or not.
     It return true if the specified argument is Not-a-Number value, false otherwise.
    */
    double d = Math.sqrt(-10);
    boolean b1 = Double.isNaN(d);
    System.out.println(b1);

    /*
     boolean isNaN() instance method checks whether the Double object is
     Not-a-Number value or not.
     It return true if the object is Not-a-Number value, false otherwise.
    */
    Double dObj = new Double(d);
    boolean b2 = dObj.isNaN();
    System.out.println(b2);
  }
}

/*
 Output would be
 true
 true
 */
