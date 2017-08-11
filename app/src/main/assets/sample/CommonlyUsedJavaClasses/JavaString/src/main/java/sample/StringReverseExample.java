package sample;
/*
 Java String Reverse example.
 This example shows how to reverse a given string
*/

public class StringReverseExample {

  public static void main(String args[]) {
    //declare orinial string
    String strOriginal = "Hello World";
    System.out.println("Original String : " + strOriginal);

    /*
    The easiest way to reverse a given string is to use reverse()
    method of java StringBuffer class.
    reverse() method returns the StringBuffer object so we need to
    cast it back to String using toString() method of StringBuffer
    */

    strOriginal = new StringBuffer(strOriginal).reverse().toString();

    System.out.println("Reversed String : " + strOriginal);
  }
}

/*
 Output of the program would be :
 Original String : Hello World
 Reversed String : dlroW olleH
 */
