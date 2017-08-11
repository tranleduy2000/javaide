package sample;
/*
        Java char Example
        This Java Example shows how to declare and use Java primitive char variable
        inside a java class.
*/

public class JavaCharExample {

  public static void main(String[] args) {

    /*
     * char is 16 bit type and used to represent Unicode characters.
     * Range of char is 0 to 65,536.
     *
     * Declare char varibale as below
     *
     * char <variable name> = <default value>;
     *
     * here assigning default value is optional.
     */

    char ch1 = 'a';
    char ch2 = 65; /* ASCII code of 'A'*/

    System.out.println("Value of char variable ch1 is :" + ch1);
    System.out.println("Value of char variable ch2 is :" + ch2);
  }
}

/*
 Output would be
 Value of char variable ch1 is :a
 Value of char variable ch2 is :A
 */
