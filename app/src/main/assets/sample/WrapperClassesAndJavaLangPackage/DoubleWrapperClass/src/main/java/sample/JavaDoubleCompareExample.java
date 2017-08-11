package sample;
/*
  Java Double compare example
  This example shows how to compare a Double object with other Double object, Double
  with an Object, or two double primitive values using methods provided by
  java.lang.Double class.
*/

public class JavaDoubleCompareExample {

  public static void main(String[] args) {

    /*
    To compare two double primitive values use
    compare(double d1, double d2) method of Double class. This is a static method.
    It returns 0 if both the values are equal, returns value less than 0 if
    d1 is less than d2, and returns value grater than 0 if d1 is grater than d2.
    */
    double d1 = 5.35;
    double d2 = 5.34;
    int i1 = Double.compare(d1, d2);

    if (i1 > 0) {
      System.out.println("First is grater");
    } else if (i1 < 0) {
      System.out.println("Second is grater");
    } else {
      System.out.println("Both are equal");
    }

    /*
    To compare a Double object with another Double object use
    int compareTo(Double d) method.
    It returns 0 if both the values are equal, returns value less than 0 if
    this Double object is less than the argument, and returns value grater
    than 0 if this Double object is grater than the argument.
    */
    Double dObj1 = new Double("5.35");
    Double dObj2 = new Double("5.34");
    int i2 = dObj1.compareTo(dObj2);

    if (i2 > 0) {
      System.out.println("First is grater");
    } else if (i2 < 0) {
      System.out.println("Second is grater");
    } else {
      System.out.println("Both are equal");
    }
  }
}

/*
 Output would be
 First is grater
 First is grater
 */
