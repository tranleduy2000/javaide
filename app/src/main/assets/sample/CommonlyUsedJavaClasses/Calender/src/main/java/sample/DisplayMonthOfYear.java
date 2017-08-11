package sample;
/*
  Display Month of year using Java Calendar
  This example shows how to display a month like January, February etc.
  using Java Calendar class.
*/

import java.util.Calendar;

public class DisplayMonthOfYear {

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

    //create an array of months
    String[] strMonths =
            new String[]{
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            };

    System.out.println("Current month is : " + strMonths[now.get(Calendar.MONTH)]);
  }
}

/*
Typical output would be
Current date : 12-25-2007
Current month is : Dec
*/
