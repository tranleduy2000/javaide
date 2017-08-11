package sample;
/*
        Java static member variable example
        This Java Example shows how to declare and use static member variable inside
        a java class.
*/

public class StaticMemberExample {

  public static void main(String[] args) {

    ObjectCounter object1 = new ObjectCounter();
    System.out.println(object1.getNumberOfObjects());

    ObjectCounter object2 = new ObjectCounter();
    System.out.println(object2.getNumberOfObjects());
  }
}

class ObjectCounter {

  /*
   * Static members are class level variables and shared by all the objects
   * of the class.
   *
   * To define static member, use static keyword
   * e.g. static int i=0;
   *
   * Please note that static member variables can be accessed inside
   * non static methods because they are class level variables.
   *
   */
  static int counter = 0;

  public ObjectCounter() {

    /*increase the object counter. Since only one varible is shared between
     * all objects of this class, it always return number of objects till now.
     */
    counter++;
  }

  //returns number of objects created till now
  public int getNumberOfObjects() {
    return counter;
  }
}
