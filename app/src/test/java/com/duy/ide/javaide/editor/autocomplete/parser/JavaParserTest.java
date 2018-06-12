/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.parser;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import junit.framework.TestCase;

import static com.sun.tools.javac.tree.JCTree.JCBlock;
import static com.sun.tools.javac.tree.JCTree.JCClassDecl;
import static com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import static com.sun.tools.javac.tree.JCTree.JCErroneous;
import static com.sun.tools.javac.tree.JCTree.JCExpression;
import static com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import static com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import static com.sun.tools.javac.tree.JCTree.JCImport;
import static com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import static com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import static com.sun.tools.javac.tree.JCTree.JCStatement;

public class JavaParserTest extends TestCase {

    public void testParse() {
        JavaParser parser = new JavaParser();
        JCCompilationUnit ast = parser.parse(getTestSource());

        JCExpression packageName = ast.getPackageName();
        System.out.println("packageName = " + packageName);

        List<JCImport> imports = ast.getImports();
        System.out.println("imports = " + imports);

        List<JCTree> classes = ast.getTypeDecls();
        for (JCTree typeDef : classes) {
            System.out.println("type def " + typeDef.getClass());
            if (typeDef instanceof JCClassDecl) {
                printClassInfo((JCClassDecl) typeDef);
            }
        }
    }

    private void printClassInfo(JCClassDecl classDef) {
        JCExpression extending = classDef.extending;
        System.out.println("extending = " + extending);
        List<JCExpression> implementing = classDef.implementing;
        System.out.println("implementing = " + implementing);

        List<JCTree> members = classDef.getMembers();
        for (JCTree member : members) {
            printMemberInfo(member);
        }
    }

    private void printMemberInfo(JCTree member) {
        if (member instanceof JCMethodDecl) {
            printMethodInfo((JCMethodDecl) member);
        }
    }

    private void printMethodInfo(JCMethodDecl methodDecl) {
        Name methodName = methodDecl.getName();
        System.out.println("methodName = " + methodName);

        JCBlock body = methodDecl.getBody();
        List<JCStatement> statements = body.getStatements();
        for (JCStatement statement : statements) {
            System.out.println("statement = " + statement);
            if (statement instanceof JCExpressionStatement) {
                JCExpression expression = ((JCExpressionStatement) statement).getExpression();
                System.out.println("expression = " + expression);
                if (expression instanceof JCErroneous) {
                    JCErroneous errorTree = (JCErroneous) expression;
                    System.out.println("error = " + errorTree);
                    for (JCTree error : errorTree.getErrorTrees()) {
                        if (error instanceof JCFieldAccess) {
                            JCFieldAccess fieldErr = (JCFieldAccess) error;
                            JCExpression exprErr = fieldErr.getExpression();
                            System.out.println("error at field access " + fieldErr);
                        } else if (error instanceof JCMethodInvocation) {
                            JCMethodInvocation methodInvocation = (JCMethodInvocation) error;
                        }

                        System.out.println("error = " + error);
                    }
                }
            }
        }
    }
    private String getTestSource() {
        return "package com.duy.ide.javaide.editor.autocomplete.parser;\n" +
                "import java.util.*;" +
                "\n" +
                "public final class IncompleteSource extends ParentClass implements OtherInterface {\n" +
                "    int var1;\n" +
                "    long var2;\n" +
                "\n" +
                "    void m1() {\n" +
                "        String s = new String(\"\");\n" +
                "         s.replace(\"\", \"\").toCharArra\n" +
                "    }\n" +
                "\n" +
                "    void m2() {\n" +
                "        String.valueOf(var1).to\n" +
                "    }\n" +
                "\n" +
                "    void m3() {\n" +
                "        valueOf(var1).toLow\n" +
                "    }\n" +
                "}";
    }
}