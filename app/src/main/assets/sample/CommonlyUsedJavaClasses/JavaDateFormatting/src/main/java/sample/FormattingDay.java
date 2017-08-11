package sample;
/*
  Formatting day using SimpleDateFormat
  This example shows how to format day using Java SimpleDateFormat class. Day can
  be formatted in either d or dd formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingDay {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting day in d format like 1,2 etc
    String strDateFormat = "d";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("Current day in d format : " + sdf.format(date));

    //formatting day in dd format like 01, 02 etc.
    strDateFormat = "dd";
    sdf = new SimpleDateFormat(strDateFormat);
    System.out.println("Current day in dd format : " + sdf.format(date));
  }
}

/*
Typical output would be
Current day in d format : 3
Current day in dd format : 03
*/
