package sample;
/*
        Java StringBuffer setLength Example
        This example shows how to set length of StringBuffer using setLength method
        of StringBuffer class in Java.
*/

public class JavaStringBufferSetLengthExample {

  public static void main(String[] args) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("StringBuffer setLength method example");

    /*
     * To set length of StringBuffer, use
     * void setLength(int newLength) method of
     * StringBuffer class.
     *
     * If newLegth is less than the original length, contents of
     * StringBuffer would be truncated.
     *
     * If newLength is grater than the original length, StringBuffer
     * would be filled with null characters ('\u0000').
     */

    sbf.setLength(12);
    System.out.println("StringBuffer contents: " + sbf);

    /*
     * To delete or clear contents of StringBuffer,
     * set length of StringBuffer to 0.
     */

    sbf.setLength(0);
    System.out.println("StringBuffer contents deleted:" + sbf);
  }
}

/*
 Output of Java StringBuffer setLength example would be
 StringBuffer contents: StringBuffer
 StringBuffer contents deleted:
 */
