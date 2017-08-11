package sample;
/*
  Formatting year using SimpleDateFormat
  This example shows how to format year using Java SimpleDateFormat class. Year can
  be formatted in either YY or YYYY formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingYear {

  public static void main(String[] args) {
    //create Date object
    Date date = new Date();

    //formatting year in yy format like 07, 08 etc
    String strDateFormat = "yy";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("Current year in yy format : " + sdf.format(date));

    //formatting year in yyyy format like 2007, 2008 etc.
    strDateFormat = "yyyy";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("Current year in yyyy format : " + sdf.format(date));
  }
}

/*
Typical output would be
Year in yy format : 07
Year in yyyy format : 2007
*/
