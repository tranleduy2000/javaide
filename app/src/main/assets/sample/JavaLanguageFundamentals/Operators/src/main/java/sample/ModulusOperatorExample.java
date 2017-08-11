package sample;
/*
        Modulus Operator Example
        This example shows how to use Java modulus operator (%).
*/

public class ModulusOperatorExample {

  public static void main(String[] args) {

    System.out.println("Java Modulus Operator example");

    /*
     * Modulus operator returns reminder of the devision of
     * floating point or integer types.
     */

    int i = 50;
    double d = 32;

    System.out.println("i mod 10 = " + i % 10);
    System.out.println("d mod 10 = " + d % 10);
  }
}

/*
 Output would be
 Java Modulus Operator example
 i mod 10 = 0
 d mod 10 = 2.0
 */
