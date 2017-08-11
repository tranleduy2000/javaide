package sample;
/*
        Determine If Year Is Leap Year Java Example
        This Determine If Year Is Leap Year Java Example shows how to
        determine whether the given year is leap year or not.
*/

public class DetermineLeapYearExample {

  public static void main(String[] args) {

    //year we want to check
    int year = 2004;

    //if year is divisible by 4, it is a leap year

    if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0)))
      System.out.println("Year " + year + " is a leap year");
    else System.out.println("Year " + year + " is not a leap year");
  }
}

/*
 Output of the example would be
 Year 2004 is a leap year
 */
