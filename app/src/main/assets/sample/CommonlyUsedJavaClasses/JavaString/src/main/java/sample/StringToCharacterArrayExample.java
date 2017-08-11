package sample;
/*
Convert String to Character Array Example
This example shows how to convert a given String object to an array
of character
*/

public class StringToCharacterArrayExample {

  public static void main(String args[]) {
    //declare the original String object
    String strOrig = "Hello World";
    //declare the char array
    char[] stringArray;

    //convert string into array using toCharArray() method of string class
    stringArray = strOrig.toCharArray();

    //display the array
    for (int index = 0; index < stringArray.length; index++) System.out.print(stringArray[index]);
  }
}

/*
 Output of the program would be :
 Hello World
 */
