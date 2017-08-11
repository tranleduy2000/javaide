package sample;
/*
  Add or substract seconds to current time using Java Calendar
  This example shows how to add or substract seconds in current time
  using Java Calendar class.
*/

import java.util.Calendar;

public class AddSecondsToCurrentDate {

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

    //add seconds to current date using Calendar.add method
    now.add(Calendar.SECOND, 100);

    System.out.println(
            "New time after adding 100 seconds : "
                    + now.get(Calendar.HOUR_OF_DAY)
                    + ":"
                    + now.get(Calendar.MINUTE)
                    + ":"
                    + now.get(Calendar.SECOND));

    /*
     * Java Calendar class automatically adjust the date,hour or minutesaccordingly
     * if adding seconds to the current time causes current minute,
     * hour or date to be changed.
     */

    //substract seconds from current time using Calendar.add method
    now = Calendar.getInstance();
    now.add(Calendar.SECOND, -50);

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
Current time : 16:37:13
New time after adding 100 seconds : 16:38:53
Time before 50 minutes : 16:36:23
*/
