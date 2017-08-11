package sample;
/*
   Java StringBuffer Reverse Example
   This example shows how to reverse the content of the StringBuffer using
   reverse method of Java StringBuffer class.
*/

public class JavaStringBufferReverseExample {

  public static void main(String[] args) {

    //create StringBuffer object
    StringBuffer sb = new StringBuffer("Java StringBuffer Reverse Example");
    System.out.println("Original StringBuffer Content : " + sb);

    //To reverse the content of the StringBuffer use reverse method
    sb.reverse();
    System.out.println("Reversed StringBuffer Content : " + sb);
  }
}

/*
 Output Would be
 Original StringBuffer Content : Java StringBuffer Reverse Example
 Reversed StringBuffer Content : elpmaxE esreveR reffuBgnirtS avaJ
 */
