package sample;
/*
  Generate random numbers using Math.random
  This java example shows how to generate random numbers using random method of
  Java Math class.
*/

public class GenerateRandomNumbers {

  public static void main(String[] args) {

    /*
     * To generate random numbers, use
     * static double random() method of Java Math class.
     *
     * This method returns a positive double value grater than 0.0
     * and less than 1.0
     */

    System.out.println("Random numbers between 0.0 and 1.0 are,");
    for (int i = 0; i < 5; i++)
      System.out.println("Random Number [" + (i + 1) + "] : " + Math.random());

    /*
     * To generate random number between 1 to 100 use following code
     */

    System.out.println("Random numbers between 1 and 100 are,");
    for (int i = 0; i < 5; i++)
      System.out.println("Random Number [" + (i + 1) + "] : " + (int) (Math.random() * 100));
  }
}

/*
 Typical output would be
 Random numbers between 0.0 and 1.0 are,
 Random Number [1] : 0.7900395454653136
 Random Number [2] : 0.15887365598103076
 Random Number [3] : 0.5570570713930629
 Random Number [4] : 0.017811004461356195
 Random Number [5] : 0.7135560403213513

 Random numbers between 1 and 100 are,
 Random Number [1] : 31
 Random Number [2] : 21
 Random Number [3] : 24
 Random Number [4] : 95
 Random Number [5] : 3
 */
