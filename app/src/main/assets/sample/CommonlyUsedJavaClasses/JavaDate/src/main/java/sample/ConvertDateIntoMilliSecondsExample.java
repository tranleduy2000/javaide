package sample;
/*
  Convert Date into milliseconds example
  This example shows how to get the milliseconds since January 1, 1970, 00:00:00 GMT
  represented by this Date object, using getTime() method of java Date class
*/

import java.util.Date;

public class ConvertDateIntoMilliSecondsExample {

  public static void main(String args[]) {

    //create a Date object
    Date date = new Date();
    System.out.println("Date is : " + date);

    //use getTime() method to retrieve milliseconds
    System.out.println("Milliseconds since January 1, 1970, 00:00:00 GMT : " + date.getTime());
  }
}

/*
TYPICAL Output would be
Date is : Sun Sep 09 19:56:42 EDT 2007
Milliseconds since January 1, 1970, 00:00:00 GMT : 1189382202606
*/
