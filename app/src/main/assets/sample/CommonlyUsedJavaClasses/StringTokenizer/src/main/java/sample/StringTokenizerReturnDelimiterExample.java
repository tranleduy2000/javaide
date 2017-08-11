package sample;
/*
  Java StringTokenizer - Return Delimiters as Tokens example.
  This example shows how to return delimiters along with the string tokens
  using StringTokenizer object.
*/

import java.util.StringTokenizer;

public class StringTokenizerReturnDelimiterExample {

  public static void main(String[] args) {

    /*
      By default, StringTokenizer object does not return the delimiters along
      with the string tokens.
      To return the delimiter along with the string tokens, use
      StringTokenizer(String str, String delim, boolean returnDelims) construcor
    */

    //Create StringTokenizer object
    StringTokenizer st = new StringTokenizer("Java|StringTokenizer|Example 1", "|", true);

    //iterate through tokens
    while (st.hasMoreTokens()) System.out.println(st.nextToken("|"));
  }
}

/*
 Java
 |
 StringTokenizer
 |
 Example 1
 */
