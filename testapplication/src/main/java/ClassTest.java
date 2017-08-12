import java.util.ArrayList;

/**
 * Created by Duy on 12-Aug-17.
 */

public class ClassTest extends ArrayList {
    public static void main(String[] args) {
        new ClassTest().add(100);
    }

    @Override
    public boolean add(Object o) {
        System.out.println(o);
        return super.add(o);
    }
}
