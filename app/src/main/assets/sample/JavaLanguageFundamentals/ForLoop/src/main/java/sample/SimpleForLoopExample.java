package sample;
/*
  Simple For loop Example
  This Java Example shows how to use for loop to iterate in Java program.
*/

public class SimpleForLoopExample {

  public static void main(String[] args) {

    /* Syntax of for loop is
     *
     * for(<initialization> ; <condition> ; <expression> )
     *   <loop body>
     *
     * where initialization usually declares a loop variable, condition is a
     * boolean expression such that if the condition is true, loop body will be
     * executed and after each iteration of loop body, expression is executed which
     * usually increase or decrease loop variable.
     *
     * Initialization is executed only once.
     */

    for (int index = 0; index < 5; index++) System.out.println("Index is : " + index);

    /*
     * Loop body may contains more than one statement. In that case they should
     * be in the block.
     */

    for (int index = 0; index < 5; index++) {
      System.out.println("Index is : " + index);
      index++;
    }

    /*
     * Please note that in above loop, index is a local variable whose scope
     * is limited to the loop. It can not be referenced from outside the loop.
     */
  }
}

/*
 Output would be
 Index is : 0
 Index is : 1
 Index is : 2
 Index is : 3
 Index is : 4
 Index is : 0
 Index is : 2
 Index is : 4
 */
