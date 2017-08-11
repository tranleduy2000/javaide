package sample;
/*
        Java byte Example
        This Java Example shows how to declare and use Java primitive byte variable
        inside a java class.
*/

public class JavaByteExample {

  public static void main(String[] args) {

    /*
     * byte is smallest Java integer type.
     * byte is 8 bit signed type ranges from –128 to 127.
     * byte is mostly used when dealing with raw data like reading a
     * binary file.
     *
     * Declare byte varibale as below
     *
     * byte <variable name> = <default value>;
     *
     * here assigning default value is optional.
     */

    byte b1 = 100;
    byte b2 = 20;

    System.out.println("Value of byte variable b1 is :" + b1);
    System.out.println("Value of byte variable b1 is :" + b2);
  }
}

/*
 Output would be
 Value of byte variable b1 is :100
 Value of byte variable b1 is :20
 */
