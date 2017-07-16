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

import java.util.Stack;

public class ASEnhancer extends AbstractASBase
{
	// options from command line or options file
	int indentLength;
	boolean useTabs;
	boolean caseIndent;
	boolean emptyLineFill;

	// parsing variables
	int lineNumber;
	boolean isInQuote;
	boolean isInComment;
	char quoteChar;

	// unindent variables
	int bracketCount;
	int switchDepth;
	boolean lookingForCaseBracket;
	boolean unindentNextLine;

	SwitchVariables sw;
	Stack<SwitchVariables> swVector; // stack vector of switch variables

	// event table variables
	boolean nextLineIsEventTable; // begin event table is reached
	boolean isInEventTable; // need to indent an event table

	// stringstream for trace

	// ---------------------------- functions for ASEnhancer Class
	// -------------------------------------

	/**
	 * ASEnhancer constructor
	 */
	public ASEnhancer()
	{
	}

	/**
	 * initialize the ASEnhancer.
	 *
	 * init() is called each time an ASFormatter object is initialized.
	 */
	void init(int fileType, int _indentLength, String _indentString,
			boolean _caseIndent, boolean _emptyLineFill)
	{
		// formatting variables from ASFormatter and ASBeautifier
		init(fileType);
		indentLength = _indentLength;
        useTabs = _indentString.charAt(0)=='\t';

		caseIndent = _caseIndent;
		emptyLineFill = _emptyLineFill;

		// unindent variables
		lineNumber = 0;
		bracketCount = 0;
		isInComment = false;
		isInQuote = false;
		switchDepth = 0;
		lookingForCaseBracket = false;
		unindentNextLine = false;

		// switch struct and vector
		sw = new SwitchVariables(0, 0, false);
		swVector=new Stack<SwitchVariables>();

		nextLineIsEventTable = false;
		isInEventTable = false;
	}

	/**
	 * additional formatting for line of source code. every line of source code
	 * in a source code file should be sent one after the other to this
	 * function. indents event tables unindents the case blocks
	 *
	 * @param line
	 *            the original formatted line will be updated if necessary.
	 */
	public void enhance(StringBuffer line)
	{
		boolean isSpecialChar = false;
		int lineLength = line.length();

		lineNumber++;

		// check for beginning of event table
		if (nextLineIsEventTable)
		{
			isInEventTable = true;
			nextLineIsEventTable = false;
		}

		if (lineLength == 0 && !isInEventTable && !emptyLineFill)
			return;

		// test for unindent on attached brackets
		if (unindentNextLine)
		{
			sw.setUnindentDepth(sw.getUnindentDepth() + 1);
			sw.setUnindentCase(true);
			unindentNextLine = false;
		}

		// parse characters in the current line.

		for (int i = 0; i < lineLength; i++)
		{
			char ch = line.charAt(i);

			// bypass whitespace
			if (isWhiteSpace(ch))
				continue;

			// handle special characters (i.e. backslash+character such as \n,
			// \t, ...)
			if (isSpecialChar)
			{
				isSpecialChar = false;
				continue;
			}
			if (!(isInComment)
					&& line.indexOf("\\\\",i)==i)
			{
				i++;
				continue;
			}
			if (!(isInComment) && ch == '\\')
			{
				isSpecialChar = true;
				continue;
			}

			// handle quotes (such as 'x' and "Hello Dolly")
			if (!(isInComment) && (ch == '"' || ch == '\''))
			{
				if (!isInQuote)
				{
					quoteChar = ch;
					isInQuote = true;
				} else if (quoteChar == ch)
				{
					isInQuote = false;
					continue;
				}
			}

			if (isInQuote)
				continue;

			// handle comments

			if (!(isInComment) && line.indexOf("//",i)==i)
			{
				// check for windows line markers
				if ((i+2)<line.length() && line.charAt(i + 2)>0xf0)
				lineNumber--;
				break; // finished with the line
			} else if (!(isInComment)
					&& line.indexOf("/*", i)==i)
			{
				isInComment = true;
				i++;
				continue;
			} else if ((isInComment)
					&& line.indexOf("*/",i)==i)
			{
				isInComment = false;
				i++;
				continue;
			}

			if (isInComment)
				continue;

			// if we have reached this far then we are NOT in a comment or
			// String of special characters

			if (line.charAt(i) == '{')
				bracketCount++;

			if (line.charAt(i) == '}')
				bracketCount--;

			boolean isPotentialKeyword = isCharPotentialHeader(line, i);

			// ---------------- process event tables
			// --------------------------------------

			// check for event table begin
			if (isPotentialKeyword && !isJavaStyle())
			{
				if (findKeyword(line, i, "BEGIN_EVENT_TABLE")
						|| findKeyword(line, i, "BEGIN_MESSAGE_MAP"))
					nextLineIsEventTable = true;

				// check for event table end
				if (findKeyword(line, i, "END_EVENT_TABLE")
						|| findKeyword(line, i, "END_MESSAGE_MAP"))
					isInEventTable = false;
			}

			// ---------------- process switch statements
			// ---------------------------------

			if (isPotentialKeyword && findKeyword(line, i, "switch"))
			{
				switchDepth++;
				swVector.push(sw); // save current variables
				sw.setSwitchBracketCount(0);
				sw.setUnindentCase(false); // don't clear case until end of
											// switch
				i += 5; // bypass switch statement
				continue;
			}

			// just want switch statements from this point

			if (caseIndent || switchDepth == 0) // from here just want switch
												// statements
				continue;

			if (line.charAt(i) == '{')
			{
				sw.setSwitchBracketCount(sw.getSwitchBracketCount() + 1);
				if (lookingForCaseBracket) // if 1st after case statement
				{
					sw.setUnindentCase(true); // unindenting this case
					sw.setUnindentDepth(sw.getUnindentDepth() + 1);
					lookingForCaseBracket = false; // not looking now
				}
				continue;
			}

			lookingForCaseBracket = false; // no opening bracket, don't indent

			if (line.charAt(i) == '}') // if close bracket
			{
				sw.setSwitchBracketCount(sw.getSwitchBracketCount() - 1);
				if (sw.getSwitchBracketCount() == 0) // if end of switch
														// statement
				{
					switchDepth--; // one less switch
					sw = swVector.pop(); // restore sw struct && remove last
											// entry from stack
				}
				continue;
			}

			// look for case or default header

			if (isPotentialKeyword
					&& (findKeyword(line, i, "case") || findKeyword(line, i,
							"default")))
			{
				if (sw.isUnindentCase()) // if unindented last case
				{
					sw.setUnindentCase(false); // stop unindenting previous case
					sw.setUnindentDepth(sw.getUnindentDepth() - 1); // reduce
																	// depth
				}
				boolean isInQuote = false;
				char quoteChar = ' ';
				for (; i < lineLength; i++) // find colon
				{
					if (isInQuote)
					{
						if (line.charAt(i) == '\\')
						{
							i++; // bypass next char
							continue;
						} else if (line.charAt(i) == quoteChar) // check ending
																// quote
						{
							isInQuote = false;
							quoteChar = ' ';
							continue;
						} else
						{
							continue; // must close quote before continuing
						}
					}
					if (line.charAt(i) == '\'' || line.charAt(i) == '\"') // check
																			// opening
																			// quote
					{
						isInQuote = true;
						quoteChar = line.charAt(i);
						continue;
					}
					if (line.charAt(i) == ':')
					{
						if ((i + 1 < lineLength) && (line.charAt(i + 1) == ':'))
							i++; // bypass scope resolution operator
						else
							break; // found it
					}
				}
				i++;
				for (; i < lineLength; i++) // bypass whitespace
				{
					if (!(isWhiteSpace(line.charAt(i))))
						break;
				}
				if (i < lineLength) // check for bracket
				{
					if (line.charAt(i) == '{') // if bracket found
					{
						sw
								.setSwitchBracketCount(sw
										.getSwitchBracketCount() + 1);
						unindentNextLine = true; // start unindenting on next
													// line
						continue;
					}
				}
				lookingForCaseBracket = true; // bracket must be on next line
				i--; // need to check for comments
				continue;
			}

			if (isPotentialKeyword)
			{
				String name = getCurrentWord(line, i); // bypass the entire name
				i += name.length() - 1;
			}

		} // end of for loop

		if (isInEventTable) // if need to indent
			indentLine(line, 1); // do it

		if (sw.getUnindentDepth() > 0) // if need to unindent
			unindentLine(line, sw.getUnindentDepth()); // do it
	}

	/**
	 * indent a line by a given number of tabsets by inserting leading
	 * whitespace to the line argument.
	 *
	 * @param line
	 *            a pointer to the line to indent.
	 * @param unindent
	 *            the number of tabsets to insert.
	 * @return the number of characters inserted.
	 */
	int indentLine(StringBuffer line, int indent)
	{
		if (line.length() == 0 && !emptyLineFill)
			return 0;

		int charsToInsert; // number of chars to insert

		if (useTabs) // if formatted with tabs
		{
			charsToInsert = indent; // tabs to insert
			line.insert((int) 0, ASUtils.repeat(charsToInsert, '\t')); // insert
																		// the
																		// tabs
		} else
		{
			charsToInsert = indent * indentLength; // compute chars to insert
			line.insert((int) 0, ASUtils.repeat(charsToInsert, ' ')); // insert
																		// the
																		// spaces
		}

		return charsToInsert;
	}

	/**
	 * unindent a line by a given number of tabsets by erasing the leading
	 * whitespace from the line argument.
	 *
	 * @param line
	 *            a pointer to the line to unindent.
	 * @param unindent
	 *            the number of tabsets to erase.
	 * @return the number of characters erased.
	 */
	int unindentLine(StringBuffer line, int unindent)
	{
		int whitespace = ASUtils.findFirstNotOf(line, " \t", 0);

		if (whitespace == -1) // if line is blank
			whitespace = line.length(); // must remove padding, if any

		if (whitespace == 0)
			return 0;

		int charsToErase; // number of chars to erase

		if (useTabs) // if formatted with tabs
		{
			charsToErase = unindent; // tabs to erase
			if (charsToErase <= whitespace) // if there is enough whitespace
				line.delete(0, charsToErase); // erase the tabs
			else
				charsToErase = 0;
		} else
		{
			charsToErase = unindent * indentLength; // compute chars to erase
			if (charsToErase <= whitespace) // if there is enough whitespace
				line.delete(0, charsToErase); // erase the spaces
			else
				charsToErase = 0;
		}

		return charsToErase;
	}
}
