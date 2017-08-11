package sample;
/*
        Java String to Lower Case example.
        This Java String to Lower Case example shows how to change the string to lower case
        using toLowerCase method of String class.
*/

public class StringToLowerCaseExample {

  public static void main(String[] args) {

    String str = "STRING TOLOWERCASE EXAMPLE";

    /*
     * To change the case of string to lower case use,
     * public String toLowerCase() method of String class.
     *
     */

    String strLower = str.toLowerCase();

    System.out.println("Original String: " + str);
    System.out.println("String changed to lower case: " + strLower);
  }
}

/*
 Output would be
 Original String: STRING TOLOWERCASE EXAMPLE
 String changed to lower case: string tolowercase example
 */
