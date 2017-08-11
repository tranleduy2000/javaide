package sample;
/*
        Convert decimal integer to binary number example.
        This example shows how to convert int to binary number using
        toBinaryString method of Integer wrapper class.
*/

public class ConvertIntToBinaryExample {

  public static void main(String[] args) {

    int i = 56;

    String strBinaryNumber = Integer.toBinaryString(i);

    System.out.println("Convert decimal number to binary number example");
    System.out.println("Binary value of " + i + " is " + strBinaryNumber);
  }
}

/*
 Output would be
 Convert decimal number to binary number example
 Binary value of 56 is 111000
 */
