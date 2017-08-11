package sample;
/*
  Java StringTokenizer - Specify Delimiter example.
  This example shows how a specify a delimiter for StringTokenizer object.
  The default delimiters are
  \t character (tab),
  \n character (new line),
  \r character (carriage return) and
  \f character (form feed).
*/

import java.util.StringTokenizer;

public class StringTokenizerSpecifyDelimiterExample {

  public static void main(String[] args) {

    /*
     * There are two ways to specify a delimiter for a StringTokenizer object.
     * 1. At the creating time by specifying in the StringTokenizer constructor
     * 2. Specify it in nextToken() method
     */

    //1. Using StringTokenizer constructor
    StringTokenizer st1 = new StringTokenizer("Java|StringTokenizer|Example 1", "|");

    //iterate through tokens
    while (st1.hasMoreTokens()) System.out.println(st1.nextToken());

    //2. Using nextToken() method. Note that the new delimiter set remains the
    //default after this method call
    StringTokenizer st2 = new StringTokenizer("Java|StringTokenizer|Example 2");

    //iterate through tokens
    while (st2.hasMoreTokens()) System.out.println(st2.nextToken("|"));
  }
}

/*
 Java
 StringTokenizer
 Example 1
 Java
 StringTokenizer
 Example 2
 */
