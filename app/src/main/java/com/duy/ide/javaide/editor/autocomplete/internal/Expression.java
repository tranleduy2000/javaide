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

package com.duy.ide.javaide.editor.autocomplete.internal;

import com.sun.tools.javac.tree.JCTree;

import java.util.LinkedList;
import java.util.List;

import static com.sun.tools.javac.tree.JCTree.JCExpression;

public class Expression {
    /**
     * Parent scope of statement, from higher to lower
     * parentOfStatement.get(0) will be contains parentOfStatement.get(1)
     */
    public final LinkedList<JCTree> parentOfStatement = new LinkedList<>();

    /**
     * Expression at cursor
     */
    public JCExpression expression;

    public Expression(JCExpression expression) {
        this.expression = expression;
    }

    public Expression(JCTree root, JCExpression expression) {
        this.expression = expression;
    }

    public void addRoot(JCTree tree) {
        if (tree != null) {
            parentOfStatement.addFirst(tree);
        }
    }

    public JCTree getRootAt(int level) {
        return parentOfStatement.get(level);
    }

    public JCExpression getExpression() {
        return expression;
    }

    public int getParentLevelCount() {
        return parentOfStatement.size();
    }

    public LinkedList<JCTree> getParentOfExpression() {
        return parentOfStatement;
    }

    @Override
    public String toString() {
        if (expression instanceof JCTree.JCErroneous) {
            List<? extends JCTree> errorTrees
                    = ((JCTree.JCErroneous) expression).getErrorTrees();
            return errorTrees.toString();
        }
        return expression.toString();
    }
}
