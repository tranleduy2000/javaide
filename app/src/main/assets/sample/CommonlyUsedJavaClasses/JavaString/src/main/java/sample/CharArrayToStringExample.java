package sample;
/*
       Java Char Array To String Example
       This Java char array to String example shows how to convert char array to
       String in Java.
*/

public class CharArrayToStringExample {

  public static void main(String args[]) {

    //char array
    char[] charArray = new char[]{'J', 'a', 'v', 'a'};

    /*
     * To convert char array to String in Java, use
     * String(Char[] ch) constructor of Java String class.
     */

    String str = new String(charArray);

    System.out.println("Char array converted to String: " + str);
  }
}

/*
 Output of above given char array to String example would be
 Char array converted to String: Java
 */
