package com.google.googlejavaformat.java;

import static com.sun.tools.javac.tree.JCTree.POSTDEC;
import static com.sun.tools.javac.tree.JCTree.POSTINC;
import static com.sun.tools.javac.tree.JCTree.PREDEC;
import static com.sun.tools.javac.tree.JCTree.PREINC;

/**
 * Created by Duy on 22-Jul-17.
 */

public class OpUtil {
    public static boolean isPostUnaryOp(int tag) {
        return tag == POSTINC || tag == POSTDEC;
    }

    public boolean isIncOrDecUnaryOp(int tag) {
        return (tag == PREINC || tag == PREDEC || tag == POSTINC || tag == POSTDEC);
    }

}
