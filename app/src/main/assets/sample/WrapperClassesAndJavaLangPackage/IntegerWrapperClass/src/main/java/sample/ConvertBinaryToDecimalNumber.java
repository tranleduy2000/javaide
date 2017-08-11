package sample;
/*
        Convert binary number to decimal number example.
        This example shows how to convert binary integer number
        to decimal integer number using valueOf method of Integer
        wrapper class.
*/

public class ConvertBinaryToDecimalNumber {

  public static void main(String[] args) {

    //declare string containing binary number
    String strBinaryNumber = "111000";

    /*
     * to convert binary number to decimal number use,
     * int parseInt method of Integer wrapper class.
     *
     * Pass 2 as redix second argument.
     */

    int decimalNumber = Integer.parseInt(strBinaryNumber, 2);
    System.out.println("Binary number converted to decimal number");
    System.out.println("Decimal number is : " + decimalNumber);
  }
}

/*
 Output would be
 Binary number converted to decimal number
 Decimal number is : 56
 */
