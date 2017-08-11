package sample;
/*
  Convert Boolean Object to boolean primitive exmaple
  This example show how a Boolean object can be converted to a boolean primitive
  type.
*/

public class JavaBooleanTobooleanExample {

  public static void main(String[] args) {
    //Construct a Boolean object.
    Boolean blnObj = new Boolean("true");

    //use booleanValue of Boolean class to convert it into boolean primitive
    boolean b = blnObj.booleanValue();
    System.out.println(b);
  }
}

/*
 Output would be :
 true
 */
