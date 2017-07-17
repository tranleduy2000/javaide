//Standard libs
#include <stdio.h>
#include <stdlib.h>

//Our own lib
#include <myfunc.h>

int main(){
        
        //Say Hello
        printf("Hello - please enter some text..\n : ");
        
        //To hold the string
        char text[100];

        //First get some input
        scanf("%s", text);

        //Now lets output it back
        mySpecialFunction(text);

        return 0;
}
