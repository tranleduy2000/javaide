package sample;
/*
  Convert Byte object to String object
  This example shows how a Byte object can be converted into String object.
*/

public class ByteToStringExample {

  public static void main(String[] args) {
    Byte bObj = new Byte("10");

    //use toString method of Byte class to convert Byte into String.
    String str = bObj.toString();
    System.out.println("Byte converted to String as " + str);
  }
}

/*
 Output of the program would be
 Byte converted to String as 10
 */
