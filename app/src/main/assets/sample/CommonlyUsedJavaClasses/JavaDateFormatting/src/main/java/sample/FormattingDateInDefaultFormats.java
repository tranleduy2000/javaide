package sample;
/*
  Formatting date in default formats using DateFormat
  This example shows how to format date and time using default formats using Java
  DateFormat class.  Predefined date formats are short, default or medium,
  long and full.
*/

import java.text.DateFormat;
import java.util.Date;

public class FormattingDateInDefaultFormats {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formats date in Short format that consists of numbers like 11/25/06
    String strDate = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
    System.out.println(strDate);

    //formats date in Medium format
    strDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    /*
     * We can also use default format instead of MEDIUM like given below
     * strDate = DateFormat.getDateInstance().format(date);
     * OR
     * strDate = DateFormat.getDateInstance(DateFormat.DEFAULT).format(date);
     */
    System.out.println(strDate);

    //formats date in Long format
    strDate = DateFormat.getDateInstance(DateFormat.LONG).format(date);
    System.out.println(strDate);

    //formats date in Full format
    strDate = DateFormat.getDateInstance(DateFormat.FULL).format(date);
    System.out.println(strDate);
  }
}

/*
Typical output would be
12/27/07
Dec 27, 2007
December 27, 2007
Thursday, December 27, 2007
*/
