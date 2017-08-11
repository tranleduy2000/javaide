package sample;
/*
        Find Square Root of BigInteger Example
        This Java example shows how to find square root of BigInteger using
        NEWTON's method.
*/

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class SquareRootOfBigIntegerExample {

  public static void main(String[] args) {

    SquareRootOfBigIntegerExample SquareRootOfBigIntegerExample =
            new SquareRootOfBigIntegerExample();
    String n = "";

    MathContext mc = new MathContext(0, RoundingMode.DOWN);
    mc = MathContext.DECIMAL32;

    BigInteger my2P100000 = new BigInteger("0");
    BigInteger two = new BigInteger("2");
    BigInteger one = new BigInteger("1");

    my2P100000 = two.shiftLeft(2000 - 1);

    System.out.println("2^2000 --  Step 1");
    System.out.println("Value of 2^2,000 " + my2P100000);
    System.out.println("");
    System.out.println("Finding the Square Root of 2^2000");

    String mys = my2P100000 + "";
    n = (mys);
    int firsttime = 0;

    BigDecimal myNumber = new BigDecimal(n);
    BigDecimal g = new BigDecimal("1");
    BigDecimal my2 = new BigDecimal("2");
    BigDecimal epsilon = new BigDecimal("0.0000000001");

    BigDecimal nByg = myNumber.divide(g, 9, BigDecimal.ROUND_FLOOR);

    //Get the value of n/g
    BigDecimal nBygPlusg = nByg.add(g);

    //Get the value of "n/g + g
    BigDecimal nBygPlusgHalf = nBygPlusg.divide(my2, 9, BigDecimal.ROUND_FLOOR);

    //Get the value of (n/g + g)/2
    BigDecimal saveg = nBygPlusgHalf;
    firsttime = 99;

    do {
      g = nBygPlusgHalf;
      nByg = myNumber.divide(g, 9, BigDecimal.ROUND_FLOOR);
      nBygPlusg = nByg.add(g);
      nBygPlusgHalf = nBygPlusg.divide(my2, 9, BigDecimal.ROUND_FLOOR);
      BigDecimal savegdiff = saveg.subtract(nBygPlusgHalf);

      if (savegdiff.compareTo(epsilon) == -1) {
        firsttime = 0;
      } else {
        saveg = nBygPlusgHalf;
      }

    } while (firsttime > 1);

    System.out.println(
            "For " + mys + "\nLength: " + mys.length() + "\nThe Square Root is " + saveg);
  }
}

/*
 Output of this Java example would be
 2^2000 --  Step 1
 Value of 2^2,000 114813069527425452423283320117768198402231770208869520047764273682576626139237031385665948631650626991844596463898746277344711896086305533142593135616665318539129989145312280000688779148240044871428926990063486244781615463646388363947317026040466353970904996558162398808944629605623311649536164221970332681344168908984458505602379484807914058900934776500429002716706625830522008132236281291761267883317206598995396418127021779858404042159853183251540889433902091920554957783589672039160081957216630582755380425583726015528348786419432054508915275783882625175435528800822842770817965453762184851149029376

 Finding the Square Root of 2^2000
 For 114813069527425452423283320117768198402231770208869520047764273682576626139237031385665948631650626991844596463898746277344711896086305533142593135616665318539129989145312280000688779148240044871428926990063486244781615463646388363947317026040466353970904996558162398808944629605623311649536164221970332681344168908984458505602379484807914058900934776500429002716706625830522008132236281291761267883317206598995396418127021779858404042159853183251540889433902091920554957783589672039160081957216630582755380425583726015528348786419432054508915275783882625175435528800822842770817965453762184851149029376
 Length: 603
 The Square Root is 10715086071862673209484250490600018105614048117055336074437503883703510511249361224931983788156958581275946729175531468251871452856923140435984577574698574803934567774824230985421074605062371141877954182153046474983581941267398767559165543946077062914571196477686542167660429831652624386837205668069376.000000000

 */
