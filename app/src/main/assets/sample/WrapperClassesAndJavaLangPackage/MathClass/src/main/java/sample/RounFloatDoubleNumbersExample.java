package sample;

/*
  Round Java float and double numbers using Math.round
  This java example shows how to round given float or double number using round
  method of Java Math class.
*/
public class RounFloatDoubleNumbersExample {

  public static void main(String[] args) {

    /*
     * To round float number, use
     * static int round(float f) method of Java Math class.
     *
     * It returns closest int number to the argument.
     * Internally, it adds 0.5 to the argument, takes floor value and casts
     * the result into int.
     *
     * i.e. result = (int) Math.floor( argument value + 0.5f )
     */

    //returns same value
    System.out.println(Math.round(10f));

    // returns (int) Math.floor(10.6) = 10
    System.out.println(Math.round(20.5f));

    //returns (int) Math.floor(20.5 + 0.5) = 30
    System.out.println(Math.round(20.5f));

    //returns (int) Math.floor(-18.9) = 19
    System.out.println(Math.round(-19.4f));

    //returns (int) Math.floor(-23) = -23
    System.out.println(Math.round(-23.5f));

    /*
     * To round double numbers, use
     * static long round(double d) method of Java Math class.
     * It returns long.
     */
  }
}

/*
 Output would be
 10
 21
 21
 -19
 -23
 */
