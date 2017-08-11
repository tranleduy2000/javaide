package sample;
/*
   Java StringBuffer SubString Example
   This example shows how to get a sub string of content of the StringBuffer using
   substring method of Java StringBuffer class.
*/

public class JavaStringBufferSubStringExample {

  public static void main(String[] args) {
    //create StringBuffer object
    StringBuffer sb = new StringBuffer("Java StringBuffer SubString Example");
    System.out.println("Original Text : " + sb);

    /*
     SubString method is overloaded in StringBuffer class
     1. String substring(int start)
     returns new String which contains sequence of characters contined in
     StringBuffer starting from start index to StringBuffer.length() - 1 index
    */
    String strPart1 = sb.substring(5);
    System.out.println("Substring 1 : " + strPart1);

    /*
     2. String substring(int start, int end)
     returns new String which contains sequence of characters contined in
     StringBuffer starting from start index to end index
    */
    String strPart2 = sb.substring(0, 17);
    System.out.println("Substring 2 : " + strPart2);

    /* Please note that both the methods can throw a StringIndexOutOfBoundsException
       if start or end is invalid.
    */

  }
}

/*
 Output would be
 Original Text : Java StringBuffer SubString Example
 Substring 1 : StringBuffer SubString Example
 Substring 2 : Java StringBuffer
 */
