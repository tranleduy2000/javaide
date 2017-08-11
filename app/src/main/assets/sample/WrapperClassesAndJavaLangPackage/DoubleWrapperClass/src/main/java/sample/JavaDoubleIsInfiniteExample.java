package sample;
/*
  Java Double isInfinite example
  This example shows how to use isInfinite() method of the Double class.
*/

public class JavaDoubleIsInfiniteExample {

  public static void main(String[] args) {

    /*
     boolean isInfinite(double) static method checks whether the agrument primitive
     double value is infinite number or not.
     It return true if the specified argument is infinite value, false otherwise.
    */
    double d = (double) 4 / 0;
    boolean b1 = Double.isInfinite(d);
    System.out.println(b1);

    /*
     boolean isInfinite() instance method checks whether the Double object
     is infinite number or not.
     It return true if the object is infinite value, false otherwise.
    */
    Double dObj = new Double(d);
    boolean b2 = dObj.isInfinite();
    System.out.println(b2);
  }
}

/*
 Output would be
 true
 true
 */
