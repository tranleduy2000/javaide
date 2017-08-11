package sample;
/*
        Java StringBuffer to InputStream Example
        This example shows how to convert StringBuffer to InputStream in Java using
        ByteInputStream class.
*/

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StringBufferToInputStreamExample {

  public static void main(String args[]) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("StringBuffer to InputStream Example");

    /*
     * To convert StringBuffer to InputStream in Java, first get bytes
     * from StringBuffer after converting it into String object.
     */

    byte[] bytes = sbf.toString().getBytes();

    /*
     * Get ByteArrayInputStream from byte array.
     */

    InputStream inputStream = new ByteArrayInputStream(bytes);

    System.out.println("StringBuffer converted to InputStream");
  }
}
