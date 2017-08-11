package sample;

/**
 * Even Odd Number Example This Java Even Odd Number Example shows how to check if the given number
 * is even or odd.
 */
public class FindEvenOrOddNumber {

  public static void main(String[] args) {

    //create an array of 10 numbers
    int[] numbers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    for (int i = 0; i < numbers.length; i++) {

      /*
       * use modulus operator to check if the number is even or odd.
       * If we divide any number by 2 and reminder is 0 then the number is
       * even, otherwise it is odd.
       */

      if (numbers[i] % 2 == 0) System.out.println(numbers[i] + " is even number.");
      else System.out.println(numbers[i] + " is odd number.");
    }
  }
}

/*
 Output of the program would be
 1 is odd number.
 2 is even number.
 3 is odd number.
 4 is even number.
 5 is odd number.
 6 is even number.
 7 is odd number.
 8 is even number.
 9 is odd number.
 10 is even number.
 */
