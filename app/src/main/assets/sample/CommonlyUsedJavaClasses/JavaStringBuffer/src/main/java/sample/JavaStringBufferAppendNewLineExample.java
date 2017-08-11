package sample;
/*
        Java StringBuffer append new line example
        This example shows how to append new line in StringBuffer in Java using
        append method.
*/

public class JavaStringBufferAppendNewLineExample {

  public static void main(String args[]) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("This is the first line.");

    /*
     * To append new line to StringBuffer in Java, use
     * append method of StringBuffer class.
     *
     * Different operating systems uses different escape characters to
     * denote new line. For example, in Windows and DOS it is \r\n, in Unix
     * it is \n.
     *
     * In order to write code which works in all OS, use Java System property
     * line.separator instead of escape characters.
     */

    sbf.append(System.getProperty("line.separator"));
    sbf.append("This is second line.");

    System.out.println(sbf);
  }
}

/*
 Output of above given StringBuffer append new line Java example would be,
 This is the first line.
 This is second line.
 */
