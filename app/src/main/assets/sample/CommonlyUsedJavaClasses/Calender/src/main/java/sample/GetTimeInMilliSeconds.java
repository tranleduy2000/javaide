package sample;
/*
  Get time in milliseconds using Java Calendar
  This example shows how to get time in milliseconds using getTimeInMillis method
  of Java Calendar class.

*/

import java.util.Calendar;

public class GetTimeInMilliSeconds {

  public static void main(String[] args) {

    //get Calendar instance
    Calendar now = Calendar.getInstance();

    /*
     * To get time in milliseconds, use
     * long getTimeInMillis() method of Java Calendar class.
     *
     * It returns millseconds from Jan 1, 1970.
     */
    System.out.println("Current milliseconds since Jan 1, 1970 are :" + now.getTimeInMillis());
  }
}

/*
Typical output would be
Current milliseconds since Jan 1, 1970 are :1198628984102
*/
