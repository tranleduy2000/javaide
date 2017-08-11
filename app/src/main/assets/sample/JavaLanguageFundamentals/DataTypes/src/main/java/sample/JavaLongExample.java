package sample;
/*
        Java long Example
        This Java Example shows how to declare and use Java primitive long variable
        inside a java class.
*/

import java.util.Date;

public class JavaLongExample {

  public static void main(String[] args) {

    /*
     * long is 64 bit signed type and used when int is not large
     * enough to hold the value.
     *
     * Declare long varibale as below
     *
     * long <variable name> = <default value>;
     *
     * here assigning default value is optional.
     */

    long timeInMilliseconds = new Date().getTime();
    System.out.println("Time in milliseconds is : " + timeInMilliseconds);
  }
}

/*
 Output would be
 Time in milliseconds is : 1226836372234
 */
