package sample;
/*
        Pause Thread Using Sleep Method Example
        This Java example shows how to pause currently running thread using
        sleep method of Java Thread class.
*/

public class PauseThreadUsingSleep {

  public static void main(String[] args) {

    /*
     * To pause execution of a thread, use
     * void sleep(int milliseconds) method of Thread class.
     *
     * This is a static method and causes the suspension of the thread
     * for specified period of time.
     *
     * Please note that, this method may throw InterruptedException.
     */

    System.out.println("Print number after pausing for 1000 milliseconds");
    try {

      for (int i = 0; i < 5; i++) {

        System.out.println(i);

        /*
         * This thread will pause for 1000 milliseconds after
         * printing each number.
         */
        Thread.sleep(1000);
      }
    } catch (InterruptedException ie) {
      System.out.println("Thread interrupted !" + ie);
    }
  }
}

/*
 Output of this example would be

 Print number after pausing for 1000 milliseconds
 0
 1
 2
 3
 4
 */
