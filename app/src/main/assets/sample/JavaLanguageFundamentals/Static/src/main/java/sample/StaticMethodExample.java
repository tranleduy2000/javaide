package sample;
/*
        Java static method example
        This Java Example shows how to declare and use static methods inside
        a java class.
*/

public class StaticMethodExample {

  public static void main(String[] args) {

    int result = MathUtility.add(1, 2);
    System.out.println("(1+2) is : " + result);
  }
}

class MathUtility {

  /*
   * To declare static method use static keyword.
   * Static methods are class level methods and can not access any instance
   * member directly. However, it can access members of a particular object
   * using its reference.
   *
   * Static methods are generally written as a utility method or it performs
   * task for all objects of the class.
   *
   */

  public static int add(int first, int second) {
    return first + second;
  }
}
