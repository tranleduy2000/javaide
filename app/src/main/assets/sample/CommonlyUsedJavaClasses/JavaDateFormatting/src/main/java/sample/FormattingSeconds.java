package sample;
/*
  Formatting seconds using SimpleDateFormat
  This example shows how to format second field using Java SimpleDateFormat class.
  Seconds can be formatted in either s or ss formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingSeconds {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting seconds in s format like 1,2 etc.
    String strDateFormat = "s";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("seconds in s format : " + sdf.format(date));

    //formatting seconds in ss format like 01, 02 etc.
    strDateFormat = "ss";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("seconds in ss format : " + sdf.format(date));
  }
}

/*
Typical output would be
seconds in s format : 9
seconds in ss format : 09
*/
