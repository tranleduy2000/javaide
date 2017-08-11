package sample;
/*
        StringBuffer Reset Java Example
        This example shows how to reset StringBuffer object to null or empty vale using delete
        method.
*/

public class StrinBufferResetExample {

  public static void main(String[] args) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("Hello World!");

    System.out.println("StringBuffer content: " + sbf);

    /*
     * To reset StringBuffer to empty value, user
     * StringBuffer delete() method of StringBuffer class.
     */

    sbf.delete(0, sbf.length());

    System.out.println("StringBuffer content after reset:" + sbf);
  }
}

/*
 Output of above given Java StringBuffer Reset example would be
 StringBuffer content: Hello World!
 StringBuffer content after reset:
 */
