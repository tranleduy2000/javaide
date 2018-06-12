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

import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteTypeDeclared;

import junit.framework.TestCase;

import static com.duy.ide.javaide.editor.autocomplete.internal.Patterns.TYPE_DECLARE_MODIFIERS;

public class CompleteTypeDeclaredTest extends TestCase {

    public void test1() {
        boolean matches = "public class CompleteClassDeclaredTest extends TestCase"
                .matches(CompleteTypeDeclared.CLASS_DECLARE.pattern());
        assertTrue(matches);
    }

    public void test2() {
        boolean matches = "public class CompleteClassDeclaredTest"
                .matches(CompleteTypeDeclared.CLASS_DECLARE.pattern());
        assertTrue(matches);
    }

    public void test3() {
        boolean matches = "public"
                .matches(TYPE_DECLARE_MODIFIERS.pattern());
        assertTrue(matches);
    }
}