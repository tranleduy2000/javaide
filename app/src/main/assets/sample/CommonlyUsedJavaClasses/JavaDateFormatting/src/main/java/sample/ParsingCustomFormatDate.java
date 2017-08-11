package sample;
/*
  Parsing custom formatted date string into Date object using SimpleDateFormat
  This example shows how to parse string containing date and time in custom formats
  into Date object using Java SimpleDateFormat class.
*/

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParsingCustomFormatDate {

  public static void main(String[] args) {

    //create object of SimpleDateFormat class with custom format
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");

    try {
      //parse string containing specified format into date object
      Date date = sdf.parse("31/12/06");
      System.out.println(date);
    } catch (ParseException pe) {
      System.out.println("Parse Exception : " + pe);
    }
  }
}
