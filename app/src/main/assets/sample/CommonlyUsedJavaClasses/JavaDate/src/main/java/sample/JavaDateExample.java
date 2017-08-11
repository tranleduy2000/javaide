package sample;
/*
Java Date example.
This Java Date example describes how Java Date class is being used in Java language.
*/

import java.util.Date;

/*
Most of the methods of the Java Date class have been depricated.
Java Calendar class should be used for date manipulation instead.
*/

public class JavaDateExample {

  public static void main(String args[]) {
    /*
    Create date object with current date and time.
    */

    Date date = new Date();
    System.out.println("Today is " + date);
  }
}

/*
OUTPUT of the above given Java Date Example would be :
Today is Sat Feb 04 18:10:21 IST 2005
*/
