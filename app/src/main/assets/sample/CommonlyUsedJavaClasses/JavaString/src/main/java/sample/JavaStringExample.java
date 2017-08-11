package sample;
/*
Java String example.
This Java String example describes how Java String object is created and used.
*/

public class JavaStringExample {

  public static void main(String args[]) {

    /*
    String in java represents the character sequence.
    */

    /* creates new empty string */
    String str1 = new String("");

    /* creates new string object whose content would be Hello World */
    String str2 = new String("Hello world");

    /* creates new string object whose content would be Hello World */
    String str3 = "Hello Wolrd";

    /*
    IMPORTANT : Difference between above given two approaches is, string object
    created using new operator will always return new string ojbect.
    While the other may return the reference of already created string
    ojbect with same content , if any.
    */

    System.out.println(str1.length());
  }
}

/*
 OUTPUT of the above given Java String Example would be :

 */
