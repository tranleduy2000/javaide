package sample;
/*
  If Else-If statement Example
  This Java Example shows how to use if else-if statement in Java program.
*/

public class IfElseIfElseExample {

  public static void main(String[] args) {

    /*
     * If Else-if statement is used to execute multiple of actions based upon
     * multiple conditions.
     * Sysntax of If Else-If statement is
     *
     * if(<condition1>)
     *   statement1
     * else if(<condition2>)
     *   statement2
     * ..
     * else
     *   statement3
     *
     * If <condition1> is true, statement1 will be executed, else if <condition2>
     * is true statement2 is executed and so on. If no condition is true, then else
     * statement will be executed.
     */

    int i = 10;

    if (i > 100) System.out.println("i is grater than 100");
    else if (i > 50) System.out.println("i is grater than 50");
    else System.out.println("i is less than 50");
  }
}

/*
 Output would be
 i is less than 50
 */
