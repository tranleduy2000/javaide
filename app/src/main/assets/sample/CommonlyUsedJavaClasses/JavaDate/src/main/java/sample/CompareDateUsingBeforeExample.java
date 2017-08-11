package sample;
/*
  Compare two Java Date objects using before method example.
  This example shows how to compare two java Date objects using before method of
  java Date Class.
*/

import java.util.Date;

public class CompareDateUsingBeforeExample {

  public static void main(String[] args) {

    //create first date object
    Date d1 = new Date();

    //make interval of 10 millisecond before creating second date object
    try {
      Thread.sleep(10);
    } catch (Exception e) {
    }

    //create second date object
    Date d2 = new Date();

    //use boolean before(Date anotherDate) method of Date Class to
    //check whether a date is before the specified date

    System.out.println("First Date : " + d1);
    System.out.println("Second Date : " + d2);
    System.out.println("Is first date before second ? : " + d1.before(d2));
  }
}

/*
TYPICAL Output Would be
First Date : Sun Sep 09 19:40:44 EDT 2007
Second Date : Sun Sep 09 19:40:44 EDT 2007
Is first date before second ? : true
*/
