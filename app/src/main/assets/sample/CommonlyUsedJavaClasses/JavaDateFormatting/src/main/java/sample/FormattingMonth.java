package sample;
/*
  Formatting month using SimpleDateFormat
  This example shows how to format month using Java SimpleDateFormat class. Month can
  be formatted in M, MM, MMM and MMMM formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingMonth {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting month in M format like 1,2 etc
    String strDateFormat = "M";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("Current Month in M format : " + sdf.format(date));

    //formatting Month in MM format like 01, 02 etc.
    strDateFormat = "MM";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("Current Month in MM format : " + sdf.format(date));

    //formatting Month in MMM format like Jan, Feb etc.
    strDateFormat = "MMM";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("Current Month in MMM format : " + sdf.format(date));

    //formatting Month in MMMM format like January, February etc.
    strDateFormat = "MMMM";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("Current Month in MMMM format : " + sdf.format(date));
  }
}

/*
Typical output would be
Current Month in M format : 2
Current Month in MM format : 02
Current Month in MMM format : Feb
Current Month in MMMM format : February
*/
