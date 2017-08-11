package sample;
/*
        Create New Thread Using Runnable Example
        This Java example shows how to create a new thread by implementing
        Java Runnable interface.
*/

/*
 * To create a thread using Runnable, a class must implement
 * Java Runnable interface.
 */
public class CreateThreadRunnableExample implements Runnable {

  public static void main(String[] args) {

    /*
     * To create new thread, use
     * Thread(Runnable thread, String threadName)
     * constructor.
     *
     */

    Thread t = new Thread(new CreateThreadRunnableExample(), "My Thread");

    /*
     * To start a particular thread, use
     * void start() method of Thread class.
     *
     * Please note that, after creation of a thread it will not start
     * running until we call start method.
     */

    t.start();

    for (int i = 0; i < 5; i++) {

      System.out.println("Main thread : " + i);

      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        System.out.println("Child thread interrupted! " + ie);
      }
    }
    System.out.println("Main thread finished!");
  }

  /*
   * A class must implement run method to implement Runnable
   * interface. Signature of the run method is,
   *
   * public void run()
   *
   * Code written inside run method will constite a new thread.
   * New thread will end when run method returns.
   */
  public void run() {

    for (int i = 0; i < 5; i++) {
      System.out.println("Child Thread : " + i);

      try {
        Thread.sleep(50);
      } catch (InterruptedException ie) {
        System.out.println("Child thread interrupted! " + ie);
      }
    }

    System.out.println("Child thread finished!");
  }
}

/*
 Typical output of this thread example would be

 Main thread : 0
 Child Thread : 0
 Child Thread : 1
 Main thread : 1
 Main thread : 2
 Child Thread : 2
 Child Thread : 3
 Main thread : 3
 Main thread : 4
 Child Thread : 4
 Child thread finished!
 Main thread finished!

 */
