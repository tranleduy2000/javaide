``^`` is cursor index

### Complete package name
1. Type ``import``
```java
import java.
            ^
            (show suggest 'io', 'util', ...)
```
2. Type package
```java
void method(){
    java.la
           ^
           (show suggest 'lang' or more package start with 'la')
}
```

## Complete class name
```java
void method(){
    Inte
        ^
        (show suggest Integer or more classes,
        when user click suggestion, the editor
        will be auto import class)
}
```
