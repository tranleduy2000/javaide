package sample;
/*
  Java Float compare example
  This example shows how to compare a Float object with other Float object, Float
  with an Object, or two float primitive values using methods provided by
  java.lang.Float class.
*/

public class JavaFloatCompareExample {

  public static void main(String[] args) {

    /*
    To compare two float primitive values use
    compare(float f1, float f2) method of Float class. This is a static method.
    It returns 0 if both the values are equal, returns value less than 0 if
    f1 is less than f2, and returns value grater than 0 if f2 is grater than f2.
    */
    float f1 = 5.35f;
    float f2 = 5.34f;
    int i1 = Float.compare(f1, f2);

    if (i1 > 0) {
      System.out.println("First is grater");
    } else if (i1 < 0) {
      System.out.println("Second is grater");
    } else {
      System.out.println("Both are equal");
    }

    /*
    To compare a Float object with another Float object use
    int compareTo(Float f) method.
    It returns 0 if both the values are equal, returns value less than 0 if
    this Float object is less than the argument, and returns value grater
    than 0 if this Float object is grater than f2.
    */
    Float fObj1 = new Float("5.35");
    Float fObj2 = new Float("5.34");
    int i2 = fObj1.compareTo(fObj2);

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
