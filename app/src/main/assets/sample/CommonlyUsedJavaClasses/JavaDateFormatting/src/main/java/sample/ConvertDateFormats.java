package sample;
/*
  Convert date string from one format to another format using SimpleDateFormat
  This example shows how to convert format of a string containing date
  and time to other formats using Java SimpleDateFormat class.
*/

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConvertDateFormats {

  public static void main(String[] args) {

    //string containing date in one format
    String strDate = "12/12/07";

    try {
      //create SimpleDateFormat object with source string date format
      SimpleDateFormat sdfSource = new SimpleDateFormat("dd/MM/yy");

      //parse the string into Date object
      Date date = sdfSource.parse(strDate);

      //create SimpleDateFormat object with desired date format
      SimpleDateFormat sdfDestination = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");

      //parse the date into another format
      strDate = sdfDestination.format(date);

      System.out.println("Date is converted from dd/MM/yy format to MM-dd-yyyy hh:mm:ss");
      System.out.println("Converted date is : " + strDate);

    } catch (ParseException pe) {
      System.out.println("Parse Exception : " + pe);
    }
  }
}

/*
Typical output would be
Date is converted from dd/MM/yy format to MM-dd-yyyy hh:mm:ss
Converted date is : 12-12-2007 12:00:00
*/
