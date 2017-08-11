package sample;
/*
       Java String to byte Array Example
       This Java String to byte Array example shows how to convert Java String object
       to byte array.
*/

public class JavaStringToByteArray {
  public static void main(String args[]) {

    //Java String object
    String str = "Hello World";

    /*
     * To convert Java String to byte array, use
     * byte[] getBytes() method of Java String class.
     */

    byte[] bytes = str.getBytes();
  }
}
