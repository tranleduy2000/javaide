package sample;
/*
   Java StringBuffer append method Example
   This example shows how a value can be appended in to StringBuffer object.
*/

public class JavaStringBufferAppendExample {

  public static void main(String[] args) {

    /*
    Java StringBuffer class provides following methods to append various
    primitive values and objects to StringBuffer object.
    */

    //StringBuffer append(boolean b) method appends boolean to StringBuffer object
    boolean b = true;
    StringBuffer sb1 = new StringBuffer("BooelanAppended : ");
    sb1.append(b);
    System.out.println(sb1);

    //StringBuffer append(char c) method appends character to StringBuffer object
    char c = 'Y';
    StringBuffer sb2 = new StringBuffer("CharAppended : ");
    sb2.append(c);
    System.out.println(sb2);

    /*StringBuffer append(char[] c) method appends character array
    to StringBuffer object*/
    char[] c1 = new char[]{'Y', 'e', 's'};
    StringBuffer sb3 = new StringBuffer("Character Array Appended : ");
    sb3.append(c1);
    System.out.println(sb3);

    //StringBuffer append(double d) method appends double to StringBuffer object
    double d = 1.0;
    StringBuffer sb4 = new StringBuffer("doubleAppended : ");
    sb4.append(d);
    System.out.println(sb4);

    //StringBuffer append(float f) method appends float to StringBuffer object
    float f = 1.0f;
    StringBuffer sb5 = new StringBuffer("floatAppended : ");
    sb5.append(f);
    System.out.println(sb5);

    //StringBuffer append(int i) method appends integer to StringBuffer object
    int i = 1;
    StringBuffer sb6 = new StringBuffer("integerAppended : ");
    sb6.append(i);
    System.out.println(sb6);

    //StringBuffer append(long l) method appends long to StringBuffer object
    long l = 1;
    StringBuffer sb7 = new StringBuffer("longAppended : ");
    sb7.append(l);
    System.out.println(sb7);

    //StringBuffer append(Object o) method appends Object to StringBuffer object
    /* NOTE : Objects are first converted to a String and then it is
    appended to StrinBuffer */
    Object obj = new String("Yes");
    StringBuffer sb8 = new StringBuffer("ObjectAppended : ");
    sb8.append(obj);
    System.out.println(sb8);

    //StringBuffer append(String str) method appends String to StringBuffer object
    String str = new String("Yes");
    StringBuffer sb9 = new StringBuffer("StringAppended : ");
    sb9.append(str);
    System.out.println(sb9);
  }
}

/*
 Output Would be

 BooelanAppended : true
 CharAppended : Y
 Character Array Appended : Yes
 doubleAppended : 1.0
 floatAppended : 1.0
 integerAppended : 1
 longAppended : 1
 ObjectAppended : Yes
 StringAppended : Yes

 */
