package sample;
/*
  Get current TimeZone using Java Calendar
  This example shows how to get current TimeZone using getTimeZone method
  of Java Calendar class.

*/

import java.util.Calendar;
import java.util.TimeZone;

public class GetCurrentTimeZone {

  public static void main(String[] args) {

    //get Calendar instance
    Calendar now = Calendar.getInstance();

    //get current TimeZone using getTimeZone method of Calendar class
    TimeZone timeZone = now.getTimeZone();

    //display current TimeZone using getDisplayName() method of TimeZone class
    System.out.println("Current TimeZone is : " + timeZone.getDisplayName());
  }
}

/*
Typical output would be
Current TimeZone is : Eastern Standard Time
*/
