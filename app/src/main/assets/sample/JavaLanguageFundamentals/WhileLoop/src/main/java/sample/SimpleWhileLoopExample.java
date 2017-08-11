package sample;
/*
  While loop Example
  This Java Example shows how to use while loop to iterate in Java program.
*/

public class SimpleWhileLoopExample {

  public static void main(String[] args) {

    /*
     * Syntax of while loop is
     *
     * while( <condition> )
     *   <loop body>
     *
     * where <condition> is a boolean expression. Loop body is executed as long
     * as condition is true.
     *
     * Loop body may contain more than one statments. In that case it should be
     * enclosed in a block.
     */

    int i = 0;

    while (i < 5) {
      System.out.println("i is : " + i);
      i++;
    }

    /*
     * The following code will create an infinite loop, since j < 5 will always
     * evaluated to true
     */
    //int j = 0;
    //while(j < 5)
    //  System.out.println("j is : " + j);

  }
}

/*
 Output would be
 i is : 0
 i is : 1
 i is : 2
 i is : 3
 i is : 4
 */
