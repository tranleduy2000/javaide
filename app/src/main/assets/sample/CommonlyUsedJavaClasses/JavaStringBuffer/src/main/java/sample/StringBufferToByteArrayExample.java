package sample;
/*
        StringBuffer To byte Array Java Example
        This example shows how to convert StringBuffer to byte Array in Java.
*/

public class StringBufferToByteArrayExample {

  public static void main(String[] args) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("Java StringBuffer To byte array example");

    /*
     * To Convert StringBuffer to byte array, first convert StringBuffer
     * to String object using toString method of String class.
     *
     * Once it is converted to String object, use
     * byte[] getBytes() method of String class to convert it into
     * byte array.
     */

    byte bytes[] = sbf.toString().getBytes();

    System.out.println("StringBuffer converted to byte array");
  }
}
