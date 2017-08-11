package sample;
/*
  Display Day of Week using Java Calendar
  This example shows how to display day of week like Monday, Tuesday etc.
  using Java Calendar class.
*/

import java.util.Calendar;

public class DisplayDayOfWeek {

  public static void main(String[] args) {

    //create Calendar instance
    Calendar now = Calendar.getInstance();

    System.out.println(
            "Current date : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));

    //create an array of days
    String[] strDays =
            new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thusday", "Friday", "Saturday"};

    //Day_OF_WEEK starts from 1 while array index starts from 0
    System.out.println("Current day is : " + strDays[now.get(Calendar.DAY_OF_WEEK) - 1]);
  }
}

/*
Typical output would be
Current date : 12-25-2007
Current day is : Tuesday
*/
