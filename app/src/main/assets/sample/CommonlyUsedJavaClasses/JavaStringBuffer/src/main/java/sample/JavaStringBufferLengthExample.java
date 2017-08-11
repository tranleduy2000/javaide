package sample;
/*
   Java StringBuffer length Example
   This example shows how to get length of a StringBuffer object.
*/

public class JavaStringBufferLengthExample {

  public static void main(String[] args) {

    /*
     int length() method of Java StringBuffer class returns the length
     of a StringBuffer object.
    */
    StringBuffer sb = new StringBuffer("Hello World");
    int len = sb.length();
    System.out.println("Length of a StringBuffer object is : " + len);
  }
}

/*
 Output would be
 Length of a StringBuffer object is : 11
 */
