package sample;

/**
 * Java break statement with label example. This example shows how to use java break statement to
 * terminate the labeled loop. The following example uses break to terminate the labeled loop while
 * searching two dimensional int array.
 */
public class JavaBreakWithLableExample {

  public static void main(String[] args) {

    int[][] intArray = new int[][]{{1, 2, 3, 4, 5}, {10, 20, 30, 40, 50}};
    boolean blnFound = false;

    System.out.println("Searching 30 in two dimensional int array..");

    Outer:
    for (int intOuter = 0; intOuter < intArray.length; intOuter++) {
      Inner:
      for (int intInner = 0; intInner < intArray[intOuter].length; intInner++) {
        if (intArray[intOuter][intInner] == 30) {
          blnFound = true;
          break Outer;
        }
      }
    }

    if (blnFound == true) System.out.println("30 found in the array");
    else System.out.println("30 not found in the array");
  }
}

/*
 Output would be
 Searching 30 in two dimensional int array..
 30 found in the array
 */
