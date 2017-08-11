package sample;
/*
        Parse CSV File using StringTokenizer example.
        This example shows how to parse comma separated file (CSV file) using
        Java StringTokenizer and BufferedReader classes.
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

public class ParseCSVFileExample {

  public static void main(String[] args) {

    try {

      //csv file containing data
      String strFile = "C:/FileIO/example.csv";

      //create BufferedReader to read csv file
      BufferedReader br = new BufferedReader(new FileReader(strFile));
      String strLine = "";
      StringTokenizer st = null;
      int lineNumber = 0, tokenNumber = 0;

      //read comma separated file line by line
      while ((strLine = br.readLine()) != null) {
        lineNumber++;

        //break comma separated line using ","
        st = new StringTokenizer(strLine, ",");

        while (st.hasMoreTokens()) {
          //display csv values
          tokenNumber++;
          System.out.println(
                  "Line # " + lineNumber + ", Token # " + tokenNumber + ", Token : " + st.nextToken());
        }

        //reset token number
        tokenNumber = 0;
      }

    } catch (Exception e) {
      System.out.println("Exception while reading csv file: " + e);
    }
  }
}

/*
 Input csv file
 "one","two","three","four"
 "parsing","comma","separated","file","java","example"
 */

/*
     Output would be,
     Line # 1, Token # 1, Token : "one"
     Line # 1, Token # 2, Token : "two"
     Line # 1, Token # 3, Token : "three"
     Line # 1, Token # 4, Token : "four"
     Line # 2, Token # 1, Token : "parsing"
     Line # 2, Token # 2, Token : "comma"
     Line # 2, Token # 3, Token : "separated"
     Line # 2, Token # 4, Token : "file"
     Line # 2, Token # 5, Token : "java"
     Line # 2, Token # 6, Token : "example"
     */
