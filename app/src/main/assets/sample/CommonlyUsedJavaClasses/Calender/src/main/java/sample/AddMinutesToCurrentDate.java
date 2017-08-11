package sample;
/*
  Add or substract minutes to current time using Java Calendar
  This example shows how to add or substract minutes in current time
  using Java Calendar class.
*/

import java.util.Calendar;

public class AddMinutesToCurrentDate {

  public static void main(String[] args) {

    //create Calendar instance
    Calendar now = Calendar.getInstance();

    System.out.println(
            "Current time : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));

    //add minutes to current date using Calendar.add method
    now.add(Calendar.MINUTE, 20);

    System.out.println(
            "New time after adding 20 minutes : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));

    /*
         * Java Calendar class automatically adjust the date or hour accordingly
    if adding minutes to the current time causes current hour or date to be changed.
         */

    //substract minutes from current date using Calendar.add method
    now = Calendar.getInstance();
    now.add(Calendar.MINUTE, -50);

    System.out.println(
            "Time before 50 minutes : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));
  }
}

/*
Typical output would be
Current time : 16:34:11
New time after adding 20 minutes : 16:54:11
Time before 25 minutes : 15:44:11
*/
