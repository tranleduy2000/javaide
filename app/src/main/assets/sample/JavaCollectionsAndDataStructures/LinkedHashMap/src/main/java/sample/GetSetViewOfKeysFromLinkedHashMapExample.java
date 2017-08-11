package sample;
/*
  Get Set view of Keys from Java LinkedHashMap example
  This Java Example shows how to get a Set of keys contained in LinkedHashMap
  using keySet method of Java LinkedHashMap class.
*/

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class GetSetViewOfKeysFromLinkedHashMapExample {

  public static void main(String[] args) {

    //create LinkedHashMap object
    LinkedHashMap lHashMap = new LinkedHashMap();

    //add key value pairs to LinkedHashMap
    lHashMap.put("1", "One");
    lHashMap.put("2", "Two");
    lHashMap.put("3", "Three");

    /*
      get Set of keys contained in LinkedHashMap using
      Set keySet() method of LinkedHashMap class
    */

    Set st = lHashMap.keySet();

    System.out.println("Set created from LinkedHashMap Keys contains :");
    //iterate through the Set of keys
    Iterator itr = st.iterator();
    while (itr.hasNext()) System.out.println(itr.next());

    /*
       Please note that resultant Set object is backed by the LinkedHashMap.
       Any key that is removed from Set will also be removed from
       original LinkedHashMap object. The same is not the case with the element
       addition.
    */

    //remove 2 from Set
    st.remove("2");

    //check if original LinkedHashMap still contains 2
    boolean blnExists = lHashMap.containsKey("2");
    System.out.println("Does LinkedHashMap contain 2 ? " + blnExists);
  }
}

/*
 Output would be
 Set created from LinkedHashMap Keys contains :
 1
 2
 3
 Does LinkedHashMap contain 2 ? false
 */
