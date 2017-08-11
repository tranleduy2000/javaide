package sample;
/*
       Read Number from Console and Check if it is a Palindrome Number
       This Java example shows how to input the number from console and
       check if the number is a palindrome number or not.
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputPalindromeNumberExample {

  public static void main(String[] args) {

    System.out.println("Enter the number to check..");
    int number = 0;

    try {
      //take input from console
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      //parse the line into int
      number = Integer.parseInt(br.readLine());

    } catch (NumberFormatException ne) {
      System.out.println("Invalid input: " + ne);
      //            System.exit(0);
    } catch (IOException ioe) {
      System.out.println("I/O Error: " + ioe);
      //            System.exit(0);
    }

    System.out.println("Number is " + number);
    int n = number;
    int reversedNumber = 0;
    int temp = 0;

    //reverse the number
    while (n > 0) {
      temp = n % 10;
      n = n / 10;
      reversedNumber = reversedNumber * 10 + temp;
    }

    /*
     * if the number and it's reversed number are same, the number is a
     * palindrome number
     */
    if (number == reversedNumber) System.out.println(number + " is a palindrome number");
    else System.out.println(number + " is not a palindrome number");
  }
}

/*
 Output of the program would be
 Enter the number to check..
 121
 Number is 121
 121 is a palindrome number
 */
