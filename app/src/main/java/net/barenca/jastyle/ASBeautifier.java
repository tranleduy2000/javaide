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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import net.barenca.jastyle.constants.FileType;

public class ASBeautifier extends AbstractASBase
{
	private List<String> headers;
	private List<String> nonParenHeaders;
	private List<String> preBlockStatements;
	private List<String> assignmentOperators;
	private List<String> nonAssignmentOperators;
	private List<String> indentableHeaders;
	private ASSourceIterator sourceIterator;
	private Stack<ASBeautifier> waitingBeautifierStack;
	private Stack<ASBeautifier> activeBeautifierStack;
	private Stack<Integer> waitingBeautifierStackLengthStack;
	private Stack<Integer> activeBeautifierStackLengthStack;
	private Stack<String> headerStack;
	private Stack<Stack<String>> tempStacks;
	private Stack<Integer> blockParenDepthStack;
	private Stack<Boolean> blockStatementStack;
	private Stack<Boolean> parenStatementStack;
	private Stack<Boolean> bracketBlockStateStack;
	private Stack<Integer> inStatementIndentStack;
	private Stack<Integer> inStatementIndentStackSizeStack;
	private Stack<Integer> parenIndentStack;

	private int beautifierFileType = 9; // initialized with an invalid type
	private String indentString;
	private String currentHeader;
	private String previousLastLineHeader;
	private String probationHeader;
	private boolean isInQuote;
	private boolean isInVerbatimQuote;
	private boolean haveLineContinuationChar;
	private boolean isInComment;
	private boolean isInCase;
	private boolean isInQuestion;
	private boolean isInStatement;
	private boolean isInHeader;
	private boolean isInTemplate;
	private boolean isInDefine;
	private boolean isInDefineDefinition;
	private boolean classIndent;
	private boolean isInClassHeader;
	private boolean isInClassHeaderTab;
	private boolean switchIndent;
	private boolean caseIndent;
	private boolean namespaceIndent;
	private boolean bracketIndent;
	private boolean blockIndent;
	private boolean labelIndent;
	private boolean preprocessorIndent;
	private boolean isInConditional;
	private boolean isMinimalConditinalIndentSet;
	private boolean shouldForceTabIndentation;
	private boolean emptyLineFill;
	private boolean backslashEndsPrevLine;
	private boolean blockCommentNoIndent;
	private boolean blockCommentNoBeautify;
	private boolean previousLineProbationTab;
	private int fileType;
	private int minConditionalIndent;
	private int parenDepth;
	private int indentLength;
	private int blockTabCount;
	private int leadingWhiteSpaces;
	private int maxInStatementIndent;
	private int templateDepth;
	private int prevFinalLineSpaceTabCount;
	private int prevFinalLineTabCount;
	private int defineTabCount;
	private char quoteChar;
	private char prevNonSpaceCh;
	private char currentNonSpaceCh;
	private char currentNonLegalCh;
	private char prevNonLegalCh;

	// variables set by ASFormatter - must be updated in activeBeautifierStack
	protected int inLineNumber;
	protected boolean lineCommentNoBeautify;
	protected boolean isNonInStatementArray;
	protected boolean isSharpAccessor;

	/**
	 * ASBeautifier's constructor<br>
	 * Por default fileType = FileType.JAVA_TYPE
	 */
	public ASBeautifier()
	{
		waitingBeautifierStack = null;
		activeBeautifierStack = null;
		waitingBeautifierStackLengthStack = null;
		activeBeautifierStackLengthStack = null;

		headerStack = null;
		tempStacks = null;
		blockParenDepthStack = null;
		blockStatementStack = null;
		parenStatementStack = null;
		bracketBlockStateStack = null;
		inStatementIndentStack = null;
		inStatementIndentStackSizeStack = null;
		parenIndentStack = null;
		sourceIterator = null;

		isMinimalConditinalIndentSet = false;
		shouldForceTabIndentation = false;

		setSpaceIndentation(4);
		setMaxInStatementIndentLength(40);
		setClassIndent(false);
		setSwitchIndent(false);
		setCaseIndent(false);
		setBlockIndent(false);
		setBracketIndent(false);
		setNamespaceIndent(false);
		setLabelIndent(false);
		setEmptyLineFill(false);
		fileType = FileType.JAVA_TYPE;
		setJavaStyle();
		setPreprocessorIndent(false);
	}

	/**
	 * ASBeautifier's copy constructor must explicitly call the base class copy
	 * constructor
	 *
	 * @param other
	 */
	public ASBeautifier(final ASBeautifier other)
	{
		// these don't need to copy the stack
		waitingBeautifierStack = null;
		activeBeautifierStack = null;
		waitingBeautifierStackLengthStack = null;
		activeBeautifierStackLengthStack = null;

		// vector '=' operator performs a DEEP copy of all elements in the
		// vector

		headerStack = other.headerStack;

		tempStacks = new Stack<Stack<String>>();

		for (final Iterator<Stack<String>> iter = other.tempStacks.iterator(); iter
				.hasNext();)
		{
			tempStacks.push(iter.next());
		}
		blockParenDepthStack = other.blockParenDepthStack;

		blockStatementStack = other.blockStatementStack;

		parenStatementStack = other.parenStatementStack;

		bracketBlockStateStack = other.bracketBlockStateStack;

		inStatementIndentStack = other.inStatementIndentStack;

		inStatementIndentStackSizeStack = other.inStatementIndentStackSizeStack;

		parenIndentStack = other.parenIndentStack;

		sourceIterator = other.sourceIterator;

		// protected variables
		// variables set by ASFormatter
		// must also be updated in activeBeautifierStack
		inLineNumber = other.inLineNumber;
		lineCommentNoBeautify = other.lineCommentNoBeautify;
		isNonInStatementArray = other.isNonInStatementArray;
		isSharpAccessor = other.isSharpAccessor;

		// private variables
		indentString = other.indentString;
		currentHeader = other.currentHeader;
		previousLastLineHeader = other.previousLastLineHeader;
		probationHeader = other.probationHeader;
		isInQuote = other.isInQuote;
		isInVerbatimQuote = other.isInVerbatimQuote;
		haveLineContinuationChar = other.haveLineContinuationChar;
		isInComment = other.isInComment;
		isInCase = other.isInCase;
		isInQuestion = other.isInQuestion;
		isInStatement = other.isInStatement;
		isInHeader = other.isInHeader;
		isInTemplate = other.isInTemplate;
		isInDefine = other.isInDefine;
		isInDefineDefinition = other.isInDefineDefinition;
		classIndent = other.classIndent;
		isInClassHeader = other.isInClassHeader;
		isInClassHeaderTab = other.isInClassHeaderTab;
		switchIndent = other.switchIndent;
		caseIndent = other.caseIndent;
		namespaceIndent = other.namespaceIndent;
		bracketIndent = other.bracketIndent;
		blockIndent = other.blockIndent;
		labelIndent = other.labelIndent;
		preprocessorIndent = other.preprocessorIndent;
		isInConditional = other.isInConditional;
		isMinimalConditinalIndentSet = other.isMinimalConditinalIndentSet;
		shouldForceTabIndentation = other.shouldForceTabIndentation;
		emptyLineFill = other.emptyLineFill;
		backslashEndsPrevLine = other.backslashEndsPrevLine;
		blockCommentNoIndent = other.blockCommentNoIndent;
		blockCommentNoBeautify = other.blockCommentNoBeautify;
		previousLineProbationTab = other.previousLineProbationTab;
		fileType = other.fileType;
		minConditionalIndent = other.minConditionalIndent;
		parenDepth = other.parenDepth;
		indentLength = other.indentLength;
		blockTabCount = other.blockTabCount;
		leadingWhiteSpaces = other.leadingWhiteSpaces;
		maxInStatementIndent = other.maxInStatementIndent;
		templateDepth = other.templateDepth;
		prevFinalLineSpaceTabCount = other.prevFinalLineSpaceTabCount;
		prevFinalLineTabCount = other.prevFinalLineTabCount;
		defineTabCount = other.defineTabCount;
		quoteChar = other.quoteChar;
		prevNonSpaceCh = other.prevNonSpaceCh;
		currentNonSpaceCh = other.currentNonSpaceCh;
		currentNonLegalCh = other.currentNonLegalCh;
		prevNonLegalCh = other.prevNonLegalCh;
	}

	/*
	 * initialize the static vars
	 */
	private void initStatic()
	{
		if (fileType == beautifierFileType) // don't build unless necessary
		{
			return;
		}

		beautifierFileType = fileType;

		headers = new ArrayList<String>();
		nonParenHeaders = new ArrayList<String>();
		assignmentOperators = new ArrayList<String>();
		nonAssignmentOperators = new ArrayList<String>();
		preBlockStatements = new ArrayList<String>();
		indentableHeaders = new ArrayList<String>();

		ASResource.buildHeaders(headers, fileType, true);
		ASResource.buildNonParenHeaders(nonParenHeaders, fileType, true);
		ASResource.buildAssignmentOperators(assignmentOperators);
		ASResource.buildNonAssignmentOperators(nonAssignmentOperators);
		ASResource.buildPreBlockStatements(preBlockStatements, fileType);
		ASResource.buildIndentableHeaders(indentableHeaders);
	}

	/**
	 * initialize the ASBeautifier.
	 *
	 * init() should be called every time a ABeautifier object is to start
	 * beautifying a NEW source file. init() recieves a pointer to a DYNAMICALLY
	 * CREATED ASSourceIterator object that will be used to iterate through the
	 * source code. This object will be deleted during the ASBeautifier's
	 * destruction, and thus should not be deleted elsewhere.
	 *
	 * @param iter
	 *            a pointer to the DYNAMICALLY CREATED ASSourceIterator object.
	 */
	public void init(final ASSourceIterator iter)
	{
		sourceIterator = iter;
		init();
	}

	/**
	 * initialize the ASBeautifier.
	 */
	void init()
	{
		initStatic();
		init(getFileType());

		waitingBeautifierStack = new Stack<ASBeautifier>();
		activeBeautifierStack = new Stack<ASBeautifier>();

		waitingBeautifierStackLengthStack = new Stack<Integer>();
		activeBeautifierStackLengthStack = new Stack<Integer>();

		headerStack = new Stack<String>();

		tempStacks = new Stack<Stack<String>>();
		tempStacks.add(new Stack<String>());

		blockParenDepthStack = new Stack<Integer>();
		blockStatementStack = new Stack<Boolean>();
		parenStatementStack = new Stack<Boolean>();

		bracketBlockStateStack = new Stack<Boolean>();
		bracketBlockStateStack.push(true);

		inStatementIndentStack = new Stack<Integer>();
		inStatementIndentStackSizeStack = new Stack<Integer>();
		inStatementIndentStackSizeStack.push(0);
		parenIndentStack = new Stack<Integer>();

		previousLastLineHeader = null;
		currentHeader = null;

		isInQuote = false;
		isInVerbatimQuote = false;
		haveLineContinuationChar = false;
		isInComment = false;
		isInStatement = false;
		isInCase = false;
		isInQuestion = false;
		isInClassHeader = false;
		isInClassHeaderTab = false;
		isInHeader = false;
		isInTemplate = false;
		isInConditional = false;
		templateDepth = 0;
		parenDepth = 0;
		blockTabCount = 0;
		leadingWhiteSpaces = 0;
		prevNonSpaceCh = '{';
		currentNonSpaceCh = '{';
		prevNonLegalCh = '{';
		currentNonLegalCh = '{';
		quoteChar = ' ';
		prevFinalLineSpaceTabCount = 0;
		prevFinalLineTabCount = 0;
		probationHeader = null;
		backslashEndsPrevLine = false;
		isInDefine = false;
		isInDefineDefinition = false;
		defineTabCount = 0;
		lineCommentNoBeautify = false;
		blockCommentNoIndent = false;
		blockCommentNoBeautify = false;
		previousLineProbationTab = false;
		isNonInStatementArray = false;
		isSharpAccessor = false;
		inLineNumber = 0;
	}

	/**
	 * set indentation style to C/C++.
	 */
	public void setCStyle()
	{
		fileType = FileType.C_TYPE;
	}

	/**
	 * set indentation style to Java.
	 */
	public void setJavaStyle()
	{
		fileType = FileType.JAVA_TYPE;
	}

	/**
	 * set indentation style to C#.
	 */
	public void setSharpStyle()
	{
		fileType = FileType.SHARP_TYPE;
	}

	/**
	 * indent using one tab per indentation
	 *
	 * @param length
	 * @param forceTabs
	 */
	public void setTabIndentation(final int length)
	{
		setTabIndentation(length, false);
	}

	/**
	 * indent using one tab per indentation
	 *
	 * @param length
	 * @param forceTabs
	 */
	public void setTabIndentation(final int length, final boolean forceTabs)
	{
		indentString = "\t";
		indentLength = length;
		shouldForceTabIndentation = forceTabs;

		if (!isMinimalConditinalIndentSet)
		{
			minConditionalIndent = indentLength * 2;
		}
	}

	/**
	 * indent using a number of spaces per indentation.
	 *
	 * @param length
	 *            number of spaces per indent.
	 */
	public void setSpaceIndentation(final int length)
	{
		indentString = ASUtils.repeat(length, ' ');
		indentLength = length;

		if (!isMinimalConditinalIndentSet)
		{
			minConditionalIndent = indentLength * 2;
		}
	}

	/**
	 * set the maximum indentation between two lines in a multi-line statement.
	 *
	 * @param max
	 *            maximum indentation length.
	 */
	public void setMaxInStatementIndentLength(final int max)
	{
		maxInStatementIndent = max;
	}

	/**
	 * set the minimum indentation between two lines in a multi-line condition.
	 *
	 * @param min
	 *            minimal indentation length.
	 */
	public void setMinConditionalIndentLength(final int min)
	{
		minConditionalIndent = min;
		isMinimalConditinalIndentSet = true;
	}

	/**
	 * set the state of the bracket indentation option. If true, brackets will
	 * be indented one additional indent.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setBracketIndent(final boolean state)
	{
		bracketIndent = state;
	}

	/**
	 * set the state of the block indentation option. If true, entire blocks
	 * will be indented one additional indent, similar to the GNU indent style.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setBlockIndent(final boolean state)
	{
		blockIndent = state;
	}

	/**
	 * set the state of the class indentation option. If true, C++ class
	 * definitions will be indented one additional indent.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setClassIndent(final boolean state)
	{
		classIndent = state;
	}

	/**
	 * set the state of the switch indentation option. If true, blocks of
	 * 'switch' statements will be indented one additional indent.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setSwitchIndent(final boolean state)
	{
		switchIndent = state;
	}

	/**
	 * set the state of the case indentation option. If true, lines of 'case'
	 * statements will be indented one additional indent.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setCaseIndent(final boolean state)
	{
		caseIndent = state;
	}

	/**
	 * set the state of the namespace indentation option. If true, blocks of
	 * 'namespace' statements will be indented one additional indent. Otherwise,
	 * NO indentation will be added.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setNamespaceIndent(final boolean state)
	{
		namespaceIndent = state;
	}

	/**
	 * set the state of the label indentation option. If true, labels will be
	 * indented one indent LESS than the current indentation level. If false,
	 * labels will be flushed to the left with NO indent at all.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setLabelIndent(boolean state)
	{
		labelIndent = state;
	}

	/**
	 * set the state of the preprocessor indentation option. If true, multiline
	 * #define statements will be indented.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setPreprocessorIndent(boolean state)
	{
		preprocessorIndent = state;
	}

	/**
	 * set the state of the empty line fill option. If true, empty lines will be
	 * filled with the whitespace. of their previous lines. If false, these
	 * lines will remain empty.
	 *
	 * @param state
	 *            state of option.
	 */
	public void setEmptyLineFill(boolean state)
	{
		emptyLineFill = state;
	}

	/**
	 * get the file type.
	 *
	 * @return
	 */
	public int getFileType()
	{
		return fileType;
	}

	/**
	 * get the number of spaces per indent
	 *
	 * @return value of indentLength option.
	 */
	public int getIndentLength()
	{
		return indentLength;
	}

	/**
	 * get the char used for indentation, space or tab
	 *
	 * @return the char used for indentation.
	 */
	public String getIndentString()
	{
		return indentString;
	}

	/**
	 * get the state of the block indentation option.
	 *
	 * @return state of blockIndent option.
	 */
	public boolean isBlockIndent()
	{
		return blockIndent;
	}

	/**
	 * get the state of the bracket indentation option.
	 *
	 * @return state of bracketIndent option.
	 */
	public boolean isBracketIndent()
	{
		return bracketIndent;
	}

	/**
	 * get the state of the case indentation option. If true, lines of 'case'
	 * statements will be indented one additional indent.
	 *
	 * @return state of caseIndent option.
	 */
	public boolean isCaseIndent()
	{
		return caseIndent;
	}

	/**
	 * get the state of the empty line fill option. If true, empty lines will be
	 * filled with the whitespace. of their previous lines. If false, these
	 * lines will remain empty.
	 *
	 * @return state of emptyLineFill option.
	 */
	public boolean isEmptyLineFill()
	{
		return emptyLineFill;
	}

	/**
	 * check if there are any indented lines ready to be read by nextLine()
	 *
	 * @return are there any indented lines ready?
	 */
	public boolean hasMoreLines()
	{
		return sourceIterator.hasMoreLines();
	}

	/**
	 * get the next indented line.
	 *
	 * @return indented line.
	 */
	public StringBuffer nextLine()
	{
		return beautify(sourceIterator.nextLine());
	}

	/**
	 * beautify a line of source code. every line of source code in a source
	 * code file should be sent one after the other to the beautify method.
	 *
	 * @return the indented line.
	 * @param originalLine
	 *            the original unindented line.
	 */
	protected StringBuffer beautify(StringBuffer originalLine)
	{
		StringBuffer line = null;
		boolean isInLineComment = false;
		boolean lineStartsInComment = false;
		boolean isInClass = false;
		boolean isInSwitch = false;
		boolean isInOperator = false;
		boolean isSpecialChar = false;
		boolean haveCaseIndent = false;
		boolean closingBracketReached = false;
		boolean shouldIndentBrackettedLine = true;
		boolean previousLineProbation = (probationHeader != null);
		final boolean isInQuoteContinuation = isInVerbatimQuote
				| haveLineContinuationChar;
		char ch = ' ';
		char prevCh;
		char tempCh;
		int tabCount = 0;
		int spaceTabCount = 0;
		int lineOpeningBlocksNum = 0;
		int lineClosingBlocksNum = 0;

		StringBuffer outBuffer = new StringBuffer(); // the newly idented line
		// is bufferd here
		String lastLineHeader = "";

		currentHeader = null;
		lineStartsInComment = isInComment;
		blockCommentNoBeautify = blockCommentNoIndent;
		previousLineProbationTab = false;
		haveLineContinuationChar = false;

		// handle and remove white spaces around the line:
		// If not in comment, first find out size of white space before line,
		// so that possible comments starting in the line continue in
		// relation to the preliminary white-space.
		if (isInQuoteContinuation)
		{
			// trim a single space added by ASFormatter, otherwise leave it
			// alone
			if (!(originalLine.length() == 1 && originalLine.charAt(0) == ' '))
			{
				line = new StringBuffer(originalLine);
			}
		} else if (!isInComment)
		{
			final int strlen = originalLine.length();
			leadingWhiteSpaces = 0;

			for (int j = 0; j < strlen
					&& isWhiteSpace(originalLine.charAt(j)); j++)
			{
				if (originalLine.charAt(j) == '\t')
				{
					leadingWhiteSpaces += indentLength;
				}
				else
				{
					leadingWhiteSpaces++;
				}
			}
			line = new StringBuffer(originalLine.toString().trim());
		} else
		{
			// convert leading tabs to spaces
			StringBuffer spaceTabs = new StringBuffer();

			spaceTabs.append(ASUtils.repeat(indentLength, ' '));

			StringBuffer newLine = new StringBuffer(originalLine.length() + 16);
			newLine.append(originalLine);
			int strlen = newLine.length();

			for (int j = 0; j < leadingWhiteSpaces && j < strlen; j++)
			{
				if (newLine.charAt(j) == '\t')
				{
					newLine.replace(j, j + 1, spaceTabs.toString());
					strlen = newLine.length();
				}
			}

			// trim the comment leaving the new leading whitespace
			int trimSize = 0;
			strlen = newLine.length();

			while (trimSize < strlen && trimSize < leadingWhiteSpaces
					&& isWhiteSpace(newLine.charAt(trimSize)))
			{
				trimSize++;
			}

			while (trimSize < strlen
					&& isWhiteSpace(newLine.charAt(strlen - 1)))
			{
				strlen--;
			}

			line = new StringBuffer(newLine.substring(trimSize, strlen));
			int spacesToDelete;
			final int trimEnd = ASUtils.findLastNotOf(line, ASUtils.WHITE_SPACE);
			if (trimEnd == -1)
			{
				spacesToDelete = line.length();
			}
			else
			{
				spacesToDelete = line.length() - 1 - trimEnd;
			}
			if (spacesToDelete > 0)
			{
				line.delete(trimEnd + 1, trimEnd + 1 + spacesToDelete);
			}
		}

		if (line.length() == 0)
		{
			if (backslashEndsPrevLine) // must continue to clear variables
			{
				line.append(" ");
			}
			else if (emptyLineFill && !isInQuoteContinuation
					&& headerStack.size() > 0)
			{
				return preLineWS(prevFinalLineSpaceTabCount,
						prevFinalLineTabCount);
			}
			else
			{
				return line;
			}
		}

		// handle preprocessor commands
		// except C# region and endregion

		if (!isInComment && (line.charAt(0) == '#' || backslashEndsPrevLine)
				&& line.indexOf("#region") != 0
				&& line.indexOf("#endregion") != 0)
		{
			if (line.charAt(0) == '#')
			{
				String preproc = line.toString().trim();

				// When finding a multi-lined #define statement, the original
				// beautifier
				// 1. sets its isInDefineDefinition flag
				// 2. clones a new beautifier that will be used for the actual
				// indentation
				// of the #define. This clone is put into the
				// activeBeautifierStack in order
				// to be called for the actual indentation.
				// The original beautifier will have isInDefineDefinition =
				// true, isInDefine = false
				// The cloned beautifier will have isInDefineDefinition = true,
				// isInDefine = true
				if (preprocessorIndent && preproc.indexOf("define") == 0
						&& line.charAt(line.length() - 1) == '\\')
				{
					if (!isInDefineDefinition)
					{
						// this is the original beautifier
						isInDefineDefinition = true;

						// push a new beautifier into the active stack
						// this beautifier will be used for the indentation of
						// this define
						ASBeautifier defineBeautifier = new ASBeautifier(this);
						activeBeautifierStack.add(defineBeautifier);
					} else
					{
						// the is the cloned beautifier that is in charge of
						// indenting the #define.
						isInDefine = true;
					}
				} else if (preproc.startsWith("if"))
				{
					// push a new beautifier into the stack
					waitingBeautifierStackLengthStack
							.add(waitingBeautifierStack.size());
					activeBeautifierStackLengthStack.add(activeBeautifierStack
							.size());
					waitingBeautifierStack.add(new ASBeautifier(this));
				} else if (preproc.indexOf("else") == 0)
				{
					if (waitingBeautifierStack != null
							&& !waitingBeautifierStack.empty())
					{
						// MOVE current waiting beautifier to active stack.
						activeBeautifierStack.add(waitingBeautifierStack.pop());
					}
				} else if (preproc.indexOf("elif") == 0)
				{
					if (waitingBeautifierStack != null
							&& !waitingBeautifierStack.empty())
					{
						// append a COPY current waiting beautifier to active
						// stack, WITHOUT deleting the original.
						activeBeautifierStack.add(new ASBeautifier(
								waitingBeautifierStack.peek()));
					}
				} else if (preproc.indexOf("endif") == 0)
				{
					int stackLength;
					if (waitingBeautifierStackLengthStack != null
							&& !waitingBeautifierStackLengthStack.empty())
					{
						stackLength = waitingBeautifierStackLengthStack.pop();

						while (waitingBeautifierStack.size() > stackLength)
						{
							waitingBeautifierStack.pop();
						}
					}

					if (!activeBeautifierStackLengthStack.empty())
					{
						stackLength = activeBeautifierStackLengthStack.pop();

						while (activeBeautifierStack.size() > stackLength)
						{
							activeBeautifierStack.pop();
						}
					}
				}
			}

			// check if the last char is a backslash
			if (line.length() > 0)
			{
				backslashEndsPrevLine = (line.charAt(line.length() - 1) == '\\');
			}
			else
			{
				backslashEndsPrevLine = false;
			}

			// check if this line ends a multi-line #define
			// if so, use the #define's cloned beautifier for the line's
			// indentation
			// and then remove it from the active beautifier stack and delete
			// it.
			if (!backslashEndsPrevLine && isInDefineDefinition && !isInDefine)
			{
				isInDefineDefinition = false;
				ASBeautifier defineBeautifier = activeBeautifierStack.pop();
				return defineBeautifier.beautify(line);
			}

			// unless this is a multi-line #define, return this precompiler line
			// as is.
			if (!isInDefine && !isInDefineDefinition)
			{
				return originalLine;
			}
		}

		// if there exists any worker beautifier in the activeBeautifierStack,
		// then use it instead of me to indent the current line.
		// variables set by ASFormatter must be updated.
		if (!isInDefine && activeBeautifierStack != null
				&& !activeBeautifierStack.empty())
		{
			activeBeautifierStack.peek().inLineNumber = inLineNumber;
			activeBeautifierStack.peek().lineCommentNoBeautify = lineCommentNoBeautify;
			activeBeautifierStack.peek().isNonInStatementArray = isNonInStatementArray;
			activeBeautifierStack.peek().isSharpAccessor = isSharpAccessor;
			// must return originalLine not the trimmed line
			return activeBeautifierStack.peek().beautify(originalLine);
		}

		// calculate preliminary indentation based on data from past lines
		if (!inStatementIndentStack.empty())
		{
			spaceTabCount = inStatementIndentStack.peek();
		}

		for (int i = 0; i < headerStack.size(); i++)
		{
			isInClass = false;
			if (blockIndent)
			{
				// do NOT indent opening block for these headers
				if (!(headerStack.get(i).equals(ASResource.AS_NAMESPACE)
						|| headerStack.get(i).equals(ASResource.AS_CLASS)
						|| headerStack.get(i).equals(ASResource.AS_STRUCT)
						|| headerStack.get(i).equals(ASResource.AS_UNION)
						|| headerStack.get(i).equals(ASResource.AS_CONST)
						|| headerStack.get(i).equals(ASResource.AS_INTERFACE)
						|| headerStack.get(i).equals(ASResource.AS_THROWS) || headerStack
						.get(i).equals(ASResource.AS_STATIC)))
				{
					++tabCount;
				}
			} else if (!(i > 0
					&& !headerStack.get(i - 1).equals(
							ASResource.AS_OPEN_BRACKET) && headerStack.get(i)
					.equals(ASResource.AS_OPEN_BRACKET)))
			{
				++tabCount;
			}

			if (!isJavaStyle() && !namespaceIndent && i >= 1
					&& headerStack.get(i - 1).equals(ASResource.AS_NAMESPACE)
					&& headerStack.get(i).equals(ASResource.AS_OPEN_BRACKET))
			{
				--tabCount;
			}

			if (isCStyle() && i >= 1
					&& headerStack.get(i - 1).equals(ASResource.AS_CLASS)
					&& headerStack.get(i).equals(ASResource.AS_OPEN_BRACKET))
			{
				if (classIndent)
				{
					++tabCount;
				}
				isInClass = true;
			}

			// is the switchIndent option is on, indent switch statements an
			// additional indent.
			else if (switchIndent && i > 1
					&& headerStack.get(i - 1).equals(ASResource.AS_SWITCH)
					&& headerStack.get(i).equals(ASResource.AS_OPEN_BRACKET))
			{
				++tabCount;
				isInSwitch = true;
			}

		}

		if (!lineStartsInComment
				&& isCStyle()
				&& isInClass
				&& classIndent
				&& headerStack.size() >= 2
				&& headerStack.get(headerStack.size() - 2).equals(
						ASResource.AS_CLASS)
				&& headerStack.get(headerStack.size() - 1).equals(
						ASResource.AS_OPEN_BRACKET) && line.charAt(0) == '}')
		{
			--tabCount;
		}

		else if (!lineStartsInComment
				&& isInSwitch
				&& switchIndent
				&& headerStack.size() >= 2
				&& headerStack.get(headerStack.size() - 2).equals(
						ASResource.AS_SWITCH)
				&& headerStack.get(headerStack.size() - 1).equals(
						ASResource.AS_OPEN_BRACKET) && line.charAt(0) == '}')
		{
			--tabCount;
		}
		if (isInClassHeader)
		{
			isInClassHeaderTab = true;
			tabCount += 2;
		}

		if (isInConditional)
		{
			--tabCount;
		}

		// parse characters in the current line.

		for (int i = 0; i < line.length(); i++)
		{
			outBuffer.append(line.charAt(i));

			tempCh = line.charAt(i);
			prevCh = ch;
			ch = tempCh;

			if (isWhiteSpace(ch))
			{
				continue;
			}

			// handle special characters (i.e. backslash+character such as \n,
			// \t, ...)

			if (isInQuote && !isInVerbatimQuote)
			{
				if (isSpecialChar)
				{
					isSpecialChar = false;
					continue;
				}
				if (line.indexOf("\\\\", i) == i)
				{
					outBuffer.append('\\');
					i++;
					continue;
				}
				if (ch == '\\')
				{
					if (peekNextChar(line, i) == ' ') // is this '\'
						// at end of
						// line
					{
						haveLineContinuationChar = true;
					}
					else
					{
						isSpecialChar = true;
					}
					continue;
				}
			} else if (isInDefine && ch == '\\')
			{
				continue;
			}

			// handle quotes (such as 'x' and "Hello Dolly")
			if (!(isInComment || isInLineComment) && (ch == '"' || ch == '\''))
			{
				if (!isInQuote)
				{
					quoteChar = ch;
					isInQuote = true;
					if (isSharpStyle() && prevCh == '@')
					{
						isInVerbatimQuote = true;
					}
				} else if (isInVerbatimQuote && ch == '"')
				{
					if (peekNextChar(line, i) == '"') // check
					// consecutive
					// quotes
					{
						outBuffer.append('"');
						i++;
					} else
					{
						isInQuote = false;
						isInVerbatimQuote = false;
					}
				} else if (quoteChar == ch)
				{
					isInQuote = false;
					isInStatement = true;
					continue;
				}
			}
			if (isInQuote)
			{
				continue;
			}

			// handle comments

			if (!(isInComment || isInLineComment) && line.indexOf("//", i) == i)
			{
				isInLineComment = true;
				outBuffer.append('/');
				i++;
				continue;
			} else if (!(isInComment || isInLineComment)
					&& line.indexOf("/*", i) == i)
			{
				isInComment = true;
				outBuffer.append('*');
				i++;
				int j = ASUtils.findFirstNotOf(line, ASUtils.WHITE_SPACE, 0);
				if (!(line.indexOf("/*", j) == j)) // does line start with comment?
				{
					blockCommentNoIndent = true; // if no, cannot indent continuation lines
				}
				continue;
			} else if ((isInComment || isInLineComment)
					&& line.indexOf("*/", i) == i)
			{
				isInComment = false;
				outBuffer.append('/');
				i++;
				blockCommentNoIndent = false; // ok to indent next comment
				continue;
			}
			// treat C# '#region' and '#endregion' statements as a line comment
			else if (isSharpStyle()
					&& (line.indexOf("#region", i) == i || line.indexOf(
							"#endregion", i) == i))
			{
				isInLineComment = true;
				continue;
			}

			if (isInComment || isInLineComment)
			{
				continue;
			}

			// if we have reached this far then we are NOT in a comment or
			// String of special character...

			if (probationHeader != null)
			{
				if (((probationHeader.equals(ASResource.AS_STATIC)) || probationHeader
						.equals(ASResource.AS_CONST)
						&& ch == '{')
						|| (probationHeader.equals(ASResource.AS_SYNCHRONIZED) && ch == '('))
				{
					// insert the probation header as a new header
					isInHeader = true;
					headerStack.add(probationHeader);

					// handle the specific probation header
					isInConditional = probationHeader
							.equals(ASResource.AS_SYNCHRONIZED);

					isInStatement = false;
					// if the probation comes from the previous line, then
					// indent by 1 tab count.
					if (previousLineProbation
							&& ch == '{'
							&& !(blockIndent && (probationHeader
									.equals(ASResource.AS_CONST) || probationHeader
									.equals(ASResource.AS_STATIC))))
					{
						tabCount++;
						previousLineProbationTab = true;
					}
					previousLineProbation = false;
				}

				// dismiss the probation header
				probationHeader = null;
			}

			prevNonSpaceCh = currentNonSpaceCh;
			currentNonSpaceCh = ch;
			if (!isLegalNameChar(ch) && ch != ',' && ch != ';')
			{
				prevNonLegalCh = currentNonLegalCh;
				currentNonLegalCh = ch;
			}

			if (isInHeader)
			{
				isInHeader = false;
				currentHeader = headerStack.peek();
			} else
			{
				currentHeader = null;
			}

			if (isCStyle()
					&& isInTemplate
					&& (ch == '<' || ch == '>')
					&& findOperator(line.toString(), i, nonAssignmentOperators) == null)
			{
				if (ch == '<')
				{
					++templateDepth;
				} else if (ch == '>')
				{
					if (--templateDepth <= 0)
					{
						if (isInTemplate)
						{
							ch = ';';
						}
						else
						{
							ch = '\t';
						}
						isInTemplate = false;
						templateDepth = 0;
					}
				}
			}

			// handle parenthesies
			if (ch == '(' || ch == '[' || ch == ')' || ch == ']')
			{
				if (ch == '(' || ch == '[')
				{
					isInOperator = false;
					// if have a struct header, this is a declaration not a
					// definition
					if (ch == '(' && (isInClassHeader || isInClassHeaderTab)
							&& headerStack.size() > 0
							&& headerStack.peek().equals(ASResource.AS_STRUCT))
					{
						headerStack.pop();
						isInClassHeader = false;
						// -1 for isInClassHeader, -2 for isInClassHeaderTab
						if (isInClassHeaderTab)
						{
							tabCount -= 3;
							isInClassHeaderTab = false;
						}
						if (tabCount < 0)
						{
							tabCount = 0;
						}
					}

					if (parenDepth == 0)
					{
						parenStatementStack.add(isInStatement);
						isInStatement = true;
					}
					parenDepth++;

					inStatementIndentStackSizeStack.add(inStatementIndentStack
							.size());

					if (currentHeader != null)
					{
						registerInStatementIndent(line.toString(), i,
								spaceTabCount, minConditionalIndent/*
																	 * indentLength*
																	 * 2
																	 */, true);
					}
					else
					{
						registerInStatementIndent(line.toString(), i,
								spaceTabCount, 0, true);
					}
				} else if (ch == ')' || ch == ']')
				{
					parenDepth--;
					if (parenDepth == 0)
					{
						if (!parenStatementStack.empty()) // in case of
						// unmatched closing
						// parens
						{
							isInStatement = parenStatementStack.pop();
						}
						ch = ' ';
						isInConditional = false;
					}

					if (!inStatementIndentStackSizeStack.empty())
					{
						int previousIndentStackSize = inStatementIndentStackSizeStack
								.peek();
						inStatementIndentStackSizeStack.pop();
						while (previousIndentStackSize < inStatementIndentStack
								.size())
						{
							inStatementIndentStack.pop();
						}

						if (!parenIndentStack.empty())
						{
							int poppedIndent = parenIndentStack.pop();

							if (i == 0)
							{
								spaceTabCount = poppedIndent;
							}
						}
					}
				}

				continue;
			}

			if (ch == '{')
			{
				// first, check if '{' is a block-opener or an static-array
				// opener
				boolean isBlockOpener = ((prevNonSpaceCh == '{' && bracketBlockStateStack
						.peek())
						|| prevNonSpaceCh == '}'
						|| prevNonSpaceCh == ')'
						|| prevNonSpaceCh == ';'
						|| peekNextChar(line, i) == '{'
						|| isNonInStatementArray
						|| isSharpAccessor
						|| isInClassHeader || (isInDefine && (prevNonSpaceCh == '(' || isLegalNameChar(prevNonSpaceCh))));

				isInClassHeader = false;

				if (!isBlockOpener && currentHeader != null)
				{
					for (int n = 0; n < nonParenHeaders.size(); n++)
					{
						if (currentHeader.equals(nonParenHeaders.get(n)))
						{
							isBlockOpener = true;
							break;
						}
					}
				}

				// TODO: TEMPORARY??? fix to give C# }) statements a full indent
				// check for anonymous method
				if (isBlockOpener && isSharpStyle()
						&& !parenIndentStack.empty())
				{
					isBlockOpener = false;
				}

				bracketBlockStateStack.push(isBlockOpener);

				if (!isBlockOpener)
				{
					inStatementIndentStackSizeStack.push(inStatementIndentStack
							.size());
					registerInStatementIndent(line.toString(), i,
							spaceTabCount, 0, true);
					parenDepth++;
					if (i == 0)
						shouldIndentBrackettedLine = false;

					continue;
				}

				// this bracket is a block opener...

				++lineOpeningBlocksNum;

				if (isInClassHeaderTab)
				{
					isInClassHeaderTab = false;
					// decrease tab count if bracket is broken
					int firstChar = ASUtils.findFirstNotOf(line, ASUtils.WHITE_SPACE, 0);
					if (firstChar != -1)
					{
						if (line.charAt(firstChar) == '{'
								&& (int) firstChar == i)
						{
							tabCount -= 2;
						}
					}
				}

				if (bracketIndent && !namespaceIndent && headerStack.size() > 0
						&& headerStack.peek().equals(ASResource.AS_NAMESPACE))
				{
					shouldIndentBrackettedLine = false;
					tabCount--;
				}

				// do not allow inStatementIndent - should occur for Java files
				// only
				if (inStatementIndentStack.size() > 0)
				{
					spaceTabCount = 0;
					inStatementIndentStack.set(
							inStatementIndentStack.size() - 1, 0);
				}

				blockParenDepthStack.push(parenDepth);
				blockStatementStack.push(isInStatement);

				inStatementIndentStackSizeStack.push(inStatementIndentStack
						.size());
				if (inStatementIndentStack.size() > 0)
				{
					inStatementIndentStack.set(
							inStatementIndentStack.size() - 1, 0);
				}
				blockTabCount += isInStatement ? 1 : 0;
				parenDepth = 0;
				isInStatement = false;

				tempStacks.push(new Stack<String>());
				headerStack.push(ASResource.AS_OPEN_BRACKET);
				lastLineHeader = ASResource.AS_OPEN_BRACKET;

				continue;
			}

			// check if a header has been reached
			boolean isPotentialHeader = isCharPotentialHeader(line, i);

			if (isPotentialHeader)
			{
				String newHeader = findHeader(line, i, headers);

				if (newHeader != null)
				{
					char peekChar = peekNextChar(line, i + newHeader.length()
							- 1);

					// is not a header if part of a definition
					if (peekChar == ',' || peekChar == ')')
					{
						newHeader = null;
					}
					// the following accessor definitions are NOT headers
					// goto default; is NOT a header
					// default(int) keyword in C# is NOT a header
					else if ((newHeader.equals(ASResource.AS_GET)
							|| newHeader.equals(ASResource.AS_SET) || newHeader
							.equals(ASResource.AS_DEFAULT))
							&& (peekChar == ';' || peekChar == '('))
					{
						newHeader = null;
					}
				}

				if (newHeader != null)
				{
					// if we reached here, then this is a header...
					boolean isIndentableHeader = true;

					isInHeader = true;

					Stack<String> lastTempStack;
					if (tempStacks.empty())
					{
						lastTempStack = null;
					}
					else
					{
						lastTempStack = tempStacks.peek();
					}

					// if a new block is opened, push a new stack into
					// tempStacks to hold the
					// future list of headers in the new block.

					// take care of the special case: 'else if (...)'
					if (newHeader.equals(ASResource.AS_IF)
							&& lastLineHeader.equals(ASResource.AS_ELSE))
					{
						headerStack.pop();
					}

					// take care of 'else'
					else if (newHeader.equals(ASResource.AS_ELSE))
					{
						if (lastTempStack != null)
						{
							int indexOfIf = lastTempStack
									.indexOf(ASResource.AS_IF);
							if (indexOfIf != -1)
							{
								// recreate the header list in headerStack up to
								// the previous 'if'
								// from the temporary snapshot stored in
								// lastTempStack.
								int restackSize = lastTempStack.size()
										- indexOfIf - 1;
								for (int r = 0; r < restackSize; r++)
								{
									headerStack.add(lastTempStack.pop());
								}
								if (!closingBracketReached)
									tabCount += restackSize;
							}
							/*
							 * If the above if is not true, i.e. no 'if' before
							 * the 'else', then nothing beautiful will come out
							 * of this... I should think about inserting an
							 * Exception here to notify the caller of this...
							 */
						}
					}

					// check if 'while' closes a previous 'do'
					else if (newHeader.equals(ASResource.AS_WHILE))
					{
						if (lastTempStack != null)
						{
							int indexOfDo = lastTempStack
									.indexOf(ASResource.AS_DO);
							if (indexOfDo != -1)
							{
								// recreate the header list in headerStack up to
								// the previous 'do'
								// from the temporary snapshot stored in
								// lastTempStack.
								int restackSize = lastTempStack.size()
										- indexOfDo - 1;
								for (int r = 0; r < restackSize; r++)
								{
									headerStack.add(lastTempStack.peek());
									lastTempStack.pop();
								}
								if (!closingBracketReached)
									tabCount += restackSize;
							}
						}
					}
					// check if 'catch' closes a previous 'try' or 'catch'
					else if (newHeader.equals(ASResource.AS_CATCH)
							|| newHeader.equals(ASResource.AS_FINALLY))
					{
						if (lastTempStack != null)
						{
							int indexOfTry = lastTempStack
									.indexOf(ASResource.AS_TRY);
							if (indexOfTry == -1)
								indexOfTry = lastTempStack
										.indexOf(ASResource.AS_CATCH);
							if (indexOfTry != -1)
							{
								// recreate the header list in headerStack up to
								// the previous 'try'
								// from the temporary snapshot stored in
								// lastTempStack.
								int restackSize = lastTempStack.size()
										- indexOfTry - 1;
								for (int r = 0; r < restackSize; r++)
								{
									headerStack.add(lastTempStack.pop());
								}

								if (!closingBracketReached)
									tabCount += restackSize;
							}
						}
					} else if (newHeader.equals(ASResource.AS_CASE))
					{
						isInCase = true;
						if (!haveCaseIndent)
						{
							haveCaseIndent = true;
							--tabCount;
						}
					} else if (newHeader.equals(ASResource.AS_DEFAULT))
					{
						isInCase = true;
						--tabCount;
					} else if (newHeader.equals(ASResource.AS_STATIC)
							|| newHeader.equals(ASResource.AS_SYNCHRONIZED)
							|| (newHeader.equals(ASResource.AS_CONST) && isCStyle()))
					{
						if (!headerStack.empty()
								&& (headerStack.peek().equals(
										ASResource.AS_STATIC)
										|| headerStack.peek().equals(
												ASResource.AS_SYNCHRONIZED) || headerStack
										.peek().equals(ASResource.AS_CONST)))
						{
							isIndentableHeader = false;
						} else
						{
							isIndentableHeader = false;
							probationHeader = newHeader;
						}
					} else if (newHeader.equals(ASResource.AS_CONST))
					{
						isIndentableHeader = false;
					} else if (newHeader.equals(ASResource.AS_TEMPLATE))
					{
						if (isCStyle())
							isInTemplate = true;
						isIndentableHeader = false;
					}

					if (isIndentableHeader)
					{
						headerStack.add(newHeader);
						isInStatement = false;
						if (nonParenHeaders.indexOf(newHeader) == -1)
						{
							isInConditional = true;
						}
						lastLineHeader = newHeader;
					} else
					{
						isInHeader = false;
					}

					outBuffer.append(newHeader.substring(1));
					i += newHeader.length() - 1;

					continue;
				} // newHeader != null
			} // isPotentialHeader

			if (ch == '?')
			{
				isInQuestion = true;
			}
			else
			// special handling of 'case' statements
			if (ch == ':')
			{
				if ((line.length() > i + 1) && line.charAt(i + 1) == ':') // look
				// for
				// ::
				{
					++i;
					outBuffer.append(':');
					ch = ' ';
					continue;
				}

				else if (isInQuestion)
				{
					isInQuestion = false;
				}
//
//				else if (isCStyle() && isInClassHeader)
//				{
//					// found a 'class A : public B' definition
//					// so do nothing special
//				}
//
//				else if (isCStyle() && Character.isDigit(peekNextChar(line, i)))
//				{
//					// found a bit field
//					// so do nothing special
//				}

				else if (isCStyle() && isInClass && prevNonSpaceCh != ')')
				{
					--tabCount;
					// found a 'private:' or 'public:' inside a class definition
					// so do nothing special
				}

//				else if (isJavaStyle()
//						&& lastLineHeader.equals(ASResource.AS_FOR))
//				{
//					// found a java for-each statement
//					// so do nothing special
//				}

				else if (isCStyle() && prevNonSpaceCh == ')' && !isInCase)
				{
					isInClassHeader = true;
					if (i == 0)
						tabCount += 2;
				} else
				{
					currentNonSpaceCh = ';'; // so that brackets after the ':'
					// will appear as block-openers
					if (isInCase)
					{
						isInCase = false;
						ch = ';'; // from here on, treat char as ';'
					} else if (isCStyle()
							|| (isSharpStyle() && peekNextChar(line, i) == ';')) // is
					// in
					// a
					// label
					// (e.g.
					// 'label1:')
					{
						if (labelIndent)
							--tabCount; // unindent label by one indent
						else
							tabCount = 0; // completely flush indent to left
					}
				}
			}

			if ((ch == ';' || (parenDepth > 0 && ch == ','))
					&& !inStatementIndentStackSizeStack.empty())
				while (inStatementIndentStackSizeStack.peek()
						+ (parenDepth > 0 ? 1 : 0) < inStatementIndentStack
						.size())
				{
					inStatementIndentStack.pop();
				}

			// handle ends of statements
			if ((ch == ';' && parenDepth == 0) || ch == '}'/*
															 * || (ch == ',' &&
															 * parenDepth == 0)
															 */)
			{
				if (ch == '}')
				{
					// first check if this '}' closes a previous block, or a
					// static array...
					if (!bracketBlockStateStack.empty())
					{
						boolean bracketBlockState = bracketBlockStateStack
								.pop();
						if (!bracketBlockState)
						{
							if (!inStatementIndentStackSizeStack.empty())
							{
								// this bracket is a static array

								int previousIndentStackSize = inStatementIndentStackSizeStack
										.pop();
								while (previousIndentStackSize < inStatementIndentStack
										.size())
								{
									inStatementIndentStack.pop();
								}
								parenDepth--;
								if (i == 0)
								{
									shouldIndentBrackettedLine = false;
								}

								if (!parenIndentStack.empty())
								{
									int poppedIndent = parenIndentStack.pop();
									if (i == 0)
										spaceTabCount = poppedIndent;
								}
							}
							continue;
						}
					}

					// this bracket is block closer...

					++lineClosingBlocksNum;

					if (!inStatementIndentStackSizeStack.empty())
						inStatementIndentStackSizeStack.pop();

					if (!blockParenDepthStack.empty())
					{
						parenDepth = blockParenDepthStack.pop();

						isInStatement = blockStatementStack.pop();

						if (isInStatement)
							blockTabCount--;
					}

					closingBracketReached = true;
					int headerPlace = headerStack
							.indexOf(ASResource.AS_OPEN_BRACKET);
					if (headerPlace != -1)
					{
						String popped = headerStack.peek();
						while (!popped.equals(ASResource.AS_OPEN_BRACKET))
						{
							headerStack.pop();
							popped = headerStack.peek();
						}
						headerStack.pop();

						// do not indent namespace bracket unless namespaces are
						// indented
						if (!namespaceIndent
								&& headerStack.size() > 0
								&& headerStack.peek().equals(
										ASResource.AS_NAMESPACE))
							shouldIndentBrackettedLine = false;

						if (!tempStacks.empty())
						{
							tempStacks.pop();
						}
					}

					ch = ' '; // needed due to cases such as '}else{', so that
					// headers ('else' tn tih case) will be
					// identified...
				}

				/*
				 * Create a temporary snapshot of the current block's
				 * header-list in the uppermost inner stack in tempStacks, and
				 * clear the headerStack up to the begining of the block. Thus,
				 * the next future statement will think it comes one indent past
				 * the block's '{' unless it specifically checks for a
				 * companion-header (such as a previous 'if' for an 'else'
				 * header) within the tempStacks, and recreates the temporary
				 * snapshot by manipulating the tempStacks.
				 */
				if (!tempStacks.peek().empty())
				{
					while (!tempStacks.peek().empty())
					{
						tempStacks.peek().pop();
					}
				}
				while (!headerStack.empty()
						&& !headerStack.peek().equals(
								ASResource.AS_OPEN_BRACKET))
				{
					tempStacks.peek().add(headerStack.pop());
				}

				if (parenDepth == 0 && ch == ';')
				{
					isInStatement = false;
				}

				previousLastLineHeader = null;
				isInClassHeader = false;
				isInQuestion = false;

				continue;
			}

			if (isPotentialHeader)
			{
				// check for preBlockStatements in C/C++ ONLY if not within
				// parenthesies
				// (otherwise 'struct XXX' statements would be wrongly
				// interpreted...)
				if (!isInTemplate && !(isCStyle() && parenDepth > 0))
				{
					String newHeader = findHeader(line, i, preBlockStatements);
					if (newHeader != null)
					{
						isInClassHeader = true;

						if (!isSharpStyle())
							headerStack.add(newHeader);
						// do not need 'where' in the headerStack
						// do not need second 'class' statement in a row
						else if (!(newHeader.equals(ASResource.AS_WHERE) || (newHeader
								.equals(ASResource.AS_CLASS)
								&& headerStack.size() > 0 && headerStack.peek()
								.equals(ASResource.AS_CLASS))))
							headerStack.add(newHeader);

						outBuffer.append(newHeader.substring(1));
						i += newHeader.length() - 1;
						continue;
					}
				}
				String foundIndentableHeader = findHeader(line, i,
						indentableHeaders);

				if (foundIndentableHeader != null)
				{
					// must bypass the header before registering the in
					// statement
					outBuffer.append(foundIndentableHeader.substring(1));
					i += foundIndentableHeader.length() - 1;
					if (!isInOperator && !isInTemplate
							&& !isNonInStatementArray)
					{
						registerInStatementIndent(line.toString(), i,
								spaceTabCount, 0, false);
						isInStatement = true;
					}
					continue;
				}

				if (isCStyle() && findKeyword(line, i, ASResource.AS_OPERATOR))
					isInOperator = true;

				// "new" operator is a pointer, not a calculation
				if (findKeyword(line, i, ASResource.AS_NEW))
				{
					if (prevNonSpaceCh == '=' && isInStatement
							&& !inStatementIndentStack.empty())
					{
						inStatementIndentStack.set(inStatementIndentStack
								.size() - 1, 0);
					}
				}

				// append the entire name for all others
				String name = getCurrentWord(line, i);
				outBuffer.append(name.substring(1));
				i += name.length() - 1;
				continue;
			}

			// Handle operators

			boolean isPotentialOperator = isCharPotentialOperator(ch);

			if (isPotentialOperator)
			{
				// Check if an operator has been reached.
				String foundAssignmentOp = findOperator(line.toString(), i,
						assignmentOperators);
				String foundNonAssignmentOp = findOperator(line.toString(), i,
						nonAssignmentOperators);

				// Since findHeader's boundry checking was not used above, it is
				// possible
				// that both an assignment op and a non-assignment op where
				// found,
				// e.g. '>>' and '>>='. If this is the case, treat the LONGER
				// one as the
				// found operator.
				if (foundAssignmentOp != null && foundNonAssignmentOp != null)
				{
					if (foundAssignmentOp.length() < foundNonAssignmentOp
							.length())
						foundAssignmentOp = null;
					else
						foundNonAssignmentOp = null;
				}

				if (foundNonAssignmentOp != null)
				{
					if (foundNonAssignmentOp.length() > 1)
					{
						outBuffer.append(foundNonAssignmentOp.substring(1));
						i += foundNonAssignmentOp.length() - 1;
					}
				}

				else if (foundAssignmentOp != null)
				{
					if (foundAssignmentOp.length() > 1)
					{
						outBuffer.append(foundAssignmentOp.substring(1));
						i += foundAssignmentOp.length() - 1;
					}

					if (!isInOperator && !isInTemplate
							&& !isNonInStatementArray)
					{
						registerInStatementIndent(line.toString(), i,
								spaceTabCount, 0, false);
						isInStatement = true;
					}
				}
			}
		} // end of for loop * end of for loop * end of for loop * end of for
		// loop

		// handle special cases of unindentation:

		/*
		 * if '{' doesn't follow an immediately previous '{' in the headerStack
		 * (but rather another header such as "for" or "if", then unindent it by
		 * one indentation relative to its block.
		 */

		if (!lineStartsInComment
				&& !blockIndent
				&& outBuffer.length() > 0
				&& outBuffer.charAt(0) == '{'
				&& !(lineOpeningBlocksNum > 0 && lineOpeningBlocksNum == lineClosingBlocksNum)
				&& !(headerStack.size() > 1 && headerStack.get(
						headerStack.size() - 2).equals(
						ASResource.AS_OPEN_BRACKET))
				&& shouldIndentBrackettedLine)
			--tabCount;

		else if (!lineStartsInComment && outBuffer.length() > 0
				&& outBuffer.charAt(0) == '}' && shouldIndentBrackettedLine)
			--tabCount;

		// correctly indent one-line-blocks...
		else if (!lineStartsInComment && outBuffer.length() > 0
				&& lineOpeningBlocksNum > 0
				&& lineOpeningBlocksNum == lineClosingBlocksNum
				&& previousLineProbationTab)
			--tabCount; // lineOpeningBlocksNum - (blockIndent ? 1 : 0);

		// correctly indent class continuation lines...
		else if (!lineStartsInComment
				&& isInClassHeaderTab
				&& !blockIndent
				&& outBuffer.length() > 0
				&& lineOpeningBlocksNum == 0
				&& lineOpeningBlocksNum == lineClosingBlocksNum
				&& (headerStack.size() > 0 && headerStack.peek().equals(
						ASResource.AS_CLASS)))
			--tabCount;

		if (tabCount < 0)
			tabCount = 0;

		// take care of extra bracket indentatation option...
		if (!lineStartsInComment && bracketIndent && shouldIndentBrackettedLine
				&& outBuffer.length() > 0
				&& (outBuffer.charAt(0) == '{' || outBuffer.charAt(0) == '}'))
			tabCount++;

		if (isInDefine)
		{
			if (outBuffer.charAt(0) == '#')
			{
				String preproc = outBuffer.toString().trim();
				if (preproc.startsWith("define"))
				{
					if (!inStatementIndentStack.empty()
							&& inStatementIndentStack.peek() > 0)
					{
						defineTabCount = tabCount;
					} else
					{
						defineTabCount = tabCount - 1;
						tabCount--;
					}
				}
			}

			tabCount -= defineTabCount;
		}

		if (tabCount < 0)
			tabCount = 0;
		if (lineCommentNoBeautify || blockCommentNoBeautify
				|| isInQuoteContinuation)
			tabCount = spaceTabCount = 0;

		// finally, insert indentations into begining of line

		prevFinalLineSpaceTabCount = spaceTabCount;
		prevFinalLineTabCount = tabCount;

		if (shouldForceTabIndentation)
		{
			tabCount += spaceTabCount / indentLength;
			spaceTabCount = spaceTabCount % indentLength;
		}
		outBuffer = new StringBuffer(preLineWS(spaceTabCount, tabCount).append(
				outBuffer));

		if (lastLineHeader != null)
			previousLastLineHeader = lastLineHeader;

		return outBuffer;
	}

	private StringBuffer preLineWS(final int spaceTabCount, int tabCount)
	{
		int stc = spaceTabCount;
		StringBuffer ws = new StringBuffer();

		for (int i = 0; i < tabCount; i++)
		{
			ws.append(indentString);
		}

		while ((stc--) > 0)
		{
			ws.append(" ");
		}
		return ws;

	}

	/**
	 * register an in-statement indent.
	 *
	 * @param line
	 * @param i
	 * @param spaceTabCount
	 * @param minIndent
	 * @param updateParenStack
	 */
	private void registerInStatementIndent(String line, int i, int spaceTabCount,
			int minIndent, boolean updateParenStack)
	{
		int inStatementIndent;
		int remainingCharNum = line.length() - i;
		int nextNonWSChar = getNextProgramCharDistance(line, i);

		// if indent is around the last char in the line, indent instead 2
		// spaces from the previous indent
		if (nextNonWSChar == remainingCharNum)
		{
			int previousIndent = spaceTabCount;
			if (!inStatementIndentStack.empty())
			{
				previousIndent = inStatementIndentStack.peek();
			}

			inStatementIndentStack.add(/* 2 */indentLength + previousIndent);
			if (updateParenStack)
			{
				parenIndentStack.add(previousIndent);
			}
			return;
		}

		if (updateParenStack)
			parenIndentStack.add(i + spaceTabCount);

		inStatementIndent = i + nextNonWSChar + spaceTabCount;

		if (i + nextNonWSChar < minIndent)
			inStatementIndent = minIndent + spaceTabCount;

		if (i + nextNonWSChar > maxInStatementIndent)
			inStatementIndent = indentLength * 2 + spaceTabCount;

		if (!inStatementIndentStack.empty()
				&& inStatementIndent < inStatementIndentStack.peek())
			inStatementIndent = inStatementIndentStack.peek();

		if (isNonInStatementArray)
			inStatementIndent = 0;

		inStatementIndentStack.add(inStatementIndent);
	}

	/**
	 * get distance to the next non-white space, non-comment character in the
	 * line. if no such character exists, return the length remaining to the end
	 * of the line.
	 *
	 * @param line
	 * @param i
	 * @return
	 */
	private int getNextProgramCharDistance(String line, int i)
	{
		boolean inComment = false;
		int remainingCharNum = line.length() - i;
		int charDistance;
		char ch;

		for (charDistance = 1; charDistance < remainingCharNum; charDistance++)
		{
			ch = line.charAt(i + charDistance);
			if (inComment)
			{
				if (line.indexOf("*/", i + charDistance) == (i + charDistance))
				{
					charDistance++;
					inComment = false;
				}
			} else if (isWhiteSpace(ch))
			{
				continue;
			}
			else if (ch == '/')
			{
				if (line.indexOf("//", i + charDistance) == (i + charDistance))
				{
					return remainingCharNum;
				}
				else if (line.indexOf("/*", i + charDistance) == (i + charDistance))
				{
					charDistance++;
					inComment = true;
				}
			} else
			{
				return charDistance;
			}
		}

		return charDistance;
	}

	// check if a specific line position contains a header.
	protected String findHeader(StringBuffer line, int i, List<String> possibleHeaders)
	{
		assert (isCharPotentialHeader(line, i)) : line
				+ " is not a potential header";
		// check the word
		int maxHeaders = possibleHeaders.size();
		for (int p = 0; p < maxHeaders; p++)
		{
			String header = possibleHeaders.get(p);
			int end = i + header.length();
			if (end > line.length())
				continue;
			int result = line.substring(i, end).compareTo(header);

			if (result > 0)
			{
				continue;
			}
			if (result < 0)
				break;
			// check that this is not part of a longer word
			int wordEnd = i + header.length();
			if (wordEnd == line.length())
				return header;
			if (isLegalNameChar(line.charAt(wordEnd)))
				continue;
			// is not a header if part of a definition
			char peekChar = peekNextChar(line, wordEnd - 1);
			if (peekChar == ',' || peekChar == ')')
				break;
			return header;
		}
		return null;
	}

	// check if a specific line position contains an operator.
	protected String findOperator(String line, int i, List<String> possibleOperators)
	{
		assert (isCharPotentialOperator(line.charAt(i))) : line.charAt(i)
				+ " is not a potential header";
		// find the operator in the vector
		// the vector contains the LONGEST operators first
		// must loop thru the entire vector
		int maxOperators = possibleOperators.size();
		for (int p = 0; p < maxOperators; p++)
		{
			if (line.indexOf(possibleOperators.get(p), i) == i)
			{
				return possibleOperators.get(p);
			}
		}
		return null;
	}

	/**
	 * peek at the next unread character.
	 *
	 * @return the next unread character.
	 * @param line
	 *            the line to check.
	 * @param i
	 *            the current char position on the line.
	 */
	public char peekNextChar(StringBuffer line, int i)
	{
		char ch = ' ';
		int peekNum = ASUtils.findFirstNotOf(line, ASUtils.WHITE_SPACE, i + 1);

		if (peekNum == -1)
			return ch;

		ch = line.charAt(peekNum);

		return ch;
	}
}
