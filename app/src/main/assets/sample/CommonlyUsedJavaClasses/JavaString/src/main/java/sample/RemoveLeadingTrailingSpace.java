package sample;
/*
        Java String Trim Example.
        This Java String trim example shows how to remove leading and trailing space
        from string using trim method of Java String class.
*/

public class RemoveLeadingTrailingSpace {

  public static void main(String[] args) {

    String str = "   String Trim Example   ";

    /*
     * To remove leading and trailing space from string use,
     * public String trim() method of Java String class.
     */

    String strTrimmed = str.trim();

    System.out.println("Original String is: " + str);
    System.out.println("Removed Leading and trailing space");
    System.out.println("New String is: " + strTrimmed);
  }
}

/*
 Output would be
 Original String is:    String Trim Example
 Removed Leading and trailing space
 New String is: String Trim Example
 */
