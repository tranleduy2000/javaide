package sample;

/**
 * Swap Numbers Java Example This Swap Numbers Java Example shows how to swap value of two numbers
 * using java.
 */
public class SwapElementsExample {

  public static void main(String[] args) {

    int num1 = 10;
    int num2 = 20;

    System.out.println("Before Swapping");
    System.out.println("Value of num1 is :" + num1);
    System.out.println("Value of num2 is :" + num2);

    //swap the value
    swap(num1, num2);
  }

  private static void swap(int num1, int num2) {

    int temp = num1;
    num1 = num2;
    num2 = temp;

    System.out.println("After Swapping");
    System.out.println("Value of num1 is :" + num1);
    System.out.println("Value of num2 is :" + num2);
  }
}

/*
 Output of Swap Numbers example would be
 Before Swapping
 Value of num1 is :10
 Value of num2 is :20
 After Swapping
 Value of num1 is :20
 Value of num2 is :10
 */
