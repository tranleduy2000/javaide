package sample;
/*
  Formatting hour using SimpleDateFormat
  This example shows how to format hour field using Java SimpleDateFormat class.
  Hour can be formatted in H, HH, h, hh, k, kk, K and KK formats.
*/

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormattingHour {

  public static void main(String[] args) {

    //create Date object
    Date date = new Date();

    //formatting hour in h (1-12 in AM/PM) format like 1, 2..12.
    String strDateFormat = "h";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in h format : " + sdf.format(date));

    //formatting hour in hh (01-12 in AM/PM) format like 01, 02..12.
    strDateFormat = "hh";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in hh format : " + sdf.format(date));

    //formatting hour in H (0-23) format like 0, 1...23.
    strDateFormat = "H";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in H format : " + sdf.format(date));

    //formatting hour in HH (00-23) format like 00, 01..23.
    strDateFormat = "HH";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in HH format : " + sdf.format(date));

    //formatting hour in k (1-24) format like 1, 2..24.
    strDateFormat = "k";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in k format : " + sdf.format(date));

    //formatting hour in kk (01-24) format like 01, 02..24.
    strDateFormat = "kk";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in kk format : " + sdf.format(date));

    //formatting hour in K (0-11 in AM/PM) format like 0, 1..11.
    strDateFormat = "K";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in K format : " + sdf.format(date));

    //formatting hour in KK (00-11) format like 00, 01,..11.
    strDateFormat = "KK";
    sdf = new SimpleDateFormat(strDateFormat);

    System.out.println("hour in KK format : " + sdf.format(date));
  }
}

/*
Typical output would be
hour in h format : 12
hour in hh format : 12
hour in H format : 0
hour in HH format : 00
hour in k format : 24
hour in kk format : 24
hour in K format : 0
hour in KK format : 00
*/
