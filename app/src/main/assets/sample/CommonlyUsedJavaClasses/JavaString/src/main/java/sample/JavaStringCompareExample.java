package sample;
/*
Java String compare example.
This Java String compare example describes how Java String is compared with another
Java String object or Java Object.
*/

public class JavaStringCompareExample {

  public static void main(String args[]) {

    /*
    Java String class defines following methods to compare Java String object.
    1) int compareTo( String anotherString )
    compare two string based upon the unicode value of each character in the String.
    Returns negative int if first string is less than another
    Returns positive int if first string is grater than another
    Returns 0 if both strings are same.
    2) int compareTo( Object obj )
    Behaves exactly like compareTo ( String anotherString) if the argument object
    is of type String, otherwise throws ClassCastException.
    3) int compareToIgnoreCase( String anotherString )
    Compares two strings ignoring the character case of the given String.
    */

    String str = "Hello World";
    String anotherString = "hello world";
    Object objStr = str;

    /* compare two strings, case sensitive */
    System.out.println(str.compareTo(anotherString));
    /* compare two strings, ignores character case  */
    System.out.println(str.compareToIgnoreCase(anotherString));
    /* compare string with object */
    System.out.println(str.compareTo(objStr.toString()));
  }
}

/*
 OUTPUT of the above given Java String compare Example would be :
 -32
 0
 0
 */
