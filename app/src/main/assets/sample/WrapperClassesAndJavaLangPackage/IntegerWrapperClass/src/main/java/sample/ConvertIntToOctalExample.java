package sample;
/*
        Convert decimal integer to octal number example.
        This example shows how to convert int to octal number using
        toOctalString method of Integer wrapper class.
*/

public class ConvertIntToOctalExample {

  public static void main(String[] args) {

    int i = 27;

    String strOctalNumber = Integer.toOctalString(i);

    System.out.println("Convert decimal number to octal number example");
    System.out.println("Octal value of " + i + " is " + strOctalNumber);
  }
}

/*
 Output would be
 Convert decimal number to octal number example
 octal value of 27 is 33
 */
