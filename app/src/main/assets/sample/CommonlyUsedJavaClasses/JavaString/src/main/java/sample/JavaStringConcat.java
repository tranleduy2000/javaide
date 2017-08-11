package sample;

/*
       Java String Concat Example.
       This Java String concat example shows how to concat String in Java.
*/
public class JavaStringConcat {
  public static void main(String args[]) {
    /*
     * String concatenation can be done in several ways in Java.
     */

    String str1 = "Hello";
    String str2 = " World";

    //1. Using + operator
    String str3 = str1 + str2;
    System.out.println("String concat using + operator : " + str3);

    /*
     * Internally str1 + str 2 statement would be executed as,
     * new StringBuffer().append(str1).append(str2)
     *
     * String concatenation using + operator is not recommended for large number
     * of concatenation as the performance is not good.
     */

    //2. Using String.concat() method
    String str4 = str1.concat(str2);
    System.out.println("String concat using String concat method : " + str4);

    //3. Using StringBuffer.append method
    String str5 = new StringBuffer().append(str1).append(str2).toString();
    System.out.println("String concat using StringBuffer append method : " + str5);
  }
}

/*
 Output of Java String concat example would be
 String concat using + operator : Hello World
 String concat using String concat method : Hello World
 String concat using StringBuffer append method : Hello World
 */
