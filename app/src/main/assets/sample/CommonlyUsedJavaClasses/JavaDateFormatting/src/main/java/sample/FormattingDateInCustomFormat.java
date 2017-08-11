package sample;
/*
  Formatting date in custom formats using SimpleDateFormat
  This example shows how to format date and time in custom formats using Java
  SimpleDateFormat class.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingDateInCustomFormat {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //create object of SimpleDateFormat class with custom format
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    String strDate = sdf.format(date);
    System.out.println("formatted date in mm/dd/yy : " + strDate);

    //format date in dd/mm/yyyy format
    sdf = new SimpleDateFormat("dd/MM/yyyy");
    strDate = sdf.format(date);
    System.out.println("formatted date in dd/MM/yyyy : " + strDate);

    //format date in mm-dd-yyyy hh:mm:ss format
    sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
    strDate = sdf.format(date);
    System.out.println("formatted date in mm-dd-yyyy hh:mm:ss : " + strDate);
  }
}

/*
Typical output would be
formatted date in mm/dd/yy : 12/27/07
formatted date in dd/MM/yyyy : 27/12/2007
formatted date in mm-dd-yyyy hh:mm:ss : 12-27-2007 06:44:26
*/
