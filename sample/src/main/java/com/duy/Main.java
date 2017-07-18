package com.duy;

import com.duy.other.Number;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Duy on 17-Jul-17.
 */
public class Main {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("Number " + i);
        }
        System.out.println(list);
        Scanner scanner = new Scanner(System.in);
        Number one = new Number();
        System.out.println("Enter number one ");
        one.read(scanner);
        System.out.println(one.getNum());
    }
}
