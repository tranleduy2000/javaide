package sample;
/*
  Infinite For loop Example
  This Java Example shows how to create a for loop that runs infinite times
  in Java program. It happens when the loop condition is always evaluated as true.
*/

public class InfiniteForLoop {

  public static void main(String[] args) {

    /*
     * Its perfectely legal to skip any of the 3 parts of the for loop.
     * Below given for loop will run infinite times.
     */
    for (; ; ) System.out.println("Hello");

    /*
     * To terminate this program press ctrl + c in the console.
     */
  }
}

/*
 Output would be
 Hello
 Hello
 Hello
 Hello
 ..
 ..
 */
