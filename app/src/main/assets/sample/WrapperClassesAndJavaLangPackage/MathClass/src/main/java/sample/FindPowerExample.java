package sample;
/*
  Find power using Math.pow
  This java example shows how to find a power using pow method of Java Math class.
*/

public class FindPowerExample {

  public static void main(String[] args) {

    /*
     * To find a value raised to power of another value, use
     * static double pow(double d1, double d2) method of Java Math class.
     */

    //returns 2 raised to 2, i.e. 4
    System.out.println(Math.pow(2, 2));

    //returns -3 raised to 2, i.e. 9
    System.out.println(Math.pow(-3, 2));
  }
}

/*
 Output would be
 4.0
 9.0
 */
