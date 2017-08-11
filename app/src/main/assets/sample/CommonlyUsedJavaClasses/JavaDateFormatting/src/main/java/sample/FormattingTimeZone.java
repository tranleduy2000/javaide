package sample;
/*
  Formatting TimeZone using SimpleDateFormat
  This example shows how to format TimeZone using Java SimpleDateFormat class.
  TimeZone can be formatted in either z, zzzz or Z formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingTimeZone {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting TimeZone in z (General time zone) format like EST.
    String strDateFormat = "zzz";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("TimeZone in z format : " + sdf.format(date));

    //formatting TimeZone in zzzz format Eastern Standard Time.
    strDateFormat = "zzzz";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("TimeZone in zzzz format : " + sdf.format(date));

    //formatting TimeZone in Z (RFC 822) format like -8000.
    strDateFormat = "Z";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("TimeZone in Z format : " + sdf.format(date));
  }
}

/*
Typical output would be
TimeZone in z format : EST
TimeZone in zzzz format : Eastern Standard Time
TimeZone in Z format : -0500
*/
