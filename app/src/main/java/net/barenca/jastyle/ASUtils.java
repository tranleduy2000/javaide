/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   jAstyle library includes in most of its parts translated C++ code originally
 *   developed by Jim Pattee and Tal Davidson for the Artistic Style project.
 *
 *	 Copyright (C) 2009 by Hector Suarez Barenca http://barenca.net
 *   Copyright (C) 2006-2008 by Jim Pattee <jimp03@email.com>
 *   Copyright (C) 1998-2002 by Tal Davidson
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

class ASUtils
{
	protected static final String WHITE_SPACE = " \t";
	public static boolean isPunct(char c)
	{
		return (('!' <= c && c <= '/') || (':' <= c && c <= '@')
				|| ('[' <= c && c <= '`') || ('{' <= c && c <= '~'));

	}

	/**
	 * Searches for the last character in the object which is not part of either
	 * str, s or c, and returns its position.
	 *
	 * @param in
	 * @param chars
	 *            string containing the characters to match against in the
	 *            object.
	 * @return
	 */
	public static int findLastNotOf(StringBuffer in, String chars)
	{
		return findLastNotOf(in, chars, 0);
	}

	/**
	 * Searches for the last character in the object which is not part of either
	 * str, s or c, and returns its position.
	 *
	 * @param in
	 * @param chars
	 *            string containing the characters to match against in the
	 *            object.
	 * @param end
	 *            Position of the last character in the string to be taken into
	 *            consideration for matches. The default value npos indicates
	 *            that the entire string is considered.
	 * @return
	 */
	public static int findLastNotOf(StringBuffer in, String chars, int end)
	{
		for (int index = in.length() - 1; index >= end; index--)
		{
			char ch = in.charAt(index);
			if (chars.indexOf(ch) < 0)
			{
				return index;
			}
		}
		return -1;
	}

	public static int findFirstNotOf(StringBuffer in, String chars)
	{
		return findFirstNotOf(in, chars, 0);
	}

	public static int findFirstNotOf(StringBuffer in, String chars,
			int start)
	{
		for (int index = start; index < in.length(); index++)
		{
			char ch = in.charAt(index);
			if (chars.indexOf(ch) < 0)
			{
				return index;
			}
		}
		return -1;
	}

	/**
	 * Repite el caracter el n&uacute;mero de veces indicado
	 *
	 * @param times
	 * @param ch
	 * @return
	 */
	public static String repeat(int times, char ch)
	{
		StringBuffer sb = new StringBuffer(times);
		for (int i = 0; i < times; i++)
		{
			sb.append(ch);
		}
		return sb.toString();
	}
}
