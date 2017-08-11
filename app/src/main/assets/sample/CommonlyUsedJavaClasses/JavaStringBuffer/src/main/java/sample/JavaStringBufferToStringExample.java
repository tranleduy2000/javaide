package sample;
/*
        StringBuffer toString Java Example
        This example shows how to convert StringBuffer to String in Java using
        toString method of String class.
*/

public class JavaStringBufferToStringExample {

  public static void main(String args[]) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("Hello World!");

    /*
     * To convert StringBuffer to String object, use
     * String toString() method of StringBuffer class.
     */

    String str = sbf.toString();

    System.out.println("StringBuffer to String: " + str);
  }
}

/*
 Output of above given StringBuffer to String example would be
 StringBuffer to String: Hello World!
 */
