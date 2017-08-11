package sample;
/*
  Add AM/PM to time using SimpleDateFormat
  This example shows how to format time to have a AM/PM text using Java
  SimpleDateFormat class.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddAMPMToFormattedDate {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting time to have AM/PM text using 'a' format
    String strDateFormat = "HH:mm:ss a";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("Time with AM/PM field : " + sdf.format(date));
  }
}

/*
Typical output would be
Time with AM/PM field : 01:39:42 AM
*/
