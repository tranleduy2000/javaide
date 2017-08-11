package sample;
/*
  Java Search String using indexOf Example
  This example shows how we can search a word within a String object using
  indexOf method.
*/

public class SearchStringExample {

  public static void main(String[] args) {
    //declare a String object
    String strOrig = "Hello world Hello World";

    /*
      To search a particular word in a given string use indexOf method.
      indexOf method. It returns a position index of a word within the string
      if found. Otherwise it returns -1.
    */

    int intIndex = strOrig.indexOf("Hello");

    if (intIndex == -1) {
      System.out.println("Hello not found");
    } else {
      System.out.println("Found Hello at index " + intIndex);
    }

    /*
      we can also search a word after particular position using
      indexOf(String word, int position) method.
    */

    int positionIndex = strOrig.indexOf("Hello", 11);
    System.out.println("Index of Hello after 11 is " + positionIndex);

    /*
      Use lastIndexOf method to search a last occurrence of a word within string.
    */
    int lastIndex = strOrig.lastIndexOf("Hello");
    System.out.println("Last occurrence of Hello is at index " + lastIndex);
  }
}

/*
 Output of the program would be :
 Found Hello at index 0
 Index of Hello after 11 is 12
 Last occurrence of Hello is at index 12
 */
