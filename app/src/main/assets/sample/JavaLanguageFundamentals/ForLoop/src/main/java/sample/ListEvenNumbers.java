package sample;
/*
        List Even Numbers Java Example
        This List Even Numbers Java Example shows how to find and list even
        numbers between 1 and any given number.
*/

public class ListEvenNumbers {

  public static void main(String[] args) {

    //define limit
    int limit = 50;

    System.out.println("Printing Even numbers between 1 and " + limit);

    for (int i = 1; i <= limit; i++) {

      // if the number is divisible by 2 then it is even
      if (i % 2 == 0) {
        System.out.print(i + " ");
      }
    }
  }
}

/*
 Output of List Even Numbers Java Example would be
 Printing Even numbers between 1 and 50
 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50
 */
