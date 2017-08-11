package sample;
/*
        Convert hexadecimal number to decimal number example.
        This example shows how to convert hexadecimal number
        to decimal number using valueOf method of Integer
        wrapper class.
*/

public class ConvertHexToDecimalExample {

  public static void main(String[] args) {

    //declare string containing hexadecimal number
    String strHexNumber = "20";

    /*
     * to convert hexadecimal number to decimal number use,
     * int parseInt method of Integer wrapper class.
     *
     * Pass 16 as redix second argument.
     */

    int decimalNumber = Integer.parseInt(strHexNumber, 16);
    System.out.println("Hexadecimal number converted to decimal number");
    System.out.println("Decimal number is : " + decimalNumber);
  }
}

/*
 Output would be
 Hexadecimal number converted to decimal number
 Decimal number is : 32
 */
