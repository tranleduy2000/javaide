package sample;
/*
  Find absolute value of float, int, double and long using Math.abs
  This java example shows how to find absolute value of any double,
  float, long or java int value using abs method of Math class.
*/

public class FindAbsoluteValueExample {

  public static void main(String[] args) {

    int i = 8;
    int j = -5;

    /*
     * To find absolute value of int, use
     * static int abs(int i) method.
     *
     * It returns the same value if the agrument is non negative value, otherwise
     * negation of the negative value.
     *
     */

    System.out.println("Absolute value of " + i + " is :" + Math.abs(i));
    System.out.println("Absolute value of " + j + " is :" + Math.abs(j));

    float f1 = 10.40f;
    float f2 = -50.28f;

    /*
     * To find absolute value of float, use
     * static float abs(float f) method.
     *
     * It returns the same value if the agrument is non negative value, otherwise
     * negation of the negative value.
     *
     */
    System.out.println("Absolute value of " + f1 + " is :" + Math.abs(f1));
    System.out.println("Absolute value of " + f2 + " is :" + Math.abs(f2));

    double d1 = 43.324;
    double d2 = -349.324;
    /*
     * To find absolute value of double, use
     * static double abs(double d) method.
     *
     * It returns the same value if the agrument is non negative value, otherwise
     * negation of the negative value.
     *
     */
    System.out.println("Absolute value of " + d1 + " is :" + Math.abs(d1));
    System.out.println("Absolute value of " + d2 + " is :" + Math.abs(d2));

    long l1 = 34;
    long l2 = -439;
    /*
     * To find absolute value of long, use
     * static long abs(long l) method.
     *
     * It returns the same value if the agrument is non negative value, otherwise
     * negation of the negative value.
     *
     */
    System.out.println("Absolute value of " + l1 + " is :" + Math.abs(l1));
    System.out.println("Absolute value of " + l2 + " is :" + Math.abs(l2));
  }
}

/*
 Output would be
 Absolute value of 8 is :8
 Absolute value of -5 is :5
 Absolute value of 10.4 is :10.4
 Absolute value of -50.28 is :50.28
 Absolute value of 43.324 is :43.324
 Absolute value of -349.324 is :349.324
 Absolute value of 34 is :34
 Absolute value of -439 is :439
 */
