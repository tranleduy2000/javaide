package sample;

/*
        Calculate Average value of Array elements using Java Example
        This Java Example shows how to calculate average value of array
        elements.
*/
public class CalculateArrayAverageExample {

  public static void main(String[] args) {

    //define an array
    int[] numbers = new int[]{10, 20, 15, 25, 16, 60, 100};

    /*
     * Average value of array elements would be
     * sum of all elements/total number of elements
     */

    //calculate sum of all array elements
    int sum = 0;

    for (int i = 0; i < numbers.length; i++) sum = sum + numbers[i];

    //calculate average value
    double average = sum / numbers.length;

    System.out.println("Average value of array elements is : " + average);
  }
}

/*
 Output of Calculate Average value of Array elements using Java Example would be
 Average value of array elements is : 35.0
 */
