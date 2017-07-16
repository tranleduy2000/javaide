/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *	 Copyright (C) 2009 by Hector Suarez Barenca http://barenca.net
 *   <http://www.gnu.org/licenses/lgpl-3.0.html>
 *
 *   This file is a part of jAstyle library - an indentation and
 *   reformatting library for C, C++, C# and Java source files.
 *   <http://jastyle.sourceforge.net>
 *
 *   jAstyle is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   jAstyle is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with jAstyle.  If not, see <http://www.gnu.org/licenses/>.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package net.barenca.jastyle;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sort comparison function. Compares the length of the value of pointers in the
 * vectors. The LONGEST Strings will be first in the vector.
 *
 */
class SortOnLength implements Serializable,Comparator<String>
{
	/**
	 *
	 */
	private static final long serialVersionUID = 4170501851833867985L;

	public int compare(String a, String b)
	{
		return a.length() - b.length();
	}
}
