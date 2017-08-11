package sample;

/*
       Java String isEmpty Example.
       This Java String isEmpty example shows how to check whether the given
       string is empty or not using isEmpty method of Java String class.
*/
public class JavaStringIsEmptyExample {
  public static void main(String args[]) {

    String str1 = "";
    String str2 = null;
    String str3 = "Hello World";

    /*
     * To check whether the String is empty or not, use
     * boolean isEmpty() method of Java String class.
     *
     * This method returns true if and only if string.length() is 0.
     */

    System.out.println("Is String 1 empty? :" + str1.isEmpty());

    //this will throw NullPointerException since str2 is null
    //System.out.println("Is String 2 empty? :" + str2.isEmpty());

    System.out.println("Is String 3 empty? :" + str3.isEmpty());

    /*
     * Please note that isEmpty method was added in JDK 1.6 and it is not available
     * in previous versions.
     *
     * However, you can use
     * (string.length() == 0) instead of (string.isEmpty())
     * in previous JDK versions.
     */
  }
}

/*
 Output of Java String isEmpty would be
 Is String 1 empty? :true
 Is String 3 empty? :false
 */
