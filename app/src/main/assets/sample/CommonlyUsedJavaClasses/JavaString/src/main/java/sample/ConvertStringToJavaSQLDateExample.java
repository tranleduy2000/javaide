package sample;
/*
       Java String to java.sql.Date Example
       This Java String to java.sql.Date example shows how to convert Java String object
       containing date to java.sql.Date object.
*/

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ConvertStringToJavaSQLDateExample {

  public static void main(String args[]) throws ParseException {

    //string containing date
    String strDate = "2011-12-31 00:00:00";

    /*
     * To convert String to java.sql.Date, use
     * Date (long date) constructor.
     *
     * It creates java.sql.Date object from the milliseconds provided.
     */

    //first convert string to java.util.Date object using SimpleDateFormat
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    java.util.Date date = sdf.parse(strDate);

    java.sql.Date sqlDate = new Date(date.getTime());

    System.out.println("String converted to java.sql.Date :" + sqlDate);
  }
}

/*
 Output of above given String to java.sql.Date example would be
 2011-01-31
 */
