package sample;
/*
        Java String to Upper Case example.
        This Java String to Upper Case example shows how to change the string to upper case
        using toUpperCase method of String class.
*/

public class StringToUpperCaseExample {

  public static void main(String[] args) {

    String str = "string touppercase example";

    /*
     * To change the case of string to upper case use,
     * public String toUpperCase() method of String class.
     *
     */

    String strUpper = str.toUpperCase();

    System.out.println("Original String: " + str);
    System.out.println("String changed to upper case: " + strUpper);
  }
}

/*
 Output would be
 Original String: string touppercase example
 String changed to upper case: STRING TOUPPERCASE EXAMPLE*/
