package sample;

/*
       Java String Contains example.
       This Java String contains examples shows how to use contains method of Java String class.
*/
public class JavaStringContains {
  public static void main(String args[]) {
    String str1 = "Hello World";
    String str2 = "Hello";

    /*
     * To check whether the string contains specified character sequence use,
     * boolean contains(CharSequence sq)
     * method of Java String class.
     *
     * This method returns true if the string contains given character sequence.
     * Please note that contains method is added in Java 1.5
     */

    boolean blnFound = str1.contains(str2);
    System.out.println("String contains another string? : " + blnFound);

    /*
     * Please also note that the comparison is case sensitive.
     */

  }
}

/*
 Output of Java String contains example would be
 String contains another string? : true
 */
