package sample;
/*
  Compare date time using before method of Java Calendar
  This example shows how to compare two calendar objects having different
   date and time using
  before method of Java Calendar class.
*/

import java.util.Calendar;

public class CompareDateTimesUsingBefore {

  public static void main(String[] args) {

    //create first Calendar object
    Calendar old = Calendar.getInstance();

    //set it to some old date
    old.set(Calendar.YEAR, 1990);

    //create second Calendar object
    Calendar now = Calendar.getInstance();

    /*
     * To compare two different Calendar objects, use
     * boolean before(Caledar anotherCal) method.
     *
     * If the first Calendar object's date and time is before
     * anotherCal date and time,
     * it returns true, false otherwise.
     */

    System.out.println("Is old before now ? : " + old.before(now));
  }
}

/*
Output would be
Is old before now ? : true
*/
