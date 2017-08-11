package sample;

/*
   Convert boolean to Boolean object Example
   This example shows how a java boolean primitive can be converted
   to a Boolean object.
*/
public class BooleanPrimitiveToBooleanObjectExample {

  public static void main(String[] args) {
    boolean b = true;

    //1. using constructor
    Boolean blnObj1 = new Boolean(b);

    //2. using valueOf method of Boolean class. This is a static method.
    Boolean blnObj2 = Boolean.valueOf(b);
  }
}

/*
 Output would be :
 true
 true
 */
