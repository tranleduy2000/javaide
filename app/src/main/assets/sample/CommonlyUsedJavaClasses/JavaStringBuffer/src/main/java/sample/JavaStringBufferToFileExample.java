package sample;
/*
        StringBuffer To File Java Example
        This example shows how to write contents of StringBuffer to file using BufferedWriter
        and FileWriter Java classes.
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JavaStringBufferToFileExample {

  public static void main(String[] args) throws IOException {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer();

    //StringBuffer contents
    sbf.append("StringBuffer contents first line.");
    //new line
    sbf.append(System.getProperty("line.separator"));
    //second line
    sbf.append("StringBuffer contents second line.");

    /*
     * To write contents of StringBuffer to a file, use
     * BufferedWriter class.
     */

    BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("d:/demo.txt")));

    //write contents of StringBuffer to a file
    bwr.write(sbf.toString());

    //flush the stream
    bwr.flush();

    //close the stream
    bwr.close();

    System.out.println("Content of StringBuffer written to File.");
  }
}

/*
 Output of above given Write StringBuffer to File example would be
 Content of StringBuffer written to File.

 Contents of file "demo.txt" would be
 StringBuffer contents first line.
 StringBuffer contents second line.
 */
