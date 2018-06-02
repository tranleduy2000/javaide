package com.example.android.multiproject.library;

import com.example.android.multiproject.person.Person;

import android.content.Context;
import android.widget.TextView;

class PersonView extends TextView {
    public PersonView(Context context, Person person) {
        super(context);
        setTextSize(20);
        setText(person.getName());
    }
}
