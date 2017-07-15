/*
 * Copyright (c) 2001, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.javac.tree;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCAssignOp;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCSkip;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.LetExpr;
import com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import com.sun.tools.javac.tree.JCTree.Visitor;
import com.sun.tools.javac.util.List;

/**
 * A subclass of Tree.Visitor, this class defines
 * a general tree scanner pattern. Translation proceeds recursively in
 * left-to-right order down a tree. There is one visitor method in this class
 * for every possible kind of tree node.  To obtain a specific
 * scanner, it suffices to override those visitor methods which
 * do some interesting work. The scanner class itself takes care of all
 * navigational aspects.
 * <p>
 * <p><b>This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own risk.
 * This code and its internal interfaces are subject to change or
 * deletion without notice.</b>
 */
public class TreeScanner extends Visitor {

    /**
     * Visitor method: Scan a single node.
     */
    public void scan(JCTree tree) {
        if (tree != null) tree.accept(this);
    }

    /**
     * Visitor method: scan a list of nodes.
     */
    public void scan(List<? extends JCTree> trees) {
        if (trees != null)
            for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
                scan(l.head);
    }


/* ***************************************************************************
 * Visitor methods
 ****************************************************************************/

    public void visitTopLevel(JCCompilationUnit tree) {
        scan(tree.packageAnnotations);
        scan(tree.pid);
        scan(tree.defs);
    }

    public void visitImport(JCImport tree) {
        scan(tree.qualid);
    }

    public void visitClassDef(JCClassDecl tree) {
        scan(tree.mods);
        scan(tree.typarams);
        scan(tree.extending);
        scan(tree.implementing);
        scan(tree.defs);
    }

    public void visitMethodDef(JCMethodDecl tree) {
        scan(tree.mods);
        scan(tree.restype);
        scan(tree.typarams);
        scan(tree.params);
        scan(tree.thrown);
        scan(tree.defaultValue);
        scan(tree.body);
    }

    public void visitVarDef(JCVariableDecl tree) {
        scan(tree.mods);
        scan(tree.vartype);
        scan(tree.init);
    }

    public void visitSkip(JCSkip tree) {
    }

    public void visitBlock(JCBlock tree) {
        scan(tree.stats);
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
        scan(tree.body);
        scan(tree.cond);
    }

    public void visitWhileLoop(JCWhileLoop tree) {
        scan(tree.cond);
        scan(tree.body);
    }

    public void visitForLoop(JCForLoop tree) {
        scan(tree.init);
        scan(tree.cond);
        scan(tree.step);
        scan(tree.body);
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
        scan(tree.var);
        scan(tree.expr);
        scan(tree.body);
    }

    public void visitLabelled(JCLabeledStatement tree) {
        scan(tree.body);
    }

    public void visitSwitch(JCSwitch tree) {
        scan(tree.selector);
        scan(tree.cases);
    }

    public void visitCase(JCCase tree) {
        scan(tree.pat);
        scan(tree.stats);
    }

    public void visitSynchronized(JCSynchronized tree) {
        scan(tree.lock);
        scan(tree.body);
    }

    public void visitTry(JCTry tree) {
        scan(tree.body);
        scan(tree.catchers);
        scan(tree.finalizer);
    }

    public void visitCatch(JCCatch tree) {
        scan(tree.param);
        scan(tree.body);
    }

    public void visitConditional(JCConditional tree) {
        scan(tree.cond);
        scan(tree.truepart);
        scan(tree.falsepart);
    }

    public void visitIf(JCIf tree) {
        scan(tree.cond);
        scan(tree.thenpart);
        scan(tree.elsepart);
    }

    public void visitExec(JCExpressionStatement tree) {
        scan(tree.expr);
    }

    public void visitBreak(JCBreak tree) {
    }

    public void visitContinue(JCContinue tree) {
    }

    public void visitReturn(JCReturn tree) {
        scan(tree.expr);
    }

    public void visitThrow(JCThrow tree) {
        scan(tree.expr);
    }

    public void visitAssert(JCAssert tree) {
        scan(tree.cond);
        scan(tree.detail);
    }

    public void visitApply(JCMethodInvocation tree) {
        scan(tree.meth);
        scan(tree.args);
    }

    public void visitNewClass(JCNewClass tree) {
        scan(tree.encl);
        scan(tree.clazz);
        scan(tree.args);
        scan(tree.def);
    }

    public void visitNewArray(JCNewArray tree) {
        scan(tree.elemtype);
        scan(tree.dims);
        scan(tree.elems);
    }

    public void visitParens(JCParens tree) {
        scan(tree.expr);
    }

    public void visitAssign(JCAssign tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    public void visitAssignop(JCAssignOp tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    public void visitUnary(JCUnary tree) {
        scan(tree.arg);
    }

    public void visitBinary(JCBinary tree) {
        scan(tree.lhs);
        scan(tree.rhs);
    }

    public void visitTypeCast(JCTypeCast tree) {
        scan(tree.clazz);
        scan(tree.expr);
    }

    public void visitTypeTest(JCInstanceOf tree) {
        scan(tree.expr);
        scan(tree.clazz);
    }

    public void visitIndexed(JCArrayAccess tree) {
        scan(tree.indexed);
        scan(tree.index);
    }

    public void visitSelect(JCFieldAccess tree) {
        scan(tree.selected);
    }

    public void visitIdent(JCIdent tree) {
    }

    public void visitLiteral(JCLiteral tree) {
    }

    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
    }

    public void visitTypeArray(JCArrayTypeTree tree) {
        scan(tree.elemtype);
    }

    public void visitTypeApply(JCTypeApply tree) {
        scan(tree.clazz);
        scan(tree.arguments);
    }

    public void visitTypeParameter(JCTypeParameter tree) {
        scan(tree.bounds);
    }

    @Override
    public void visitWildcard(JCWildcard tree) {
        scan(tree.kind);
        if (tree.inner != null)
            scan(tree.inner);
    }

    @Override
    public void visitTypeBoundKind(TypeBoundKind that) {
    }

    public void visitModifiers(JCModifiers tree) {
        scan(tree.annotations);
    }

    public void visitAnnotation(JCAnnotation tree) {
        scan(tree.annotationType);
        scan(tree.args);
    }

    public void visitErroneous(JCErroneous tree) {
    }

    public void visitLetExpr(LetExpr tree) {
        scan(tree.defs);
        scan(tree.expr);
    }

    public void visitTree(JCTree tree) {
        assert false;
    }
}
