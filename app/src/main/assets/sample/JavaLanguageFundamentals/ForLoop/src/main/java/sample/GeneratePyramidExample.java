package sample;
/*
        Generate Pyramid For a Given Number Example
        This Java example shows how to generate a pyramid of numbers for given
        number using for loop example.
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GeneratePyramidExample {

  public static void main(String[] args) throws Exception {

    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Enter Number:");
    int as = Integer.parseInt(keyboard.readLine());
    System.out.println("Enter X:");
    int x = Integer.parseInt(keyboard.readLine());

    int y = 0;

    for (int i = 0; i <= as; i++) {

      for (int j = 1; j <= i; j++) {
        System.out.print(y + "\t");
        y = y + x;
      }

      System.out.println("");
    }
  }
}

/*
 Output of this example would be

 Enter Number:
 5
 Enter X:
 1

 0
 1       2
 3       4       5
 6       7       8       9
 10      11      12      13      14

 ----------------------------------------------

 Enter Number:
 5
 Enter X:
 2

 0
 2       4
 6       8       10
 12      14      16      18
 20      22      24      26      28

 ----------------------------------------------

 Enter Number:
 5
 Enter X:
 3

 0
 3       6
 9       12      15
 18      21      24      27
 30      33      36      39      42
 */
