package sample;
/*
  Find minimum of two numbers using Math.min
  This java example shows how to find minimum of two int, float,
  double or long numbers using min method of Java Math class.
*/

public class FindMinimumOfTwoNumbersExample {

  public static void main(String[] args) {

    /*
     * To find minimum of two int values, use
     * static int min(int a, int b) method of Math class.
     */

    System.out.println(Math.min(34, 45));

    /*
     * To find minimum of two float values, use
     * static float min(float f1, float f2) method of Math class.
     */
    System.out.println(Math.min(43.34f, 23.34f));

    /*
     * To find minimum of two double values, use
     * static double min(double d2, double d2) method of Math class.
     */
    System.out.println(Math.min(4324.334, 3987.342));

    /*
     * To find minimum of two long values, use
     * static long min(long l1, long l2) method of Math class.
     */

    System.out.println(Math.min(48092840, 4230843));
  }
}

/*
 Output would be
 34
 23.34
 3987.342
 4230843
 */
