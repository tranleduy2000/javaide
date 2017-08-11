package sample;
/*
   Java StringBuffer delete remove character or clear content Example
   This example shows how to delete or remove a particular character or clear
   entire content of a StringBuffer object.
*/

public class JavaStringBufferDeleteExample {

  public static void main(String[] args) {
    /*
      Java StringBuffer class following methods to delete / remove characters
      or claring the contents of a StringBuffer object.
    */

    /*
     StringBuffer delete(int start, int end) remove the characters from start
     index to an end-1 index provided.
     This method can throw a StringIndexOutOfBoundException if the start
     index is invalid.
    */
    StringBuffer sb1 = new StringBuffer("Hello World");
    sb1.delete(0, 6);
    System.out.println(sb1);

    /*
     To clear contents of a StringBuffer use delete(int start, int end) method
     in the below given way
    */
    StringBuffer sb2 = new StringBuffer("Some Content");
    System.out.println(sb2);
    sb2.delete(0, sb2.length());
    System.out.println(sb2);

    /*
     StringBuffer deleteCharAt(int index) deletes the character at specified
     index.
     This method throws StringIndexOutOfBoundException if index is negative
     or grater than or equal to the length.
    */
    StringBuffer sb3 = new StringBuffer("Hello World");
    sb3.deleteCharAt(0);
    System.out.println(sb3);
  }
}

/*
 Output would be

 World
 Some Content

 ello World

 */
