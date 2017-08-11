package sample;
/*
  Add or substract months to current date using Java Calendar
  This example shows how to add or substract months in current date and time values
  using Java Calendar class.
*/

import java.util.Calendar;

public class AddMonthsToCurrentDate {

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

    //add months to current date using Calendar.add method
    now.add(Calendar.MONTH, 10);

    System.out.println(
            "date after 10 months : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));

    //substract months from current date using Calendar.add method
    now = Calendar.getInstance();
    now.add(Calendar.MONTH, -5);

    System.out.println(
            "date before 5 months : "
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
date after 10 months : 10-25-2008
date before 5 months : 7-25-2007
*/
