package sample;
/*
        Switch Statement Example
        This example shows how to use switch statement in a Java program.
        Switch statement is a better replacement if multiple if else if
        statements.
*/

public class SwitchStatementExample {

  public static void main(String[] args) {

    /*
     * Syntax of switch statement is
     *
     * switch(expression){
     *
     *      case value1:
     *              //statements
     *              break;
     *
     * case value2:
     *              //statements
     *              break;
     *
     * ....
     *
     * default:
     *              //statements
     *              break;
     *
     * }
     *
     * here, expression must be of type int, short, byte or char.
     * values should be constants literal values and can not be
     * duplicated.
     *
     * Flow of switch statement is as below.
     * Expression value is compared with each case value. If it
     * matches, statements following case would be executed.
     * break statement is used to terminate the execution of
     * statements.
     *
     * If none of the case matches, statements following default
     * would be executed.
     *
     * If break statement is not used within case, all cases following
     * matching cases would be executed.
     *
     */

    for (int i = 0; i <= 3; i++) {
      switch (i) {
        case 0:
          System.out.println("i is 0");
          break;

        case 1:
          System.out.println("i is 1");
          break;

        case 2:
          System.out.println("i is 2");
          break;

        default:
          System.out.println("i is grater than 2");
      }
    }
  }
}

/*
 Output would be
 i is 0
 i is 1
 i is 2
 i is grater than 2
 */
