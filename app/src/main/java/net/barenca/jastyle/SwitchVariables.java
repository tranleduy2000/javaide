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

class SwitchVariables
{

	private int switchBracketCount = 0;
	private int unindentDepth = 0;
	private boolean unindentCase = false;

	public SwitchVariables()
	{
	}

	/**
	 *
	 * @param switchBracketCount
	 * @param unindentDepth
	 * @param unindentCase
	 */
	public SwitchVariables(int switchBracketCount, int unindentDepth,
			boolean unindentCase)
	{
		super();
		this.switchBracketCount = switchBracketCount;
		this.unindentDepth = unindentDepth;
		this.unindentCase = unindentCase;
	}

	public int getSwitchBracketCount()
	{
		return switchBracketCount;
	}

	public void setSwitchBracketCount(int switchBracketCount)
	{
		this.switchBracketCount = switchBracketCount;
	}

	public int getUnindentDepth()
	{
		return unindentDepth;
	}

	public void setUnindentDepth(int unindentDepth)
	{
		this.unindentDepth = unindentDepth;
	}

	public boolean isUnindentCase()
	{
		return unindentCase;
	}

	public void setUnindentCase(boolean unindentCase)
	{
		this.unindentCase = unindentCase;
	}

}
