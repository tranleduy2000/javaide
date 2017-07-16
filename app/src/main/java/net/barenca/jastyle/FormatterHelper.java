/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   jAstyle library.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class FormatterHelper
{
	/**
	 * Returns the formatted version of the source code
	 * @param in The reader of the original source code (this reader will be closed)
	 * @param formatter The formatter to use
	 * @return String
	 */
	public static String format(Reader in, ASFormatter formatter)
	{
		ASStreamIterator streamIterator = new ASStreamIterator(in);
		formatter.init(streamIterator);
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		// format the file
		while (formatter.hasMoreLines())
		{
			out.println(formatter.nextLine().toString());
		}
		out.flush();
		out.close();
		try
		{
			in.close();
		} catch (IOException e)
		{
			throw new RuntimeException(e.getCause());
		}
		return writer.toString();
	}
	/**
	 * Returns the formatted version of the source code
	 * @param in The reader of the original source code (this reader will be closed)
	 * @param formatter The formatter to use
	 * @param writer The writer where thw output will be written
	 */
	public static void format(Reader in, ASFormatter formatter,Writer writer)
	{
		ASStreamIterator streamIterator = new ASStreamIterator(in);
		formatter.init(streamIterator);
		PrintWriter out = new PrintWriter(writer);
		// format the file
		while (formatter.hasMoreLines())
		{
			out.println(formatter.nextLine().toString());
		}
		out.flush();
		out.close();
		try
		{
			in.close();
		} catch (IOException e)
		{
			throw new RuntimeException(e.getCause());
		}
	}
}
