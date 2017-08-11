package sample;
/*
        Java Pyramid 5 Example
        This Java Pyramid example shows how to generate pyramid or triangle like
        given below using for loop.

        12345
        1234
        123
        12
        1

*/

public class JavaPyramid5 {

  public static void main(String[] args) {

    for (int i = 5; i > 0; i--) {

      for (int j = 0; j < i; j++) {
        System.out.print(j + 1);
      }

      System.out.println("");
    }
  }
}

/*

 Output of the example would be
 12345
 1234
 123
 12
 1

 */
