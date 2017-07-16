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

import net.barenca.jastyle.reader.ExtendedReader;

import java.io.IOException;
import java.io.Reader;

public class ASStreamIterator implements ASSourceIterator
{
	private boolean peekSet = true;
	private ExtendedReader inStream; // pointer to the input stream
	private StringBuffer buffer; // current input line
	private int eolWindows; // number of Windows line endings (CRLF)
	private int eolLinux; // number of Linux line endings (LF)
	private int eolMacOld; // number of old Mac line endings (CR)
	private String outputEOL; // output end of line char

	public ASStreamIterator(Reader in)
	{
		this.peekSet = true;
		this.inStream = new ExtendedReader(in);
		this.eolWindows = eolLinux = eolMacOld = 0;
	}

	public StringBuffer nextLine()
	{
		return nextLine(false);
	}

	/**
	 * read the input stream, delete any end of line characters, and build a
	 * String that contains the input line.
	 *
	 * @return String containing the next input line minus any end of line
	 *         characters
	 */
	public StringBuffer nextLine(boolean emptyLineWasDeleted)
	{
		try
		{
			// read the next record
			buffer = new StringBuffer();
			int ch = inStream.read();
			while (ch > 0 && ch != '\n' && ch != '\r')
			{
				buffer.append((char) ch);
				ch = inStream.read();
			}

			if (ch <= 0)
			{
				return buffer;
			}

			int peekCh = inStream.peek();

			// find input end-of-line characters
			if (peekCh > 0)
			{
				if (ch == '\r') // CR+LF is windows otherwise Mac OS 9
				{
					if (peekCh == '\n')
					{
						ch = (char) inStream.read();
						eolWindows++;
					} else
						eolMacOld++;
				} else
				// LF is Linux, allow for improbable LF/CR
				{
					if (peekCh == '\r')
					{
						ch = (char) inStream.read();
						eolWindows++;
					} else
						eolLinux++;
				}
			}

			// set output end of line characters
			if (eolWindows >= eolLinux)
			{
				if (eolWindows >= eolMacOld)
				{
					outputEOL = "\r\n"; // Windows (CR+LF)
				} else
				{
					outputEOL = "\r"; // MacOld (CR)
				}
			} else if (eolLinux >= eolMacOld)
			{
				outputEOL = "\n"; // Linux (LF)
			} else
			{
				outputEOL = "\r"; // MacOld (CR)
			}

		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return buffer;
	}

	private final static int MAX_PEEK = 256;

	// save the current position and get the next line
	// this can be called for multiple reads
	// when finished peeking you MUST call peekReset()
	// call this function from ASFormatter ONLY
	public StringBuffer peekNextLine()
	{
		StringBuffer nextLine = new StringBuffer();
		try
		{
			if (peekSet)
			{
				peekSet = false;
				inStream.mark(MAX_PEEK);
			}
			// read the next record
			int ch = inStream.read();
			while (ch > 0 && ch != '\n' && ch != '\r')
			{
				nextLine.append((char) ch);
				ch = inStream.read();
			}

			if (ch <= 0)
			{
				return nextLine;
			}

			int peekCh = inStream.peek();

			// remove end-of-line characters
			if (peekCh > 0)
			{
				if ((peekCh == '\n' || peekCh == '\r') && peekCh != ch) // ///////////
					// changed
					// //////////
					inStream.read();
			}
		} catch (IOException e)
		{
			throw new RuntimeException(e.getCause());
		}
		return nextLine;
	}

	public boolean hasMoreLines()
	{
		boolean hasMoreLines = false;
		try
		{
			hasMoreLines = !inStream.isEndOfStream();
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return hasMoreLines;
	}

	// save the last input line after input has reached EOF
	public String getOutputEOL()
	{
		return outputEOL;
	}

	public void peekReset()
	{
		try
		{
			if (!peekSet)
			{
				peekSet = true;
				inStream.reset();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
