package sample;
/*
  Find ceiling value of a number using Math.ceil
  This java example shows how to find a ceiling value of a number using ceil method
  of Java Math class. ceil method returns the smallest interger which is not
  less than the value.
*/

public class FindCeilingExample {

  public static void main(String[] args) {

    /*
     * To find a ceiling value, use
     * static double ceil(double d) method of Math class.
     *
     * It returns a smallest integer which is not less than the argument value.
     */

    //Returns the same value
    System.out.println(Math.ceil(10));

    //returns a smallest integer which is not less than 10.1, i.e. 11
    System.out.println(Math.ceil(10.1));

    //returns a smallest integer which is not less than 5.5, i.e. 6
    System.out.println(Math.ceil(5.5));

    //in this case it would be -20
    System.out.println(Math.ceil(-20));

    //it returns -42 not -43. (-42 is grater than 42.4 :) )
    System.out.println(Math.ceil(-42.4));

    //returns 0
    System.out.println(Math.ceil(0));
  }
}

/*
 Output would be
 10.0
 11.0
 6.0
 -20.0
 -42.0
 0.0
 */
