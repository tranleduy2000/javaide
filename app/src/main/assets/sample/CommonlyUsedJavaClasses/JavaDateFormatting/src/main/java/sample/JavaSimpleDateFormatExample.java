package sample;
/*
Java SimpleDateFormat example.
This Java SimpleDateFormat example describes how class is defined and being used
in Java language.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaSimpleDateFormatExample {

  public static void main(String args[]) {

    // Create Date object.
    Date date = new Date();
    //Specify the desired date format
    String DATE_FORMAT = "MM/dd/yyyy";
    //Create object of SimpleDateFormat and pass the desired date format.
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    /*
    Use format method of SimpleDateFormat class to format the date.
    */
    System.out.println("Today is " + sdf.format(date));
  }
}

/*
OUTPUT of the above given Java SimpleDateFormat Example would be :
Today is 02/06/2005
*/
