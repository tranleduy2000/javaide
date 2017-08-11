package sample;
/*
        Reverse string by word using StringTokenizer example.
        This example shows how to reverse a line or sentense using
        Java StringTokenizer and String classes.
*/

import java.util.StringTokenizer;

public class ReverseLine {

  public static void main(String[] args) {

    String strLine = "Java Reverse string by word example";

    //specify delimiter as " " space
    StringTokenizer st = new StringTokenizer(strLine, " ");

    String strReversedLine = "";

    while (st.hasMoreTokens()) {
      strReversedLine = st.nextToken() + " " + strReversedLine;
    }

    System.out.println("Reversed string by word is : " + strReversedLine);
  }
}

/*
 Output would be
 Reversed string by word is : example word by string Reverse Java
 */
