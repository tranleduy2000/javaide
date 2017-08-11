package sample;
/*
  Java StringTokenizer example
  This example shows how a Java StringTokenizer can be used to break a string
  into tokens.
*/

import java.util.StringTokenizer;

public class JavaStringTokenizerExample {

  public static void main(String[] args) {

    //create StringTokenizer object
    StringTokenizer st = new StringTokenizer("Java StringTokenizer Example");

    //iterate through tokens
    while (st.hasMoreTokens()) System.out.println(st.nextToken());
  }
}

/*
 Output Would be
 Java
 StringTokenizer
 Example
 */
