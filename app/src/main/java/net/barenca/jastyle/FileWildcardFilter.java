/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class FileWildcardFilter implements FilenameFilter
{
	private Pattern p;

	public FileWildcardFilter(String filenameWithWildcars)
	{
		p = Pattern.compile(replaceWildcards(filenameWithWildcars));
	}

	/**
	 * Checks for * and ? in the wildcard variable and replaces them correct
	 * pattern characters.
	 *
	 * @param wild
	 *            - Wildcard name containing * and ?
	 * @return - String containing modified wildcard name
	 */
	private String replaceWildcards(String wild)
	{
		StringBuffer buffer = new StringBuffer();

		char[] chars = wild.toCharArray();

		for (int i = 0; i < chars.length; ++i)
		{
			if (chars[i] == '*')
			{
				buffer.append(".*");
			} else if (chars[i] == '?')
			{
				buffer.append(".");
			} else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
			{
				buffer.append('\\').append(chars[i]);
			} else
			{
				buffer.append(chars[i]);
			}
		}

		return buffer.toString();

	}

	@Override
	public boolean accept(File dir, String name)
	{
		return p.matcher(name).matches();
	}

}
