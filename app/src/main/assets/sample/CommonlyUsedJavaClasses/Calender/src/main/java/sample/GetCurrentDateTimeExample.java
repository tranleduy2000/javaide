package sample;
/*
  Get current date time values using Java Calendar
  This example shows how to get current date and time values
  using Java Calendar class.
*/

import java.util.Calendar;

public class GetCurrentDateTimeExample {

  public static void main(String[] args) {

    //get instance of Calendar class
    Calendar now = Calendar.getInstance();

    /*
     * Calendar class has several contstants which represents current date
     * and time values
     */

    //get current date, year and month
    System.out.println("Current Year is : " + now.get(Calendar.YEAR));
    //month start from 0 to 11
    System.out.println("Current Month is : " + (now.get(Calendar.MONTH) + 1));
    System.out.println("Current Date is : " + now.get(Calendar.DATE));

    //get current time information
    System.out.println("Current Hour in 12 hour format is : " + now.get(Calendar.HOUR));
    System.out.println("Current Hour in 24 hour format is : " + now.get(Calendar.HOUR_OF_DAY));
    System.out.println("Current Minute is : " + now.get(Calendar.MINUTE));
    System.out.println("Current Second is : " + now.get(Calendar.SECOND));
    System.out.println("Current Millisecond is : " + now.get(Calendar.MILLISECOND));

    //display full date time
    System.out.println(
            "Current full date time is : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR)
                    + " "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND)
                    + "."
                    + now.get(Calendar.MILLISECOND));
  }
}

/*
Typical output would be
Current Year is : 2007
Current Month is : 12
Current Date is : 25
Current Hour in 12 hour format is : 6
Current Hour in 24 hour format is : 18
Current Minute is : 28
Current Second is : 54
Current Millisecond is : 797
Current full date time is : 12-25-2007 18:28:54.797
*/
