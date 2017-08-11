package sample;
/*
  Add or substract weeks to current date using Java Calendar
  This example shows how to add or substract weeks in current date and time values
  using Java Calendar class.
*/

import java.util.Calendar;

public class AddWeeksToCurrentWeek {

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

    System.out.println("Current week of month is : " + now.get(Calendar.WEEK_OF_MONTH));

    System.out.println("Current week of year is : " + now.get(Calendar.WEEK_OF_YEAR));

    //add week to current date using Calendar.add method
    now.add(Calendar.WEEK_OF_YEAR, 1);

    System.out.println(
            "date after one week : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));

    //substract week from current date
    now = Calendar.getInstance();
    now.add(Calendar.WEEK_OF_YEAR, -50);
    System.out.println(
            "date before 50 weeks : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));
  }
}

/*
Typical output would be
Current date : 12-25-2007
Current week of month is : 5
Current week of year is : 52
date after one week : 1-1-2008
date before 50 weeks : 1-9-2007
*/
