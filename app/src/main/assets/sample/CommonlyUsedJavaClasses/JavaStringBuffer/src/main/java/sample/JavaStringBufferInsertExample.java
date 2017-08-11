package sample;
/*
   Java StringBuffer insert method Example
   This example shows how a value can be inserted in to StringBuffer object.
*/

public class JavaStringBufferInsertExample {

  public static void main(String[] args) {
    /*
     Java StringBuffer class provides following methods to insert various
     primitive values and objects to StringBuffer object at specified offset.
    */

    /*
     StringBuffer insert(int offset, boolean b) method inserts
     boolean to StringBuffer object at specified offset
    */
    boolean b = true;
    StringBuffer sb1 = new StringBuffer("Hello  World");
    sb1.insert(6, b);
    System.out.println(sb1);

    /*
     StringBuffer insert(int offset, char c) method inserts
     character to StringBuffer object at specified offset
    */
    char c = 'Y';
    StringBuffer sb2 = new StringBuffer("Hello  World");
    sb2.insert(6, c);
    System.out.println(sb2);

    /*
     StringBuffer insert(int offset, char[] c1) method inserts
     character array to StringBuffer object at specified offset
    */
    char[] c1 = new char[]{'Y', 'e', 's'};
    StringBuffer sb3 = new StringBuffer("Hello  World");
    sb3.insert(6, c1);
    System.out.println(sb3);

    /*
     StringBuffer insert(int offset, double d) method inserts
     double to StringBuffer object at specified offset
    */
    double d = 1.0;
    StringBuffer sb4 = new StringBuffer("Hello  World");
    sb4.insert(6, d);
    System.out.println(sb4);

    /*
     StringBuffer insert(int offset, float f) method inserts
     float to StringBuffer object at specified offset
    */
    float f = 2.0f;
    StringBuffer sb5 = new StringBuffer("Hello  World");
    sb5.insert(6, f);
    System.out.println(sb5);

    /*
     StringBuffer insert(int offset, int i) method inserts
     integer to StringBuffer object at specified offset
    */
    int i = 5;
    StringBuffer sb6 = new StringBuffer("Hello  World");
    sb6.insert(6, i);
    System.out.println(sb6);

    /*
     StringBuffer insert(int offset, long l) method inserts
     long to StringBuffer object at specified offset
    */
    long l = 10;
    StringBuffer sb7 = new StringBuffer("Hello  World");
    sb7.insert(6, l);
    System.out.println(sb7);

    /*
     StringBuffer insert(int offset, Object obj) method inserts
     Object to StringBuffer object at specified offset
    */
    Object obj = new String("My");
    StringBuffer sb8 = new StringBuffer("Hello  World");
    sb8.insert(6, obj);
    System.out.println(sb8);

    /*
     StringBuffer insert(int offset, String str) method inserts
     String to StringBuffer object at specified offset
    */
    String str = "New";
    StringBuffer sb9 = new StringBuffer("Hello  World");
    sb9.insert(6, str);
    System.out.println(sb9);

    /*
     NOTE: Above all method throws StringIndexOutOfBoundsException if the
     offset is less than 0 or grater than length of StringBuffer object.
    */
  }
}

/*
 Output would be

 Hello true World
 Hello Y World
 Hello Yes World
 Hello 1.0 World
 Hello 2.0 World
 Hello 5 World
 Hello 10 World
 Hello My World
 Hello New World

 */
