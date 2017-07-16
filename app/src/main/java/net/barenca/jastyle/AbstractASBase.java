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

import net.barenca.jastyle.constants.FileType;

public class AbstractASBase
{

	private int fileType; // a value from enum FileType

	protected void init(final int fileTypeArg)
	{
		fileType = fileTypeArg;
	}

	protected boolean isCStyle()
	{
		return (fileType == FileType.C_TYPE);
	}

	protected boolean isJavaStyle()
	{
		return (fileType == FileType.JAVA_TYPE);
	}

	protected boolean isSharpStyle()
	{
		return (fileType == FileType.SHARP_TYPE);
	}

	// check if a specific character can be used in a legal
	// variable/method/class name
    protected boolean isLegalNameChar(char ch)
	{
		if (isWhiteSpace(ch))
		{
			return false;
		}

		if (ch > 127)
		{
			return false;
		}

		return (Character.isLetterOrDigit(ch) || ch == '.'
				|| ch == '_' || (isJavaStyle() && ch == '$') || (isSharpStyle() && ch == '@')); // may
		// be
		// used
		// as
		// a
		// prefix
	}

	// check if a specific character can be part of a header
	protected boolean isCharPotentialHeader(StringBuffer line, final int i)
	{
		assert (!isWhiteSpace(line.charAt(i))) : "White spaces not permitted at position "+ i;
		char prevCh = ' ';
		if (i > 0)
		{
			prevCh = line.charAt(i - 1);
		}
        return !isLegalNameChar(prevCh) && isLegalNameChar(line.charAt(i));
    }

	// check if a specific character can be part of an operator
	protected boolean isCharPotentialOperator(char ch)
	{
		assert (!isWhiteSpace(ch)): "White spaces not permitted as a Potential Operator";
		if (ch > 127)
		{
			return false;
		}
		return (ASUtils.isPunct(ch) && ch != '{' && ch != '}' && ch != '('
				&& ch != ')' && ch != '[' && ch != ']' && ch != ';'
				&& ch != ',' && ch != '#' && ch != '\\' && ch != '\'' && ch != '\"');
	}

	// check if a specific character is a whitespace character
	protected boolean isWhiteSpace(char ch)
	{
		return (ch == ' ' || ch == '\t');
	}

	// peek at the next unread character.
    protected char peekNextChar(StringBuffer line, int start)
	{
		final int peekNum = ASUtils.findFirstNotOf(line, " \t", start + 1);
		if (peekNum == -1)
		{
			return ' ';
		}
		return line.charAt(peekNum);
	}

	// check if a specific line position contains a keyword.
    protected boolean findKeyword(StringBuffer line, int i, String keyword)
	{
		assert (isCharPotentialHeader(line, i)): line + " is not a potential header";
		// check the word
		final int keywordLength = keyword.length();

		if (line.indexOf(keyword, i)!=i)
		{
			return false;
		}
		// check that this is not part of a longer word
		final int wordEnd = i + keywordLength;
		if (wordEnd == line.length())
		{
			return true;
		}
		if (isLegalNameChar(line.charAt(wordEnd)))
		{
			return false;
		}
		// is not a keyword if part of a definition
		final char peekChar = peekNextChar(line, wordEnd - 1);
		if (peekChar == ',' || peekChar == ')')
		{
			return false;
		}
		return true;
	}

	// get the current word on a line
	// i must point to the beginning of the word
    protected String getCurrentWord(StringBuffer line, int charNum)
	{
		final int lineLength = line.length();
		int i;
		for (i = charNum; i < lineLength; i++)
		{
			if (!isLegalNameChar(line.charAt(i)))
			{
				break;
			}
		}
		return line.substring(charNum, i );
	}
}
