package sample;
/*
Java String replace example.
This Java String Replace example describes how replace method of java String class
can be used to replace character or substring can be replaced by new one.
*/

public class JavaStringReplaceExample {

  public static void main(String args[]) {

    /*
    Java String class defines three methods to replace character or substring from
    the given Java String object.
    1) String replace(int oldChar, int newChar)
    This method replaces a specified character with new character and returns a
    new string object.
    2) String replaceFirst(String regularExpression, String newString)
    Replaces the first substring of this string that matches the given regular
    expression with the given new string.
    3) String replaceAll(String regex, String replacement)
    Replaces the each substring of this string that matches the
    given regular expression with the given new string.
    */

    String str = "Replace Region";

    /*
    Replaces all occourances of given character with new one and returns new
    String object.
    */
    System.out.println(str.replace('R', 'A'));

    /*
    Replaces only first occourances of given String with new one and
    returns new String object.
    */
    System.out.println(str.replaceFirst("Re", "Ra"));

    /*
    Replaces all occourances of given String with new one and returns
    new String object.
    */
    System.out.println(str.replaceAll("Re", "Ra"));
  }
}

/*

 OUTPUT of the above given Java String Replace Example would be :

 Aeplace Aegion
 Raplace Region
 Raplace Ragion

 */
