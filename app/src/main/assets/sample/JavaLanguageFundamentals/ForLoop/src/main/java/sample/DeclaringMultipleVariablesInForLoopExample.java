package sample;
/*
  Declare multiple variables in for loop Example
  This Java Example shows how to declare multiple variables in Java For loop using
  declaration block.
*/

public class DeclaringMultipleVariablesInForLoopExample {

  public static void main(String[] args) {

    /*
     * Multiple variables can be declared in declaration block of for loop.
     */

    for (int i = 0, j = 1, k = 2; i < 5; i++)
      System.out.println("I : " + i + ",j : " + j + ", k : " + k);

    /*
     * Please note that the variables which are declared, should be of same type
     * as in this example int.
     */

    //THIS WILL NOT COMPILE
    //for(int i=0, float j; i < 5; i++);
  }
}
