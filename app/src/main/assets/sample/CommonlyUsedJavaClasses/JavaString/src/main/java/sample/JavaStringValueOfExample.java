package sample;
/*
Java String valueOf example.
This Java String valueOf example describes how various java primitives and Object
are converted to Java String object using String valueOf method.
*/

public class JavaStringValueOfExample {

  public static void main(String args[]) {

    /*
    Java String class defines following methods to convert various Java primitives to
    Java String object.
    1) static String valueOf(int i)
    Converts argument int to String and returns new String object representing
    argument int.
    2) static String valueOf(float f)
    Converts argument float to String and returns new String object representing
    argument float.
    3) static String valueOf(long l)
    Converts argument long to String and returns new String object representing
    argument long.
    4) static String valueOf(double i)
    Converts argument double to String and returns new String object representing
    argument double.
    5) static String valueOf(char c)
    Converts argument char to String and returns new String object representing
    argument char.
    6) static String valueOf(boolean b)
    Converts argument boolean to String and returns new String object representing
    argument boolean.
    7) static String valueOf(Object o)
    Converts argument Object to String and returns new String object representing
    argument Object.
    */

    int i = 10;
    float f = 10.0f;
    long l = 10;
    double d = 10.0d;
    char c = 'a';
    boolean b = true;
    Object o = new String("Hello World");

    /* convert int to String */
    System.out.println(String.valueOf(i));
    /* convert float to String */
    System.out.println(String.valueOf(f));
    /* convert long to String */
    System.out.println(String.valueOf(l));
    /* convert double to String */
    System.out.println(String.valueOf(d));
    /* convert char to String */
    System.out.println(String.valueOf(c));
    /* convert boolean to String */
    System.out.println(String.valueOf(b));
    /* convert Object to String */
    System.out.println(String.valueOf(o));
  }
}

/*
 OUTPUT of the above given Java String valueOf Example would be :
 10
 10.0
 10
 10.0
 a
 true
 true
 Hello World
 */
