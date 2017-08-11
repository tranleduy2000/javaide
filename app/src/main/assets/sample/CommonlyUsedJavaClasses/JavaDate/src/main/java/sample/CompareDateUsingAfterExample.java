package sample;
/*
  Compare two Java Date objects using after method example.
  This example shows how to compare two java Date objects using after method of
  java Date Class.
*/

import java.util.Date;

public class CompareDateUsingAfterExample {

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

    //use boolean after(Date anotherDate) method of Date Class to
    //check whether a date is after the specified date

    System.out.println("First Date : " + d1);
    System.out.println("Second Date : " + d2);
    System.out.println("Is second date after first ? : " + d2.after(d1));
  }
}

/*
TYPICAL Output Would be
First Date : Sun Sep 09 19:43:12 EDT 2007
Second Date : Sun Sep 09 19:43:12 EDT 2007
Is second date after first ? : true
*/
