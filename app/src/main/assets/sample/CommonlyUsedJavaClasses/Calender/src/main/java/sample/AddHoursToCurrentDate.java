package sample;
/*
  Add or substract hours to current time using Java Calendar
  This example shows how to add or substract hours in current time
  using Java Calendar class.
*/

import java.util.Calendar;

public class AddHoursToCurrentDate {

  public static void main(String[] args) {

    //create Calendar instance
    Calendar now = Calendar.getInstance();

    System.out.println(
            "Current Date : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));

    System.out.println(
            "Current time : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));

    //add hours to current date using Calendar.add method
    now.add(Calendar.HOUR, 10);

    System.out.println(
            "New time after adding 10 hours : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));

    /*
     * Java Calendar class automatically adjust the date accordingly if adding
     * hours to the current time causes current date to be changed.
     */

    System.out.println(
            "New date after adding 10 hours : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));

    //substract hours from current date using Calendar.add method
    now = Calendar.getInstance();
    now.add(Calendar.HOUR, -3);

    System.out.println(
            "Time before 3 hours : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));
  }
}

/*
Typical output would be
Current Date : 12-25-2007
Current time : 18:31:42
New time after adding 10 hours : 4:31:42
New date after adding 10 hours : 12-26-2007
Time before 3 hours : 15:31:42
*/
