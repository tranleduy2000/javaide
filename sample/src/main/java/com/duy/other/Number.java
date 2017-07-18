package com.duy.other;

import java.util.Scanner;

/**
 * Created by duy on 18/07/2017.
 */

public class Number {
    private double num;

    public double getNum() {
        return num;
    }

    public void setNum(double num) {
        this.num = num;
    }

    public void read(Scanner scanner) {
        this.num = scanner.nextDouble();
    }
}
