package sample;
/*
        Compare Two Numbers Java Example
        This Compare Two Numbers Java Example shows how to compare two numbers
        using if else if statements.
*/

public class CompareTwoNumbers {

  public static void main(String[] args) {

    //declare two numbers to compare
    int num1 = 324;
    int num2 = 234;

    if (num1 > num2) {
      System.out.println(num1 + " is greater than " + num2);
    } else if (num1 < num2) {
      System.out.println(num1 + " is less than " + num2);
    } else {
      System.out.println(num1 + " is equal to " + num2);
    }
  }
}

/*
 Output of Compare Two Numbers Java Example would be
 324 is greater than 234
 */
