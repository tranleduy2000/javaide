package com.example;

import java.util.ArrayList;

public class Main {public static void main(String[] args) {NumberOne numberOne = new NumberOne();numberOne.print();NumberTwo numberTwo = new NumberTwo();ArrayList<String> arrayList = new ArrayList<String>();arrayList.add(numberOne.toString());arrayList.add(numberTwo.toString());System.out.println(arrayList);}
}
