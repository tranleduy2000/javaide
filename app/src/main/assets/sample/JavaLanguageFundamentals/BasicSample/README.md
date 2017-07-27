Break Statement
Break statement is one of the several control statements Java provide to control the flow of the program. As the name says, Break Statement is generally used to break the loop of switch statement.

Please note that Java does not provide Go To statement like other programming languages e.g. C, C++.

Break statement has two forms labeled and unlabeled.

Unlabeled Break statement

This form of break statement is used to jump out of the loop when specific condition occurs. This form of break statement is also used in switch statement.

For example,

```Java
for(int var =0; var < 5 ; var++)
{
        System.out.println(“Var is : “ + var);

        if(var == 3)
                break;
}
```
In above break statement example, control will jump out of loop when var becomes 3.

Labeled Break Statement

The unlabeled version of the break statement is used when we want to jump out of a single loop or single case in switch statement. Labeled version of the break statement is used when we want to jump out of nested or multiple loops.

For example,

Outer:
```Java
for(int var1=0; var1 < 5 ; var1++)
{
         for(int var2 = 1; var2 < 5; var2++)
        {
                System.out.println(“var1:” + var1 + “, var2:” + var2);

                if(var1 == 3)
                        break Outer;

        }
}
```