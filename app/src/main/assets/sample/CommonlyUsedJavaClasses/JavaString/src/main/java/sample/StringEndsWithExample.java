package sample;

/*
  String endsWith Example
  This example shows how to check if a particular string is ending with
  a specified word.
*/
public class StringEndsWithExample {

  public static void main(String[] args) {

    //declare the original String
    String strOrig = "Hello World";

    /*
    check whether String ends with World or not.
    Use endsWith method of the String class to check the same.
    endsWith() method returns true if a string is ending with a given word
    otherwise it returns false
    */
    if (strOrig.endsWith("World")) {
      System.out.println("String ends with World");
    } else {
      System.out.println("String does not end with World");
    }
  }
}

/*
 Output of the program would be :
 String ends with World
 */
