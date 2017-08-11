package sample;
/*
  Java StringTokenizer return tokens as Objects example
  This example shows how to return tokens of java StringTokenizer as Objects
  instead of String.
*/

import java.util.StringTokenizer;

public class StringTokenizerReturnTokensAsObjectsExample {

  public static void main(String[] args) {

    //create StringTokenizer object
    StringTokenizer st = new StringTokenizer("Java StringTokenizer Example");

    /*
     To return tokens as Objects use hasMoreElements() method of StringTokenizer
     class which returns the same value as hasMoreTokens(), with nextElement()
     method which returns the same value as nextToken() method except that the
     return type of nextElement() method is Object and not a String.
    */

    //iterate through tokens using hasMoreElements() method
    while (st.hasMoreElements()) {
      System.out.println(st.nextElement());
    }
  }
}

/*
 Output Would be
 Java
 StringTokenizer
 Example
 */
