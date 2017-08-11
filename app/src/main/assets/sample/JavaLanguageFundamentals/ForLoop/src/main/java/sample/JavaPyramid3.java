package sample;

/*
        Java Pyramid 3 Example
        This Java Pyramid example shows how to generate pyramid or triangle like
        given below using for loop.

        *
        **
        ***
        ****
        *****
        *****
        ****
        ***
        **
        *
*/
public class JavaPyramid3 {

  public static void main(String[] args) {

    for (int i = 1; i <= 5; i++) {

      for (int j = 0; j < i; j++) {
        System.out.print("*");
      }

      //generate a new line
      System.out.println("");
    }

    //create second half of pyramid
    for (int i = 5; i > 0; i--) {

      for (int j = 0; j < i; j++) {
        System.out.print("*");
      }

      //generate a new line
      System.out.println("");
    }
  }
}

/*

 Output of the example would be
 *
 **
 ***
 ****
 *****
 *****
 ****
 ***
 **
 *

 */
