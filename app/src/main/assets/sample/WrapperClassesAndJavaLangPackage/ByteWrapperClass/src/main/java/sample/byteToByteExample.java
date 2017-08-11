package sample;
/*
  Convert byte to Byte object Example
  This example shows how a byte primitive value can be converted to Byte object
*/

public class byteToByteExample {

  public static void main(String[] args) {
    byte i = 10;

    /*
    Use Byte constructor to convert byte primitive type to Byte object.
    */

    Byte bObj = new Byte(i);
    System.out.println(bObj);
  }
}

/*
 Output of the program would be
 10
 */
