package sample;
/*
        Increment and Decrement Operators Example
        This example shows how to use Java increment operator (++) and decrement
        (--) operator.
*/

public class IncrementDecrementOperatorExample {

  public static void main(String[] args) {

    /*
     * Java increment operator ++ increases its operand's value by one
     * while decrement operator -- decreases its operand's value by
     * one as given below.
     */

    int i = 10;
    int j = 10;

    i++;
    j++;

    System.out.println("i = " + i);
    System.out.println("j = " + j);

    /*
     * Increment and decrement operators can be used in two ways,
     * postfix (as given in above example) and prefix.
     *
     * In normal use, both form behaves the same way. However, when they
     * are part of expression, there is difference between these
     * two forms.
     *
     * If prefix form is used, operand is incremented or decremented
     * before substituting its value.
     *
     * On the other hand, if postfix form is used,
     * operand's old value is used to evaluate the expression.
     *
     * Simple example would be,
     */

    /*
     * Here, value of i would be assigned to k and then its
     * incremented by one.
     */
    int k = i++;

    /*
     * Here, value of j would be incremented first and then
     * assigned to k.
     */
    int l = ++j;

    System.out.println("k = " + k);
    System.out.println("l = " + l);
  }
}

/*
 Output would be
 i = 11
 j = 11
 k = 11
 l = 12
 */
