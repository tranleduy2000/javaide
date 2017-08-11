package sample;

/*
        Java Pyramid 4 Example
        This Java Pyramid example shows how to generate pyramid or triangle like
        given below using for loop.

        1
        12
        123
        1234
        12345

*/
public class JavaPyramid4 {

  public static void main(String[] args) {

    for (int i = 1; i <= 5; i++) {

      for (int j = 0; j < i; j++) {
        System.out.print(j + 1);
      }

      System.out.println("");
    }
  }
}

/*

 Output of the example would be
 1
 12
 123
 1234
 12345

 */
