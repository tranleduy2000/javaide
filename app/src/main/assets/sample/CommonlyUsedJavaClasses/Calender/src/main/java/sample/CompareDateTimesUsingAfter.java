package sample;
/*
  Compare date time using after method of Java Calendar
  This example shows how to compare two calendar objects having
  different date and time using
  after method of Java Calendar class.
*/

import java.util.Calendar;

public class CompareDateTimesUsingAfter {

  public static void main(String[] args) {

    //create first Calendar object
    Calendar futureCal = Calendar.getInstance();

    //set it to some future date
    futureCal.set(Calendar.YEAR, 2010);

    //create second Calendar object
    Calendar now = Calendar.getInstance();

    /*
     * To compare two different Calendar objects, use
     * boolean after(Caledar anotherCal) method.
     *
     * If the first Calendar object's date and time is after
     * anotherCal's date and time,
     * it returns true, false otherwise.
     */

    System.out.println(
            "Current date : "
                    + (now.get(Calendar.MONTH) + 1)
                    + "-"
                    + now.get(Calendar.DATE)
                    + "-"
                    + now.get(Calendar.YEAR));

    System.out.println("Is futureCal after now ? : " + futureCal.after(now));
  }
}

/*
Typical output would be
Current date : 12-25-2007
Is futureCal after now ? : true
*/
