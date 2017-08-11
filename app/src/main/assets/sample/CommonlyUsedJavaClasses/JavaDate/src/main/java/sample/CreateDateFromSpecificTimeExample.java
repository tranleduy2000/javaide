package sample;
/*
  Create java Date from specific time example.
  This example shows how to create a java Date object for specific date and time.
*/

import java.util.Date;

public class CreateDateFromSpecificTimeExample {

  public static void main(String[] args) {

    /*
     To create a Date object for sepefic date and time use
     Date(long date) constuctor where date is the number of milliseconds
     since January 1, 1970, 00:00:00 GMT.
    */

    //date after one year of January 1, 1970, 00:00:00 GMT
    Date d = new Date(365L * 24L * 60L * 60L * 1000L);
    System.out.println(d);
  }
}

/*
TYPICAL Output Would be
Thu Jan 01 00:00:00 EST 1971
*/
