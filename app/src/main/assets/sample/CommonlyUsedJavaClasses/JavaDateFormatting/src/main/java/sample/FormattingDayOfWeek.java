package sample;
/*
  Formatting day of week using SimpleDateFormat
  This example shows how to format day of week using Java SimpleDateFormat class.
  Day of week can be formatted in either E or EEEE formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingDayOfWeek {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting day of week in E format like Sun, Mon etc.
    String strDateFormat = "E";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("Current day of week in E format : " + sdf.format(date));

    //formatting day of week in EEEE format like Sunday, Monday etc.
    strDateFormat = "EEEE";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("Current day of week in EEEE format : " + sdf.format(date));
  }
}

/*
Typical output would be
Current day of week in E format : Sat
Current day of week in EEEE format : Saturday
*/
