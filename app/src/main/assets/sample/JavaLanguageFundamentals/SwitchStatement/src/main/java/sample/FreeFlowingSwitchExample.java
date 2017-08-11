package sample;

/*
        Free Flowing Switch Statement Example
        This example shows how case statements are executed if break is
        not used to terminate the execution of the statments.
*/
public class FreeFlowingSwitchExample {

  public static void main(String[] args) {

    /*
     * break statement is used to terminate the flow of
     * matching case statements. If break statement is
     * not specified, switch statement becomes free flowing and
     * all cases following matching case including default
     * would be executed.
     */

    int i = 0;

    switch (i) {
      case 0:
        System.out.println("i is 0");

      case 1:
        System.out.println("i is 1");

      case 2:
        System.out.println("i is 2");

      default:
        System.out.println("Free flowing switch example!");
    }
  }
}

/*
 Output would be
 i is 0
 i is 1
 i is 2
 Free flowing switch example!
 */
