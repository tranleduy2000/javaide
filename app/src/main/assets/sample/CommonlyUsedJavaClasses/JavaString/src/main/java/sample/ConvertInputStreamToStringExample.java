package sample;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/*
       Java InputStream to String Example
       This Java InputStream to String example shows how to convert InputStream to String in Java.
*/
public class ConvertInputStreamToStringExample {

  public static void main(String args[]) throws IOException {

    //get InputStream of a file
    InputStream is = new FileInputStream("c:/data.txt");
    String strContent;

    /*
     * There are several way to convert InputStream to String. First is using
     * BufferedReader as given below.
     */

    //Create BufferedReader object
    BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
    StringBuffer sbfFileContents = new StringBuffer();
    String line = null;

    //read file line by line
    while ((line = bReader.readLine()) != null) {
      sbfFileContents.append(line);
    }

    //finally convert StringBuffer object to String!
    strContent = sbfFileContents.toString();

    /*
     * Second and one liner approach is to use Scanner class. This is only supported
     * in Java 1.5 and higher version.
     */

    strContent = new Scanner(is).useDelimiter("\\A").next();
  }
}
