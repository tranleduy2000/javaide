package sample;
/*
  Convert Short object to String object
  This example shows how a Short object can be converted into String object.
*/

public class ShortToStringExample {

  public static void main(String[] args) {
    short s = 10;
    Short sObj = new Short(s);

    //use toString method of Short class to convert Short into String.
    String str = sObj.toString();
    System.out.println("Short converted to String as " + str);
  }
}

/*
 Output of the program would be
 Short converted to String as 10
 */
