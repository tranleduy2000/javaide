package com.example.android.multiproject.person;

import com.google.common.collect.Lists;

import java.util.Iterator;

public class People implements Iterable<Person> {
    public Iterator<Person> iterator() {
        return Lists.newArrayList(new Person("Fred"), new Person("Barney")).iterator();
    }
}
