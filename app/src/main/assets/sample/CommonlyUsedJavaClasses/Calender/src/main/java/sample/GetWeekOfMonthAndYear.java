package sample;
/*
  Get Week of month and year using Java Calendar
  This example shows how to get current week of month and curent week of year
  using Java Calendar class.
*/

import java.util.Calendar;

public class GetWeekOfMonthAndYear {

  public static void main(String[] args) {

    //create Calendar instance
    Calendar now = Calendar.getInstance();

    System.out.println("Current week of month is : " + now.get(Calendar.WEEK_OF_MONTH));

    System.out.println("Current week of year is : " + now.get(Calendar.WEEK_OF_YEAR));

    now.add(Calendar.WEEK_OF_MONTH, 1);
    System.out.println(
            "date after one year : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));
  }
}

/*
Typical output would be
Current week of month is : 5
Current week of year is : 52
*/
