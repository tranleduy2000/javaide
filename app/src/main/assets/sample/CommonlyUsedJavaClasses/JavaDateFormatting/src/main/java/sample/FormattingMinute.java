package sample;
/*
  Formatting Minutes using SimpleDateFormat
  This example shows how to format minute field using Java SimpleDateFormat class.
  Minutes can be formatted in either m or mm formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingMinute {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting minute in m format like 1,2 etc.
    String strDateFormat = "m";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("minutes in m format : " + sdf.format(date));

    //formatting minutes in mm format like 01, 02 etc.
    strDateFormat = "mm";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("minutes in mm format : " + sdf.format(date));
  }
}

/*
Typical output would be
minutes in m format : 3
minutes in mm format : 03
*/
