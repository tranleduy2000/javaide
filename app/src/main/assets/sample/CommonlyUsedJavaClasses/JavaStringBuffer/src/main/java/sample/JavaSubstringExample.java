package sample;
/*
Java substring example.
This Java substring example describes how substring method of java String class can
be used to get substring of the given java string object.
*/

public class JavaSubstringExample {

  public static void main(String args[]) {

    /*
    Java String class defines two methods to get substring from the given
    Java String object.

    1) public String substring(int startIndex)
    This method returns new String object containing the substring of the
    given string from specified startIndex (inclusive).
    IMPORTANT : This method can throw IndexOutOfBoundException if startIndex
    is negative or grater than length of the string.

    2) public String substring(int startIndex,int endIndex)
    This method returns new String object containing the substring of the
    given string from specified startIndex to endIndex. Here, startIndex is
    inclusive while endIndex is exclusive.
    IMPORTANT: This method can throw IndexOutOfBoundException if startIndex
    is negative and if startIndex of endIndex is grater than the string length.
    */

    String name = "Hello World";

    /*
    This will print the substring starting from index 6
    */
    System.out.println(name.substring(6));

    /*
    This will print the substring starting from index 0 upto 4 not 5.
    IMPORTANT : Here startIndex is inclusive while endIndex is exclusive.
    */
    System.out.println(name.substring(0, 5));
  }
}

/*
 OUTPUT of the above given Java substring Example would be :
 World
 Hello
 */
