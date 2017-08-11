package sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UnmodifiableCollection {

  public static void main(String args[]) {
    List<String> list = new ArrayList<String>();

    list.add("This");
    list.add("is");
    list.add("Unmodifiable Collection");

    System.out.println("Element added to list: " + list.get(2));

    Collection<String> immutableCol = Collections.unmodifiableCollection(list);

    Iterator<String> iterator = immutableCol.iterator();

    while (iterator.hasNext()) {
      System.out.println(iterator.next());
    }
  }
}
