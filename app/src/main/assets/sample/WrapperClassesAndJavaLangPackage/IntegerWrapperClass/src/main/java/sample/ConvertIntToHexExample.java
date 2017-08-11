package sample;
/*
        Convert decimal integer to hexadecimal number example.
        This example shows how to convert int to hexadecimal number using
        toHexString method of Integer wrapper class.
*/

public class ConvertIntToHexExample {

  public static void main(String[] args) {

    int i = 32;

    String strHexNumber = Integer.toHexString(i);

    System.out.println("Convert decimal number to hexadecimal number example");
    System.out.println("Hexadecimal value of " + i + " is " + strHexNumber);
  }
}

/*
 Output would be
 Convert decimal number to hexadecimal number example
 Hexadecimal value of 32 is 20
 */
