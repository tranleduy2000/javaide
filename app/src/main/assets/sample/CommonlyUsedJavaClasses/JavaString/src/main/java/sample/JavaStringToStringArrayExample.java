package sample;
/*
 Java String to String Array Example
 This Java String to String Array example shows how to convert String object to String array in
 Java using split method.
*/

public class JavaStringToStringArrayExample {

  public static void main(String args[]) {

    //String which we want to convert to String array
    String str = "Java String to String Array Example";

    /*
     * To convert String object to String array, first thing
     * we need to consider is to how we want to create array.
     *
     * In this example, array will be created by words contained
     * in the original String object. So, first element of array
     * will contain "java", second will contain "String" and so on.
     *
     * To convert String to String array, use
     * String[] split(String delimiter) method of Java String
     * class as given below.
     */

    String strArray[] = str.split(" ");

    System.out.println("String converted to String array");

    //print elements of String array
    for (int i = 0; i < strArray.length; i++) {
      System.out.println(strArray[i]);
    }
  }
}

/*
 Output of Java String to String Array Example would be
 String converted to String array
 Java
 String
 to
 String
 Array
 Example
 */
