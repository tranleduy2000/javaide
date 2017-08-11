package sample;
/*
  Find maximum of two numbers using Math.max
  This java example shows how to find maximum of two int, float,
  double or long numbers using max method of Java Math class.
*/

public class FindMaxOfTwoNumbersExample {

  public static void main(String[] args) {

    /*
     * To find maximum of two int values, use
     * static int max(int a, int b) method of Math class.
     */

    System.out.println(Math.max(20, 40));

    /*
     * To find minimum of two float values, use
     * static float max(float f1, float f2) method of Math class.
     */
    System.out.println(Math.max(324.34f, 432.324f));

    /*
     * To find maximum of two double values, use
     * static double max(double d2, double d2) method of Math class.
     */
    System.out.println(Math.max(65.34, 123.45));

    /*
     * To find maximum of two long values, use
     * static long max(long l1, long l2) method of Math class.
     */

    System.out.println(Math.max(435l, 523l));
  }
}

/*
 Output would be
 40
 432.324
 123.45
 523
 */
