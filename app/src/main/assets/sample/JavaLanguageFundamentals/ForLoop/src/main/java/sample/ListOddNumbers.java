package sample;
/*
        List Odd Numbers Java Example
        This List Odd Numbers Java Example shows how to find and list odd
        numbers between 1 and any given number.
*/

public class ListOddNumbers {

  public static void main(String[] args) {

    //define the limit
    int limit = 50;

    System.out.println("Printing Odd numbers between 1 and " + limit);

    for (int i = 1; i <= limit; i++) {

      //if the number is not divisible by 2 then it is odd
      if (i % 2 != 0) {
        System.out.print(i + " ");
      }
    }
  }
}

/*
 Output of List Odd Numbers Java Example would be
 Printing Odd numbers between 1 and 50
 1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 35 37 39 41 43 45 47 49
 */
