package sample;
/*
  Java StringTokenizer count tokens example
  This example shows how to count remaining tokens of java StringTokenizer object
  using countTokens() method.
*/

import java.util.StringTokenizer;

public class StringTokenizerCountTokensExample {

  public static void main(String[] args) {

    //create StringTokenizer object
    StringTokenizer st = new StringTokenizer("Java StringTokenizer count Tokens Example");

    /*
     countTokens() method returns the number of tokens remaining from the current
     delimiter set.
     It calculates the number of times StringTokenizer's nextToken() method
     can be called before it generates an exception.
     Please note that invoking countTokens() method does not advance the
     current position.

    */

    //iterate through tokens
    while (st.hasMoreTokens()) {
      System.out.println("Remaining Tokens : " + st.countTokens());
      System.out.println(st.nextToken());
    }
  }
}

/*
 Output Would be
 Remaining Tokens : 5
 Java
 Remaining Tokens : 4
 StringTokenizer
 Remaining Tokens : 3
 count
 Remaining Tokens : 2
 Tokens
 Remaining Tokens : 1
 Example
 */
