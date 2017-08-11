package sample;
/*
  Compare two Java Date objects using compareTo method example.
  This example shows how to compare two java Date objects using compareTo method of
  java Date Class.
*/

import java.util.Date;

public class CompareDateUsingCompareToExample {

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

    System.out.println("First Date : " + d1);
    System.out.println("Second Date : " + d2);

    /*
     Use compareTo method of java Date class to compare two date objects.
     compareTo returns value grater than 0 if first date is after another date,
     returns value less than 0 if first date is before another date and returns
     0 if both dates are equal.
    */

    int results = d1.compareTo(d2);

    if (results > 0) System.out.println("First Date is after second");
    else if (results < 0) System.out.println("First Date is before second");
    else System.out.println("Both dates are equal");
  }
}

/*
TYPICAL Output Would be
First Date : Sun Sep 09 19:50:32 EDT 2007
Second Date : Sun Sep 09 19:50:32 EDT 2007
First Date is before second
*/
