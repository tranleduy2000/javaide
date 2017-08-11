package sample;
/*
       Java String to InputStream Example.
       This Java String to InputStream example shows how to convert Java String to InputStream.
*/

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JavaStringToInputStreamExample {
  public static void main(String args[]) {

    String str1 = "Java convert String to InputStream Example";

    //convert string to bytes first using getBytes method of String class.
    byte[] bytes = str1.getBytes();

    /*
     * To convert Java String to InputStream, use
     * ByteArrayInputStream class.
     */

    InputStream inputStream = new ByteArrayInputStream(bytes);
  }
}
