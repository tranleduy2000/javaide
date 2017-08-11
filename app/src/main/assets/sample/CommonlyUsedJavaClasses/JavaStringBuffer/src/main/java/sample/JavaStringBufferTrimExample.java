package sample;
/*
        StringBuffer Trim Java Example
        This example shows how to trim StringBuffer object in Java using substring method.
*/

public class JavaStringBufferTrimExample {

  public static void main(String[] args) {

    //create StringBuffer object
    StringBuffer sbf = new StringBuffer("   Hello World  !  ");

    /*
     * Method 1: convert StringBuffer to string and use trim method of
     * String.
     */

    String str = sbf.toString().trim();

    System.out.println("StringBuffer trim: \"" + str + "\"");

    /*
     * Method 2: Create method to trim contents of StringBuffer
     * using substring method.
     */

    System.out.println("\"" + trim(sbf) + "\"");
  }

  private static String trim(StringBuffer sbf) {

    int start, end;

    //find the first character which is not space
    for (start = 0; start < sbf.length(); start++) {
      if (sbf.charAt(start) != ' ') break;
    }

    //find the last character which is not space
    for (end = sbf.length(); end > start; end--) {
      if (sbf.charAt(end - 1) != ' ') break;
    }

    return sbf.substring(start, end);
  }
}

/*
 Output of above given StringBuffer trim example would be
 StringBuffer trim: Hello World  !
 Hello World
 */
