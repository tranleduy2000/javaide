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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.barenca.jastyle.constants.BracketType;

public class ASFormatter extends ASBeautifier
{
	public static enum FormatStyle {
		STYLE_NONE, STYLE_ALLMAN, STYLE_JAVA, STYLE_KandR, STYLE_STROUSTRUP, STYLE_WHITESMITH, STYLE_BANNER, STYLE_GNU, STYLE_LINUX
	}

	public enum BracketMode {
		NONE_MODE, ATTACH_MODE, BREAK_MODE, LINUX_MODE, STROUSTRUP_MODE, BDAC_MODE
	}

	private int formatterFileType = 9; // initialized with an invalid type

	private List<String> headers;
	private List<String> nonParenHeaders;
	private List<String> preDefinitionHeaders;
	private List<String> preCommandHeaders;
	private List<String> operators;
	private List<String> assignmentOperators;
	private List<String> castOperators;
	private ASSourceIterator sourceIterator;
	private ASEnhancer enhancer;

	private Stack<String> preBracketHeaderStack;
	private Stack<Integer> bracketTypeStack;
	private Stack<Integer> parenStack;
	private StringBuffer readyFormattedLine;
	private StringBuffer currentLine;
	private StringBuffer formattedLine;
	private String currentHeader;
	private char currentChar;
	private char previousChar;
	private char previousNonWSChar;
	private char previousCommandChar;
	private char quoteChar;
	private int charNum;
	private int preprocBracketTypeStackSize;
	private int tabIncrementIn;
	private int spacePadNum;
	private int templateDepth;
	private int traceLineNumber;
	private long formattedLineCommentNum; // comment location on formattedLine
	private long previousReadyFormattedLineLength;
	private FormatStyle formattingStyle;
	private BracketMode bracketFormatMode;
	private int previousBracketType;
	private boolean isVirgin;
	private boolean shouldPadOperators;
	private boolean shouldPadParensOutside;
	private boolean shouldPadParensInside;
	private boolean shouldUnPadParens;
	private boolean shouldConvertTabs;
	private boolean isInLineComment;
	private boolean isInComment;
	private boolean isInPreprocessor;
	private boolean isInTemplate; // true both in template definitions (e.g.
	// template<class A>) and template usage (e.g. F<int>).
	private boolean doesLineStartComment;
	private boolean lineEndsInCommentOnly;
	private boolean lineIsLineCommentOnly;
	private boolean lineIsEmpty;
	private boolean isImmediatelyPostCommentOnly;
	private boolean isImmediatelyPostEmptyLine;
	private boolean isInQuote;
	private boolean isInVerbatimQuote;
	private boolean haveLineContinuationChar;
	private boolean isInQuoteContinuation;
	private boolean isInBlParen;
	private boolean isSpecialChar;
	private boolean isNonParenHeader;
	private boolean foundQuestionMark;
	private boolean foundPreDefinitionHeader;
	private boolean foundNamespaceHeader;
	private boolean foundClassHeader;
	private boolean foundInterfaceHeader;
	private boolean foundPreCommandHeader;
	private boolean foundCastOperator;
	private boolean isInLineBreak;
	private boolean endOfCodeReached;
	private boolean lineCommentNoIndent;
	private boolean isLineReady;
	private boolean isPreviousBracketBlockRelated;
	private boolean isInPotentialCalculation;
	private boolean isCharImmediatelyPostComment;
	private boolean isPreviousCharPostComment;
	private boolean isCharImmediatelyPostLineComment;
	private boolean isCharImmediatelyPostOpenBlock;
	private boolean isCharImmediatelyPostCloseBlock;
	private boolean isCharImmediatelyPostTemplate;
	private boolean isCharImmediatelyPostReturn;
	private boolean isCharImmediatelyPostOperator;
	private boolean shouldBreakOneLineBlocks;
	private boolean shouldReparseCurrentChar;
	private boolean shouldBreakOneLineStatements;
	private boolean shouldBreakClosingHeaderBrackets;
	private boolean shouldBreakElseIfs;
	private boolean shouldDeleteEmptyLines;
	private boolean needHeaderOpeningBracket;
	private boolean passedSemicolon;
	private boolean passedColon;
	private boolean isImmediatelyPostComment;
	private boolean isImmediatelyPostLineComment;
	private boolean isImmediatelyPostEmptyBlock;
	private boolean isImmediatelyPostPreprocessor;
	private boolean isImmediatelyPostReturn;
	private boolean isImmediatelyPostOperator;

	private boolean shouldBreakBlocks;
	private boolean shouldBreakClosingHeaderBlocks;
	private boolean isPrependPostBlockEmptyLineRequested;
	private boolean isAppendPostBlockEmptyLineRequested;

	private boolean prependEmptyLine;
	private boolean appendOpeningBracket;
	private boolean foundClosingHeader;

	private boolean isInHeader;
	private boolean isImmediatelyPostHeader;
	private boolean isInCase;
	private boolean isJavaStaticConstructor;

	/**
	 * Constructor of ASFormatter
	 */
	public ASFormatter()
	{
		sourceIterator = null;
		enhancer = new ASEnhancer();
		preBracketHeaderStack = null;
		bracketTypeStack = null;
		parenStack = null;
		lineCommentNoIndent = false;
		formattingStyle = FormatStyle.STYLE_NONE;
		bracketFormatMode = BracketMode.NONE_MODE;
		shouldPadOperators = true;
		shouldPadParensOutside = false;
		shouldPadParensInside = false;
		shouldUnPadParens = false;
		shouldBreakOneLineBlocks = true;
		shouldBreakOneLineStatements = true;
		shouldConvertTabs = false;
		shouldBreakBlocks = false;
		shouldBreakClosingHeaderBlocks = false;
		shouldBreakClosingHeaderBrackets = false;
		shouldDeleteEmptyLines = false;
		shouldBreakElseIfs = false;
	}

	/**
	 * build vectors for each programing language depending on the file
	 * extension.
	 */
	private void buildLanguageVectors()
	{
		if (getFileType() == formatterFileType) // don't build unless necessary
		{
			return;
		}

		formatterFileType = getFileType();

		headers=new ArrayList<String>();
		nonParenHeaders=new ArrayList<String>();
		assignmentOperators=new ArrayList<String>();
		operators=new ArrayList<String>();
		preDefinitionHeaders=new ArrayList<String>();
		preCommandHeaders=new ArrayList<String>();
		castOperators=new ArrayList<String>();

		ASResource.buildHeaders(headers, getFileType(), false);
		ASResource.buildNonParenHeaders(nonParenHeaders, getFileType(), false);
		ASResource.buildPreDefinitionHeaders(preDefinitionHeaders,
				getFileType());
		ASResource.buildPreCommandHeaders(preCommandHeaders, getFileType());
		if (operators.size() == 0)
			ASResource.buildOperators(operators);
		if (assignmentOperators.size() == 0)
			ASResource.buildAssignmentOperators(assignmentOperators);
		if (castOperators.size() == 0)
			ASResource.buildCastOperators(castOperators);
	}

	/**
	 * set the variables for each preefined style. this will override any
	 * previous settings.
	 */
	void fixOptionVariableConflicts()
	{
		switch (formattingStyle)
		{
		case STYLE_NONE:
			// do nothing, accept the current settings
			break;

		case STYLE_ALLMAN:
			setBracketFormatMode(BracketMode.BREAK_MODE);
			setBlockIndent(false);
			setBracketIndent(false);
			break;

		case STYLE_JAVA:
			setBracketFormatMode(BracketMode.ATTACH_MODE);
			setBlockIndent(false);
			setBracketIndent(false);
			break;

		case STYLE_KandR:
			setBracketFormatMode(BracketMode.LINUX_MODE);
			setBlockIndent(false);
			setBracketIndent(false);
			break;

		case STYLE_STROUSTRUP:
			setBracketFormatMode(BracketMode.STROUSTRUP_MODE);
			setBlockIndent(false);
			setBracketIndent(false);
			break;

		case STYLE_WHITESMITH:
			setBracketFormatMode(BracketMode.BREAK_MODE);
			setBlockIndent(false);
			setBracketIndent(true);
			setClassIndent(true);
			setSwitchIndent(true);
			break;

		case STYLE_BANNER:
			setBracketFormatMode(BracketMode.ATTACH_MODE);
			setBlockIndent(false);
			setBracketIndent(true);
			setClassIndent(true);
			setSwitchIndent(true);
			break;

		case STYLE_GNU:
			setBracketFormatMode(BracketMode.BREAK_MODE);
			setBlockIndent(true);
			setBracketIndent(false);
			setSpaceIndentation(2);
			break;

		case STYLE_LINUX:
			setBracketFormatMode(BracketMode.LINUX_MODE);
			setBlockIndent(false);
			setBracketIndent(false);
			setSpaceIndentation(8);
			break;
		}
		// cannot have both bracketIndent and block Indent
		// default to bracketIndent
		if (isBracketIndent() && isBlockIndent())
		{
			setBracketIndent(false);
		}
	}

	/**
	 * initialize the ASFormatter.
	 *
	 * init() should be called every time a ASFormatter object is to start
	 * formatting a NEW source file. init() recieves a pointer to a DYNAMICALLY
	 * CREATED ASSourceIterator object that will be used to iterate through the
	 * source code. This object will be deleted during the ASFormatter's
	 * destruction, and thus should not be deleted elsewhere.
	 *
	 * @param iter
	 *            a pointer to the DYNAMICALLY CREATED ASSourceIterator object.
	 */
	public void init(ASSourceIterator si)
	{
		buildLanguageVectors();
		fixOptionVariableConflicts();

		super.init(si);
		enhancer.init(getFileType(), getIndentLength(), getIndentString(),
				isCaseIndent(), isEmptyLineFill());
		sourceIterator = si;

		preBracketHeaderStack = new Stack<String>();
		parenStack = new Stack<Integer>();
		parenStack.push(0); // parenStack must contain this default entry
		bracketTypeStack = new Stack<Integer>();
		bracketTypeStack.push(BracketType.NULL_TYPE);

		currentHeader = null;
		currentLine = new StringBuffer();
		readyFormattedLine = new StringBuffer();
		formattedLine = new StringBuffer();
		currentChar = ' ';
		previousChar = ' ';
		previousCommandChar = ' ';
		previousNonWSChar = ' ';
		quoteChar = '"';
		charNum = 0;
		preprocBracketTypeStackSize = 0;
		spacePadNum = 0;
		previousReadyFormattedLineLength = -1;
		templateDepth = 0;
		traceLineNumber = 0;
		previousBracketType = BracketType.NULL_TYPE;

		isVirgin = true;
		isInLineComment = false;
		isInComment = false;
		isInPreprocessor = false;
		doesLineStartComment = false;
		lineEndsInCommentOnly = false;
		lineIsLineCommentOnly = false;
		lineIsEmpty = false;
		isImmediatelyPostCommentOnly = false;
		isImmediatelyPostEmptyLine = false;
		isInQuote = false;
		isInVerbatimQuote = false;
		haveLineContinuationChar = false;
		isInQuoteContinuation = false;
		isSpecialChar = false;
		isNonParenHeader = true;
		foundNamespaceHeader = false;
		foundClassHeader = false;
		foundInterfaceHeader = false;
		foundPreDefinitionHeader = false;
		foundPreCommandHeader = false;
		foundCastOperator = false;
		foundQuestionMark = false;
		isInLineBreak = false;
		endOfCodeReached = false;
		isLineReady = false;
		isPreviousBracketBlockRelated = true;
		isInPotentialCalculation = false;
		shouldReparseCurrentChar = false;
		needHeaderOpeningBracket = false;
		passedSemicolon = false;
		passedColon = false;
		isInTemplate = false;
		isInBlParen = false;
		isImmediatelyPostComment = false;
		isImmediatelyPostLineComment = false;
		isImmediatelyPostEmptyBlock = false;
		isImmediatelyPostPreprocessor = false;
		isImmediatelyPostReturn = false;
		isImmediatelyPostOperator = false;
		isCharImmediatelyPostReturn = false;
		isCharImmediatelyPostOperator = false;

		isPrependPostBlockEmptyLineRequested = false;
		isAppendPostBlockEmptyLineRequested = false;
		prependEmptyLine = false;
		appendOpeningBracket = false;

		foundClosingHeader = false;
		previousReadyFormattedLineLength = 0;

		isImmediatelyPostHeader = false;
		isInHeader = false;
		isInCase = false;
		isJavaStaticConstructor = false;
	}

	/**
	 * get the next formatted line.
	 *
	 * @return formatted line.
	 */

	public StringBuffer nextLine()
	{
		String newHeader;
		boolean isInVirginLine = isVirgin;
		isCharImmediatelyPostComment = false;
		isPreviousCharPostComment = false;
		isCharImmediatelyPostLineComment = false;
		isCharImmediatelyPostOpenBlock = false;
		isCharImmediatelyPostCloseBlock = false;
		isCharImmediatelyPostTemplate = false;
		traceLineNumber++;

		while (!isLineReady)
		{
			if (shouldReparseCurrentChar)
				shouldReparseCurrentChar = false;
			else if (!getNextChar())
			{
				breakLine();  // readyFormattedLine is assigned here
				return beautify(readyFormattedLine);
			} else
			// stuff to do when reading a new character...
			{
				// make sure that a virgin '{' at the begining ofthe file will
				// be treated as a block...
				if (isInVirginLine && currentChar == '{' && lineBeginsWith('{'))
					previousCommandChar = '{';
				isPreviousCharPostComment = isCharImmediatelyPostComment;
				isCharImmediatelyPostComment = false;
				isCharImmediatelyPostTemplate = false;
				isCharImmediatelyPostReturn = false;
				isCharImmediatelyPostOperator = false;
				isCharImmediatelyPostOpenBlock = false;
				isCharImmediatelyPostCloseBlock = false;
			}

			// if (inLineNumber >= 9)
			// int x = 1;

			if (isInLineComment)
			{
				appendCurrentChar();

				// explicitely break a line when a line comment's end is found.
				if ((charNum + 1) == currentLine.length())
				{
					isInLineBreak = true;
					isInLineComment = false;
					isImmediatelyPostLineComment = true;
					currentChar = 0; // make sure it is a neutral char.
				}
				continue;
			} else if (isInComment)
			{
				if (isSequenceReached("*/"))
				{
					isInComment = false;
					isImmediatelyPostComment = true;
					appendSequence(ASResource.AS_CLOSE_COMMENT, true);
					goForward(1);
					if (doesLineStartComment
							&& (ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE,
									charNum + 1) == -1))
						lineEndsInCommentOnly = true;
				} else
					appendCurrentChar();

				continue;
			}

			// not in line comment or comment

			else if (isInQuote)
			{
				if (isSpecialChar)
				{
					isSpecialChar = false;
				} else if (currentChar == '\\' && !isInVerbatimQuote)
				{
					if (peekNextChar() == ' ') // is this '\' at end of line
						haveLineContinuationChar = true;
					else
						isSpecialChar = true;
				} else if (isInVerbatimQuote && currentChar == '"')
				{
					if (peekNextChar() == '"') // check consecutive quotes
					{
						appendSequence("\"\"");
						goForward(1);
						continue;
					} else
					{
						isInQuote = false;
						isInVerbatimQuote = false;
					}
				} else if (quoteChar == currentChar)
				{
					isInQuote = false;
				}

				appendCurrentChar();
				continue;
			}

			if (isSequenceReached("//"))
			{
				if ( (charNum+2)<currentLine.length() && currentLine.charAt(charNum + 2) == 0xf2) // check for
					//if ( currentLine.charAt(charNum + 2) == 0xf2) // check for
					// windows line
					// marker
					isAppendPostBlockEmptyLineRequested = false;
				isInLineComment = true;
				// do not indent if in column 1 or 2
				if (!lineCommentNoIndent)
				{
					if (charNum == 0)
						lineCommentNoIndent = true;
					else if (charNum == 1 && currentLine.charAt(0) == ' ')
						lineCommentNoIndent = true;
				}
				// move comment if spaces were added or deleted
				if (!lineCommentNoIndent && spacePadNum != 0)
					adjustComments();
				formattedLineCommentNum = formattedLine.length();

				// appendSequence will write the previous line
				appendSequence(ASResource.AS_OPEN_LINE_COMMENT);
				goForward(1);

				if (shouldBreakBlocks)
				{
					// break before the comment if a header follows the line
					// comment
					// for speed, do not check if previous line is empty,
					// if previous line is '{', or if previous line is a line
					// comment
					if (lineIsLineCommentOnly && !isImmediatelyPostEmptyLine
							&& !(previousCommandChar == '{')
							&& !isImmediatelyPostLineComment)
					{

						checkForFollowingHeader(new StringBuffer(currentLine
								.substring(charNum - 1)));
					}
				}

				if (previousCommandChar == '}')
					currentHeader = null;

				// explicitely break a line when a line comment's end is found.
				if ((charNum + 1) == currentLine.length())
				{
					isInLineBreak = true;
					isInLineComment = false;
					isImmediatelyPostLineComment = true;
					currentChar = 0; // make sure it is a neutral char.
				}
				continue;
			} else if (isSequenceReached("/*"))
			{
				isInComment = true;
				if (spacePadNum != 0)
					adjustComments();
				formattedLineCommentNum = formattedLine.length();
				appendSequence(ASResource.AS_OPEN_COMMENT);
				goForward(1);

				if (shouldBreakBlocks)
				{
					// break before the comment if a header follows the comment
					// for speed, do not check if previous line is empty,
					// if previous line is '{', or if previous line is a line
					// comment
					if (doesLineStartComment && !isImmediatelyPostEmptyLine
							&& !(previousCommandChar == '{')
							&& !isImmediatelyPostLineComment)
					{
						checkForFollowingHeader(new StringBuffer(currentLine
								.substring(charNum - 1)));
					}
				}

				if (previousCommandChar == '}')
					currentHeader = null;

				continue;
			} else if (currentChar == '"' || currentChar == '\'')
			{
				isInQuote = true;
				if (isSharpStyle() && previousChar == '@')
					isInVerbatimQuote = true;
				quoteChar = currentChar;
				appendCurrentChar();
				continue;
			}
			// treat these preprocessor statements as a line comment
			else if (currentChar == '#')
			{
				if (isSequenceReached("#region")
						|| isSequenceReached("#endregion")
						|| isSequenceReached("#error")
						|| isSequenceReached("#warning"))
				{
					isInLineComment = true;
					appendCurrentChar();
					continue;
				}
			}

			// handle white space - needed to simplify the rest.
			if (isWhiteSpace(currentChar) || isInPreprocessor)
			{
				appendCurrentChar();
				continue;
			}

			/* not in MIDDLE of quote or comment or white-space of any type ... */

			// need to reset 'previous' chars if appending a bracket
			if (appendOpeningBracket)
				previousCommandChar = previousNonWSChar = previousChar = '{';

			// check if in preprocessor
			// ** isInPreprocessor will be automatically reset at the begining
			// of a new line in getnextChar()
			if (currentChar == '#')
			{
				isInPreprocessor = true;
				processPreprocessor();
				// need to fall thru here to reset the variables
			}

			/* not in preprocessor ... */

			if (isImmediatelyPostComment)
			{
				isImmediatelyPostComment = false;
				isCharImmediatelyPostComment = true;
			}

			if (isImmediatelyPostLineComment)
			{
				isImmediatelyPostLineComment = false;
				isCharImmediatelyPostLineComment = true;
			}

			if (isImmediatelyPostReturn)
			{
				isImmediatelyPostReturn = false;
				isCharImmediatelyPostReturn = true;
			}

			if (isImmediatelyPostOperator)
			{
				isImmediatelyPostOperator = false;
				isCharImmediatelyPostOperator = true;
			}

			// reset isImmediatelyPostHeader information
			if (isImmediatelyPostHeader)
			{
				isImmediatelyPostHeader = false;

				// Make sure headers are broken from their succeeding blocks
				// (e.g.
				// if (isFoo) DoBar();
				// should become
				// if (isFoo)
				// DoBar;
				// )
				// But treat else if() as a special case which should not be
				// broken!
				if (shouldBreakOneLineStatements
						&& (shouldBreakOneLineBlocks || !isBracketType(
								bracketTypeStack.peek(),
								BracketType.SINGLE_LINE_TYPE)))
				{
					// if may break 'else if()'s, then simply break the line
					if (shouldBreakElseIfs)
						isInLineBreak = true;
				}
			}

			if (passedSemicolon) // need to break the formattedLine
			{
				passedSemicolon = false;
				if (parenStack.peek() == 0 && currentChar != ';') // allow ;;
				{
					// does a one-line statement have ending comments?
					if (isBracketType(bracketTypeStack.peek(),
							BracketType.SINGLE_LINE_TYPE))
					{
						int blockEnd = currentLine
								.lastIndexOf(ASResource.AS_CLOSE_BRACKET);
						assert (blockEnd != -1);
						// move ending comments to this formattedLine
						if (isBeforeLineEndComment(blockEnd))
						{
							int commentStart = ASUtils.findFirstNotOf(
									currentLine, ASUtils.WHITE_SPACE, blockEnd + 1);
							assert (commentStart != -1);
							assert ((currentLine.indexOf(
									"//",commentStart)==commentStart) || (currentLine.indexOf("/*", commentStart)==commentStart));
							int commentLength = currentLine.length()
									- commentStart;
							int tabCount = getIndentLength();
							appendSpacePad();
							formattedLine.append(ASUtils.repeat(tabCount, ' '));

							formattedLine.append(currentLine.substring(commentStart,
									commentStart+commentLength));

							currentLine.delete(commentStart, commentStart
									+ commentLength);
						}
					}
					shouldReparseCurrentChar = true;
					isInLineBreak = true;
					if (needHeaderOpeningBracket)
					{
						isCharImmediatelyPostCloseBlock = true;
						needHeaderOpeningBracket = false;
					}
					continue;
				}
			}

			if (passedColon)
			{
				passedColon = false;
				if (parenStack.peek() == 0 && !isBeforeComment())
				{
					shouldReparseCurrentChar = true;
					isInLineBreak = true;
					continue;
				}
			}

			// Check if in template declaration, e.g. foo<bar> or foo<bar,fig>
			// If so, set isInTemplate to true
			if (!isInTemplate && currentChar == '<')
			{
				int maxTemplateDepth = 0;
				templateDepth = 0;
				for (int i = charNum; i < currentLine.length(); i++)
				{
					char currentChar = currentLine.charAt(i);

					if (currentChar == '<')
					{
						templateDepth++;
						maxTemplateDepth++;
					} else if (currentChar == '>')
					{
						templateDepth--;
						if (templateDepth == 0)
						{
							// this is a template!
							isInTemplate = true;
							templateDepth = maxTemplateDepth;
							break;
						}
					} else if (currentChar == ',' // comma, e.g. A<int, char>
							|| currentChar == '&' // reference, e.g. A<int&>
							|| currentChar == '*' // pointer, e.g. A<int*>
							|| currentChar == ':' // ::, e.g. std::String
							|| currentChar == '[' // [] e.g. String[]
							|| currentChar == ']') // [] e.g. String[]
					{
						continue;
					} else if (!isLegalNameChar(currentChar)
							&& !isWhiteSpace(currentChar))
					{
						// this is not a template -> leave...
						isInTemplate = false;
						break;
					}
				}
			}

			// handle parenthesies
			if (currentChar == '(' || currentChar == '['
					|| (isInTemplate && currentChar == '<'))
			{
				parenStack.set(parenStack.size() - 1, Integer
						.valueOf(parenStack.peek().intValue() + 1));

				if (currentChar == '[')
					isInBlParen = true;
			} else if (currentChar == ')' || currentChar == ']'
					|| (isInTemplate && currentChar == '>'))
			{
				parenStack.set(parenStack.size() - 1, Integer
						.valueOf(parenStack.peek().intValue() - 1));

				if (isInTemplate && currentChar == '>')
				{
					templateDepth--;
					if (templateDepth == 0)
					{
						isInTemplate = false;
						isCharImmediatelyPostTemplate = true;
					}
				}

				// check if this parenthesis closes a header, e.g. if (...),
				// while (...)
				if (isInHeader && parenStack.peek() == 0)
				{
					isInHeader = false;
					isImmediatelyPostHeader = true;
				}
				if (currentChar == ']')
					isInBlParen = false;
				if (currentChar == ')')
					foundCastOperator = false;
			}

			// handle brackets
			if (currentChar == '{' || currentChar == '}')
			{
				if (currentChar == '{')
				{
					int newBracketType = getBracketType();
					foundNamespaceHeader = false;
					foundClassHeader = false;
					foundInterfaceHeader = false;
					foundPreDefinitionHeader = false;
					foundPreCommandHeader = false;
					isInPotentialCalculation = false;
					isJavaStaticConstructor = false;
					needHeaderOpeningBracket = false;

					bracketTypeStack.add(newBracketType);
					preBracketHeaderStack.add(currentHeader);
					currentHeader = null;

					isPreviousBracketBlockRelated = !isBracketType(
							newBracketType, BracketType.ARRAY_TYPE);
				}

				// this must be done before the bracketTypeStack is popped
				int bracketType = bracketTypeStack.peek();
				boolean isOpeningArrayBracket = (isBracketType(bracketType,
						BracketType.ARRAY_TYPE)
						&& bracketTypeStack.size() >= 2 && !isBracketType(
						bracketTypeStack.get(bracketTypeStack.size() - 2),
						BracketType.ARRAY_TYPE));

				if (currentChar == '}')
				{
					// if a request has been made to append a post block empty
					// line,
					// but the block exists immediately before a closing
					// bracket,
					// then there is no need for the post block empty line.
					//
					isAppendPostBlockEmptyLineRequested = false;

					if (bracketTypeStack.size() > 1)
					{
						previousBracketType = bracketTypeStack.peek();
						bracketTypeStack.pop();
						isPreviousBracketBlockRelated = !isBracketType(
								bracketType, BracketType.ARRAY_TYPE);
					} else
					{
						previousBracketType = BracketType.NULL_TYPE;
						isPreviousBracketBlockRelated = false;
					}

					if (!preBracketHeaderStack.empty())
					{
						currentHeader = preBracketHeaderStack.peek();
						preBracketHeaderStack.pop();
					} else
						currentHeader = null;
				}

				// format brackets
				if (isBracketType(bracketType, BracketType.ARRAY_TYPE))
					formatArrayBrackets(bracketType, isOpeningArrayBracket);
				else
					formatBrackets(bracketType);
				continue;
			}

			if (((previousCommandChar == '{' && isPreviousBracketBlockRelated) || ((previousCommandChar == '}'
					&& !isImmediatelyPostEmptyBlock
					&& isPreviousBracketBlockRelated
					&& !isPreviousCharPostComment // Fixes wrongly appended
					// newlines after '}'
					// immediately after
					// comments
					&& peekNextChar() != ' ' && !isBracketType(
					previousBracketType, BracketType.DEFINITION_TYPE)) && !isBracketType(
					bracketTypeStack.peek(), BracketType.DEFINITION_TYPE)))
					&& (shouldBreakOneLineBlocks || !isBracketType(
							bracketTypeStack.peek(),
							BracketType.SINGLE_LINE_TYPE)))
			{
				isCharImmediatelyPostOpenBlock = (previousCommandChar == '{');
				isCharImmediatelyPostCloseBlock = (previousCommandChar == '}');

				if (isCharImmediatelyPostOpenBlock
						|| (isCharImmediatelyPostCloseBlock
								&& shouldBreakOneLineStatements
								&& (isLegalNameChar(currentChar) && currentChar != '.') && !isCharImmediatelyPostComment))
				{
					previousCommandChar = ' ';
					isInLineBreak = true;
				}
			}

			// reset block handling flags
			isImmediatelyPostEmptyBlock = false;

			// look for headers
			boolean isPotentialHeader = isCharPotentialHeader(currentLine,
					charNum);

			if (isPotentialHeader && !isInTemplate)
			{
				newHeader = findHeader(headers);

				if (newHeader != null)
				{
					char peekChar = peekNextChar(currentLine,
							charNum + newHeader.length() - 1);

					// is not a header if part of a definition
					if (peekChar == ',' || peekChar == ')')
						newHeader = null;
					// the following accessor definitions are NOT headers
					// goto default; is NOT a header
					else if ((newHeader.equals(ASResource.AS_GET)
							|| newHeader.equals(ASResource.AS_SET) || newHeader
							.equals(ASResource.AS_DEFAULT))
							&& peekChar == ';')
					{
						newHeader = null;
					}
				}

				if (newHeader != null)
				{
					foundClosingHeader = false;
					String previousHeader;

					// recognize closing headers of do..while, if..else,
					// try..catch..finally
					if( currentHeader!= null)
					if ((newHeader.equals(ASResource.AS_ELSE) && currentHeader
							.equals(ASResource.AS_IF))
							|| (newHeader.equals(ASResource.AS_WHILE) && currentHeader
									.equals(ASResource.AS_DO))
							|| (newHeader.equals(ASResource.AS_CATCH) && currentHeader
									.equals(ASResource.AS_TRY))
							|| (newHeader.equals(ASResource.AS_CATCH) && currentHeader
									.equals(ASResource.AS_CATCH))
							|| (newHeader.equals(ASResource.AS_FINALLY) && currentHeader
									.equals(ASResource.AS_TRY))
							|| (newHeader.equals(ASResource.AS_FINALLY) && currentHeader
									.equals(ASResource.AS_CATCH))
							|| (newHeader.equals(ASResource.AS_SET) && currentHeader
									.equals(ASResource.AS_GET))
							|| (newHeader.equals(ASResource.AS_REMOVE) && currentHeader
									.equals(ASResource.AS_ADD)))
						foundClosingHeader = true;

					previousHeader = currentHeader;
					currentHeader = newHeader;
					needHeaderOpeningBracket = true;

					if (foundClosingHeader
							&& previousNonWSChar == '}'
							&& (shouldBreakOneLineBlocks || !isBracketType(
									bracketTypeStack.peek(),
									BracketType.SINGLE_LINE_TYPE)))
					{
						if (bracketFormatMode.equals(BracketMode.BREAK_MODE))
						{
							isInLineBreak = true;
						} else if (bracketFormatMode == BracketMode.NONE_MODE)
						{
							if (shouldBreakClosingHeaderBrackets
									|| isBracketIndent() || isBlockIndent())
							{
								isInLineBreak = true;
							} else
							{
								appendSpacePad();
								// is closing bracket broken?
								int i = ASUtils.findFirstNotOf(currentLine,
										ASUtils.WHITE_SPACE, 0);
								if (i != -1 && currentLine.charAt(i) == '}')
									isInLineBreak = false;

								if (shouldBreakBlocks)
									isAppendPostBlockEmptyLineRequested = false;
							}
						}
						// bracketFormatMode == ATTACH_MODE, LINUX_MODE,
						// STROUSTRUP_MODE
						else
						{
							if (shouldBreakClosingHeaderBrackets
									|| isBracketIndent() || isBlockIndent())
							{
								isInLineBreak = true;
							} else
							{
								spacePadNum = 0; // don't count as padding

								int firstChar = ASUtils.findFirstNotOf(
										formattedLine, ASUtils.WHITE_SPACE, 0);
								if (firstChar != -1) // if a blank line does not
								// preceed this
								{
									isInLineBreak = false;
									appendSpacePad();
								}

								if (shouldBreakBlocks)
									isAppendPostBlockEmptyLineRequested = false;
							}
						}
					}

					// check if the found header is non-paren header
					isNonParenHeader = nonParenHeaders.contains(newHeader);

					// join 'else if' statements
					if (previousHeader!=null && currentHeader.equals(ASResource.AS_IF)
							&& previousHeader.equals(ASResource.AS_ELSE)
							&& isInLineBreak && !shouldBreakElseIfs)
					{
						// 'else' must be last thing on the line, but must not
						// be #else
						int start = formattedLine.length() >= 6 ? formattedLine
								.length() - 6 : 0;
						if (formattedLine.indexOf("else", start) != -1
								&& formattedLine.indexOf("#else", start) == -1)
						{
							appendSpacePad();
							isInLineBreak = false;
						}
					}

					appendSequence(currentHeader, true);
					goForward(currentHeader.length() - 1);
					// if a paren-header is found add a space after it, if
					// needed
					// this checks currentLine, appendSpacePad() checks
					// formattedLine
					// in C# 'catch' can be either a paren or non-paren header
					if ((!isNonParenHeader || (currentHeader
							.equals(ASResource.AS_CATCH) && peekNextChar() == '('))
							&& !isWhiteSpace(currentLine
									.charAt(charNum + 1)))
						appendSpacePad();

					// Signal that a header has been reached
					// *** But treat a closing while() (as in do...while)
					// as if it were NOT a header since a closing while()
					// should never have a block after it!
					if (!(foundClosingHeader && currentHeader
							.equals(ASResource.AS_WHILE)))
					{
						isInHeader = true;
						// in C# 'catch' can be a paren or non-paren header
						if (isNonParenHeader && peekNextChar() != '(')
						{
							isImmediatelyPostHeader = true;
							isInHeader = false;
						}
					}

					if (shouldBreakBlocks
							&& (shouldBreakOneLineBlocks || !isBracketType(
									bracketTypeStack.peek(),
									BracketType.SINGLE_LINE_TYPE)))
					{
						if (previousHeader == null && !foundClosingHeader
								&& !isCharImmediatelyPostOpenBlock
								&& !isImmediatelyPostCommentOnly)
						{
							isPrependPostBlockEmptyLineRequested = true;
						}

						if (currentHeader.equals(ASResource.AS_ELSE)
								|| currentHeader.equals(ASResource.AS_CATCH)
								|| currentHeader.equals(ASResource.AS_FINALLY)
								|| foundClosingHeader)
						{
							isPrependPostBlockEmptyLineRequested = false;
						}

						if (shouldBreakClosingHeaderBlocks
								&& isCharImmediatelyPostCloseBlock
								&& !isImmediatelyPostCommentOnly
								&& !currentHeader.equals(ASResource.AS_WHILE)) // closing
						// do-while
						// block
						{
							isPrependPostBlockEmptyLineRequested = true;
						}

					}

					continue;
				} else if ((newHeader = findHeader(preDefinitionHeaders)) != null
						&& parenStack.peek() == 0)
				{
					if (newHeader.equals(ASResource.AS_NAMESPACE))
						foundNamespaceHeader = true;
					if (newHeader.equals(ASResource.AS_CLASS))
						foundClassHeader = true;
					if (newHeader.equals(ASResource.AS_INTERFACE))
						foundInterfaceHeader = true;
					foundPreDefinitionHeader = true;
					appendSequence(newHeader);
					goForward(newHeader.length() - 1);

					continue;
				} else if ((newHeader = findHeader(preCommandHeaders)) != null)
				{
					if (!(newHeader.equals(ASResource.AS_CONST) && previousCommandChar != ')')) // ''
						// member
						// functions
						// is
						// a
						// command
						// bracket
						foundPreCommandHeader = true;
					appendSequence(newHeader);
					goForward(newHeader.length() - 1);

					continue;
				} else if ((newHeader = findHeader(castOperators)) != null)
				{
					foundCastOperator = true;
					appendSequence(newHeader);
					goForward(newHeader.length() - 1);
					continue;
				}
			} // (isPotentialHeader && !isInTemplate)

			if (isInLineBreak) // OK to break line here
				breakLine();

			if (previousNonWSChar == '}' || currentChar == ';')
			{
				if (shouldBreakOneLineStatements
						&& currentChar == ';'
						&& (shouldBreakOneLineBlocks || !isBracketType(
								bracketTypeStack.peek(),
								BracketType.SINGLE_LINE_TYPE)))
				{
					passedSemicolon = true;
				}

				// append post block empty line for unbracketed header
				if (shouldBreakBlocks && currentChar == ';'
						&& currentHeader != null && parenStack.peek() == 0)
				{
					isAppendPostBlockEmptyLineRequested = true;
				}

				// end of block if a closing bracket was found
				// or an opening bracket was not found (';' closes)
				if (currentChar != ';'
						|| (needHeaderOpeningBracket && parenStack.peek() == 0))
					currentHeader = null;

				foundQuestionMark = false;
				foundNamespaceHeader = false;
				foundClassHeader = false;
				foundInterfaceHeader = false;
				foundPreDefinitionHeader = false;
				foundPreCommandHeader = false;
				foundCastOperator = false;
				isInPotentialCalculation = false;
				isNonInStatementArray = false;
				isSharpAccessor = false;
			}

			if (currentChar == ':' && shouldBreakOneLineStatements)
			{
				if (isInCase && previousChar != ':' // not part of '::'
						&& peekNextChar() != ':') // not part of '::'
				{
					isInCase = false;
					passedColon = true;
				} else if (isCStyle() // for C/C++ only
						&& !foundQuestionMark // not in a ... ? ... : ...
						// sequence
						&& !foundPreDefinitionHeader // not in a definition
						// block (e.g. class foo
						// : public bar
						&& previousCommandChar != ')' // not immediately after
						// closing paren of a
						// method header, e.g.
						// ASFormatter(...) :
						// ASBeautifier(...)
						&& previousChar != ':' // not part of '::'
						&& peekNextChar() != ':' // not part of '::'
						&& !Character.isDigit(peekNextChar())) // not a bit
				// field
				{
					passedColon = true;
				}
			}

			if (currentChar == '?')
				foundQuestionMark = true;

			if (isPotentialHeader && !isInTemplate)
			{
				if (findKeyword(currentLine, charNum, ASResource.AS_CASE)
						|| findKeyword(currentLine, charNum,
								ASResource.AS_DEFAULT))
					isInCase = true;

				if (findKeyword(currentLine, charNum, ASResource.AS_NEW))
					isInPotentialCalculation = false;

				if (findKeyword(currentLine, charNum, ASResource.AS_RETURN))
					isImmediatelyPostReturn = true;

				if (findKeyword(currentLine, charNum, ASResource.AS_OPERATOR))
					isImmediatelyPostOperator = true;

				if (isJavaStyle()
						&& (findKeyword(currentLine, charNum,
								ASResource.AS_STATIC) && isNextCharOpeningBracket(charNum + 6)))
					isJavaStaticConstructor = true;

				// append the entire name
				String name = getCurrentWord(currentLine, charNum);
				appendSequence(name);
				goForward(name.length() - 1);

				continue;

			} // (isPotentialHeader && !isInTemplate)

			// determine if this is a potential calculation

			boolean isPotentialOperator = isCharPotentialOperator(currentChar);
			newHeader = null;

			if (isPotentialOperator)
			{
				newHeader = findOperator(operators);

				if (newHeader != null)
				{
					// correct mistake of two >> closing a template
					if (isInTemplate
							&& (newHeader.equals(ASResource.AS_GR_GR) || newHeader
									.equals(ASResource.AS_GR_GR_GR)))
						newHeader = ASResource.AS_GR;

					if (!isInPotentialCalculation)
					{
						// must determine if newHeader is an assignment operator
						// do NOT use findOperator!!!
						if (assignmentOperators.contains(newHeader))
						{
							char peekedChar = peekNextChar();
							isInPotentialCalculation = (!(newHeader
									.equals(ASResource.AS_EQUAL) && peekedChar == '*') && !(newHeader
									.equals(ASResource.AS_EQUAL) && peekedChar == '&'));
						}
					}
				}
			}

			if (shouldPadOperators && newHeader != null)
			{
				padOperators(newHeader);
				continue;
			}

			// pad commas and semi-colons
			if (currentChar == ';'
					|| (currentChar == ',' && shouldPadOperators))
			{
				char nextChar = ' ';
				if (charNum + 1 < currentLine.length())
					nextChar = currentLine.charAt(charNum + 1);
				if (!isWhiteSpace(nextChar) && nextChar != '}'
						&& nextChar != ')' && nextChar != ']'
						&& nextChar != '>' && nextChar != ';'
						&& !isBeforeComment()
				/* && !(isBracketType(bracketTypeStack.peek(), ARRAY_TYPE)) */)
				{
					appendCurrentChar();
					appendSpaceAfter();
					continue;
				}
			}

			if ((shouldPadParensOutside || shouldPadParensInside || shouldUnPadParens)
					&& (currentChar == '(' || currentChar == ')'))
			{
				padParens();
				continue;
			}

			appendCurrentChar();
		} // end of while loop * end of while loop * end of while loop * end of
		// while loop

		// return a beautified (i.e. correctly indented) line.

		StringBuffer beautifiedLine;
		int readyFormattedLineLength = readyFormattedLine.toString().trim()
				.length();

		if (prependEmptyLine // prepend a blank line before this formatted line
				&& readyFormattedLineLength > 0
				&& previousReadyFormattedLineLength > 0)
		{
			isLineReady = true; // signal a waiting readyFormattedLine
			beautifiedLine = beautify(new StringBuffer());
			previousReadyFormattedLineLength = 0;
		} else
		// format the current formatted line
		{
			isLineReady = false;
			beautifiedLine = beautify(readyFormattedLine);
			previousReadyFormattedLineLength = readyFormattedLineLength;
			lineCommentNoBeautify = lineCommentNoIndent;
			lineCommentNoIndent = false;
			if (appendOpeningBracket) // insert bracket after this formatted
			// line
			{
				appendOpeningBracket = false;
				isLineReady = true; // signal a waiting readyFormattedLine
				readyFormattedLine = new StringBuffer("{");
				isPrependPostBlockEmptyLineRequested = false; // next line
				// should not be
				// empty
				lineCommentNoIndent = lineCommentNoBeautify; // restore variable
				lineCommentNoBeautify = false;
			}
		}

		prependEmptyLine = false;
		enhancer.enhance(beautifiedLine); // call the enhancer function
		return beautifiedLine;
	}

	/**
	 * check if there are any indented lines ready to be read by nextLine()
	 *
	 * @return are there any indented lines ready?
	 */
	public boolean hasMoreLines()
	{
		return !endOfCodeReached;
	}

	/**
	 * comparison function for BracketType enum
	 */
	boolean isBracketType(final int a, final int b)
	{
		return ((a & b) == b);
	}

	/**
	 * set the formatting style.
	 *
	 * @param mode
	 *            the formatting style.
	 */
	public void setFormattingStyle(FormatStyle style)
	{
		formattingStyle = style;
	}

	/**
	 * set the bracket formatting mode. options:
	 *
	 * @param mode
	 *            the bracket formatting mode.
	 */
	public void setBracketFormatMode(BracketMode mode)
	{
		bracketFormatMode = mode;
	}

	/**
	 * set closing header bracket breaking mode options: true brackets just
	 * before closing headers (e.g. 'else', 'catch') will be broken, even if
	 * standard brackets are attached. false closing header brackets will be
	 * treated as standard brackets.
	 *
	 * @param state
	 *            the closing header bracket breaking mode.
	 */
	public void setBreakClosingHeaderBracketsMode(boolean state)
	{
		shouldBreakClosingHeaderBrackets = state;
	}

	/**
	 * set 'else if()' breaking mode options: true 'else' headers will be broken
	 * from their succeeding 'if' headers. false 'else' headers will be attached
	 * to their succeeding 'if' headers.
	 *
	 * @param state
	 *            the 'else if()' breaking mode.
	 */
	public void setBreakElseIfsMode(boolean state)
	{
		shouldBreakElseIfs = state;
	}

	/**
	 * set operator padding mode. options: true statement operators will be
	 * padded with spaces around them. false statement operators will not be
	 * padded.
	 *
	 * @param state
	 *            the padding mode.
	 */
	public void setOperatorPaddingMode(boolean state)
	{
		shouldPadOperators = state;
	}

	/**
	 * set parenthesis outside padding mode. options: true statement
	 * parenthesiss will be padded with spaces around them. false statement
	 * parenthesiss will not be padded.
	 *
	 * @param state
	 *            the padding mode.
	 */
	public void setParensOutsidePaddingMode(boolean state)
	{
		shouldPadParensOutside = state;
	}

	/**
	 * set parenthesis inside padding mode. options: true statement parenthesis
	 * will be padded with spaces around them. false statement parenthesis will
	 * not be padded.
	 *
	 * @param state
	 *            the padding mode.
	 */
	public void setParensInsidePaddingMode(boolean state)
	{
		shouldPadParensInside = state;
	}

	/**
	 * set parenthesis unpadding mode. options: true statement parenthesis will
	 * be unpadded with spaces removed around them. false statement parenthesis
	 * will not be unpadded.
	 *
	 * @param state
	 *            the padding mode.
	 */
	public void setParensUnPaddingMode(boolean state)
	{
		shouldUnPadParens = state;
	}

	/**
	 * set option to break/not break one-line blocks
	 *
	 * @param state
	 *            true = break, false = don't break.
	 */
	public void setBreakOneLineBlocksMode(boolean state)
	{
		shouldBreakOneLineBlocks = state;
	}

	/**
	 * set option to break/not break lines consisting of multiple statements.
	 *
	 * @param state
	 *            true = break, false = don't break.
	 */
	public void setSingleStatementsMode(boolean state)
	{
		shouldBreakOneLineStatements = state;
	}

	/**
	 * set option to convert tabs to spaces.
	 *
	 * @param state
	 *            true = convert, false = don't convert.
	 */
	public void setTabSpaceConversionMode(boolean state)
	{
		shouldConvertTabs = state;
	}

	/**
	 * set option to break unrelated blocks of code with empty lines.
	 *
	 * @param state
	 *            true = convert, false = don't convert.
	 */
	public void setBreakBlocksMode(boolean state)
	{
		shouldBreakBlocks = state;
	}

	/**
	 * set option to break closing header blocks of code (such as 'else',
	 * 'catch', ...) with empty lines.
	 *
	 * @param state
	 *            true = convert, false = don't convert.
	 */
	public void setBreakClosingHeaderBlocksMode(boolean state)
	{
		shouldBreakClosingHeaderBlocks = state;
	}

	/**
	 * set option to delete empty lines.
	 *
	 * @param state
	 *            true = delete, false = don't delete.
	 */
	public void setDeleteEmptyLinesMode(boolean state)
	{
		shouldDeleteEmptyLines = state;
	}

	/**
	 * jump over several characters.
	 *
	 * @param i
	 *            the number of characters to jump over.
	 */
	private void goForward(int i)
	{
		while (--i >= 0)
			getNextChar();
	}

	/**
	 * peek at the next unread character.
	 *
	 * @return the next unread character.
	 */
	private char peekNextChar()
	{
		char ch = ' ';
		int peekNum = ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, charNum + 1);

		if (peekNum == -1)
			return ch;

		ch = currentLine.charAt(peekNum);

		return ch;
	}

	/**
	 * check if current placement is before a comment or line-comment
	 *
	 * @return is before a comment or line-comment.
	 */
	private boolean isBeforeComment()
	{
		boolean foundComment = false;
		int peekNum = ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, charNum + 1);

		if (peekNum == -1)
			return foundComment;

		foundComment = (currentLine.indexOf("/*",peekNum)==peekNum || currentLine.indexOf("//",peekNum)==peekNum);

		return foundComment;
	}

	/**
	 * check if current placement is before a comment or line-comment if a block
	 * comment it must be at the end of the line
	 *
	 * @return is before a comment or line-comment.
	 */
	private boolean isBeforeLineEndComment(int startPos)
	{
		boolean foundLineEndComment = false;
		int peekNum = ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, startPos + 1);

		if (peekNum != -1)
		{
			if (currentLine.indexOf("//", peekNum)==peekNum)
				foundLineEndComment = true;
			else if (currentLine.indexOf( "/*",peekNum)==peekNum)
			{
				// comment must be closed on this line with nothing after it
				int endNum = currentLine.indexOf("*/", peekNum + 2);
				if (endNum != -1)
					if (ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, endNum + 2) == -1)
						foundLineEndComment = true;
			}
		}
		return foundLineEndComment;
	}

	/**
	 * get the next character, increasing the current placement in the process.
	 * the new character is inserted into the variable currentChar.
	 *
	 * @return whether succeded to recieve the new character.
	 */
	private boolean getNextChar()
	{
		isInLineBreak = false;
		previousChar = currentChar;

		if (!isWhiteSpace(currentChar))
		{
			previousNonWSChar = currentChar;
			if (!isInComment && !isInLineComment && !isInQuote
					&& !isImmediatelyPostComment
					&& !isImmediatelyPostLineComment
					&& !isSequenceReached("/*") && !isSequenceReached("//"))
				previousCommandChar = currentChar;
		}

		if ((charNum + 1) < currentLine.length()
				&& (!isWhiteSpace(peekNextChar()) || isInComment || isInLineComment))
		{
			currentChar = currentLine.charAt(++charNum);

			if (shouldConvertTabs && currentChar == '\t')
				convertTabToSpaces();

			return true;
		}

		// end of line has been reached
		return getNextLine();
	}

	/**
	 * get the next line of input, increasing the current placement in the
	 * process. emptyLineWasDeleted=false
	 *
	 * @return whether succeded in reading the next line.
	 */
	private boolean getNextLine()
	{
		return getNextLine(false);
	}

	/**
	 * get the next line of input, increasing the current placement in the
	 * process.
	 *
	 * @param emptyLineWasDeleted
	 *            the sequence to append.
	 * @return whether succeded in reading the next line.
	 */
	private boolean getNextLine(boolean emptyLineWasDeleted /* false */)
	{
		if (sourceIterator.hasMoreLines())
		{
			currentLine.delete(0, currentLine.length());

			currentLine.append(sourceIterator.nextLine(emptyLineWasDeleted));

			// reset variables for new line
			spacePadNum = 0;
			inLineNumber++;
			isInCase = false;
			isInQuoteContinuation = isInVerbatimQuote
					| haveLineContinuationChar;
			haveLineContinuationChar = false;
			isImmediatelyPostEmptyLine = lineIsEmpty;
			previousChar = ' ';

			if (currentLine.length() == 0)
			{
				currentLine.append(" "); // a null is inserted if this is not
				// done
			}

			// unless reading in the first line of the file, break a new line.
			if (!isVirgin)
				isInLineBreak = true;
			else
				isVirgin = false;

			// check if is in preprocessor before line trimming
			// a blank line after a \ will remove the flag
			isImmediatelyPostPreprocessor = isInPreprocessor;
			if (previousNonWSChar != '\\'
					|| ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, 0) == -1)
				isInPreprocessor = false;

			trimNewLine();
			currentChar = currentLine.charAt(charNum);

			if (shouldConvertTabs && currentChar == '\t')
				convertTabToSpaces();

			// check for an empty line inside a command bracket.
			// if yes then read the next line (calls getNextLine recursively).
			// must be after trimNewLine.
			if (shouldDeleteEmptyLines
					&& lineIsEmpty
					&& isBracketType(bracketTypeStack.get(bracketTypeStack
							.size() - 1), BracketType.COMMAND_TYPE))
			{
				// but do NOT delete an empty line between comments if blocks
				// are being broken
				if (!(shouldBreakBlocks || shouldBreakClosingHeaderBlocks)
						|| !isImmediatelyPostCommentOnly
						|| !commentAndHeaderFollows())
				{
					isInPreprocessor = isImmediatelyPostPreprocessor; // restore
					// isInPreprocessor
					lineIsEmpty = false;
					return getNextLine(true);
				}
			}

			return true;
		} else
		{
			endOfCodeReached = true;
			return false;
		}
	}

	/**
	 * jump over the leading white space in the current line, IF the line does
	 * not begin a comment or is in a preprocessor definition.
	 */
	private void trimNewLine()
	{
		int len = currentLine.length();
		int indent = getIndentLength();
		charNum = 0;
		tabIncrementIn = 0;

		if (isInComment || isInPreprocessor || isInQuoteContinuation)
			return;

		while (isWhiteSpace(currentLine.charAt(charNum))
				&& charNum + 1 < len)
		{
			if (currentLine.charAt(charNum) == '\t')
				tabIncrementIn += indent - 1
						- ((tabIncrementIn + charNum) % indent);
			++charNum;
		}

		isImmediatelyPostCommentOnly = lineIsLineCommentOnly
				|| lineEndsInCommentOnly;
		lineIsLineCommentOnly = false;
		lineEndsInCommentOnly = false;
		doesLineStartComment = false;
		lineIsEmpty = false;
		if (isSequenceReached("/*"))
		{
			charNum = 0;
			tabIncrementIn = 0;
			doesLineStartComment = true;
		}
		if (isSequenceReached("//"))
		{
			lineIsLineCommentOnly = true;
		}
		if (isWhiteSpace(currentLine.charAt(charNum))
				&& !(charNum + 1 < currentLine.length()))
		{
			lineIsEmpty = true;
		}
	}

	/**
	 * append a String sequence to the current formatted line. Unless disabled
	 * (via canBreakLine == false), first check if a line-break has been
	 * registered, and if so break the formatted line, and only then append the
	 * sequence into the next formatted line.
	 *
	 * @param sequence
	 *            the sequence to append.
	 * @param canBreakLine
	 *            if true, a registered line-break
	 */
	private void appendSequence(String sequence, boolean canBreakLine)
	{
		if (canBreakLine && isInLineBreak)
			breakLine();

		formattedLine.append(sequence);
	}

	/**
	 * append a String sequence to the current formatted line. In this case
	 * canBreakLine=true, first check if a line-break has been registered, and
	 * if so break the formatted line, and only then append the sequence into
	 * the next formatted line.
	 *
	 * @param sequence
	 *            the sequence to append.
	 */
	private void appendSequence(String sequence)
	{
		appendSequence(sequence, true);
	}

	/**
	 * append a space to the current formattedline, UNLESS the last character is
	 * already a white-space character.
	 */
	private void appendSpacePad()
	{
		int len = formattedLine.length();
		if (len > 0 && !isWhiteSpace(formattedLine.charAt(len - 1)))
		{
			formattedLine.append(' ');
			spacePadNum++;
		}
	}

	/**
	 * append a space to the current formattedline, UNLESS the next character is
	 * already a white-space character.
	 */
	private void appendSpaceAfter()
	{
		int len = currentLine.length();
		if (charNum + 1 < len
				&& !isWhiteSpace(currentLine.charAt(charNum + 1)))
		{
			formattedLine.append(' ');
			spacePadNum++;
		}
	}

	/**
	 * register a line break for the formatted line.
	 */
	private void breakLine()
	{
		isLineReady = true;
		isInLineBreak = false;
		spacePadNum = 0;
		formattedLineCommentNum = -1;

		// queue an empty line prepend request if one exists
		prependEmptyLine = isPrependPostBlockEmptyLineRequested;

		readyFormattedLine.delete(0, readyFormattedLine.length());
		readyFormattedLine.append(formattedLine);

		if (isAppendPostBlockEmptyLineRequested)
		{
			isAppendPostBlockEmptyLineRequested = false;
			isPrependPostBlockEmptyLineRequested = true;
		} else
		{
			isPrependPostBlockEmptyLineRequested = false;
		}

		formattedLine.delete(0, formattedLine.length());
	}

	/**
	 * check if the currently reached open-bracket (i.e. '{') opens a: - a
	 * definition type block (such as a class or namespace), - a command block
	 * (such as a method block) - a static array this method takes for granted
	 * that the current character is an opening bracket.
	 *
	 * @return the type of the opened block.
	 */
	private int getBracketType()
	{
		assert (currentChar == '{');

		int returnVal;

		if (previousNonWSChar == '=')
			returnVal = BracketType.ARRAY_TYPE;
		else if (foundPreDefinitionHeader)
		{
			returnVal = BracketType.DEFINITION_TYPE;
			if (foundNamespaceHeader)
				returnVal = (returnVal | BracketType.NAMESPACE_TYPE);
			else if (foundClassHeader)
				returnVal = (returnVal | BracketType.CLASS_TYPE);
			else if (foundInterfaceHeader)
				returnVal = (returnVal | BracketType.INTERFACE_TYPE);
		} else
		{
			boolean isCommandType = (foundPreCommandHeader
					|| (currentHeader != null && isNonParenHeader)
					|| (previousCommandChar == ')')
					|| (previousCommandChar == ':' && !foundQuestionMark)
					|| (previousCommandChar == ';')
					|| ((previousCommandChar == '{' || previousCommandChar == '}') && isPreviousBracketBlockRelated) || isJavaStaticConstructor);

			// C# methods containing 'get', 'set', 'add', and 'remove' do NOT
			// end with parens
			if (!isCommandType && isSharpStyle()
					&& isNextWordSharpNonParenHeader(charNum + 1))
			{
				isCommandType = true;
				isSharpAccessor = true;
			}

			returnVal = (isCommandType ? BracketType.COMMAND_TYPE
					: BracketType.ARRAY_TYPE);
		}

		if (isOneLineBlockReached())
			returnVal = (returnVal | BracketType.SINGLE_LINE_TYPE);

		return returnVal;
	}

	/**
	 * check if the currently reached '*' or '&' character is a
	 * pointer-or-reference symbol, or another operator. this method takes for
	 * granted that the current character is either a '*' or '&'.
	 *
	 * @return whether current character is a reference-or-pointer
	 */
	private boolean isPointerOrReference()
	{
		assert (currentChar == '*' || currentChar == '&');

		boolean isPR;
		isPR = (!isInPotentialCalculation
				|| isBracketType(bracketTypeStack.peek(),
						BracketType.DEFINITION_TYPE) || (!isLegalNameChar(previousNonWSChar)
				&& previousNonWSChar != ')' && previousNonWSChar != ']'));

		if (!isPR)
		{
			char nextChar = peekNextChar();
			isPR |= (!isWhiteSpace(nextChar) && nextChar != '-'
					&& nextChar != '(' && nextChar != '[' && !isLegalNameChar(nextChar));
		}

		return isPR;
	}

	/**
	 * check if the currently reached '+' or '-' character is a unary operator
	 * this method takes for granted that the current character is a '+' or '-'.
	 *
	 * @return whether the current '+' or '-' is a unary operator.
	 */
	private boolean isUnaryOperator()
	{
		assert (currentChar == '+' || currentChar == '-');

		return ((isCharImmediatelyPostReturn || !isLegalNameChar(previousCommandChar))
				&& previousCommandChar != '.'
				&& previousCommandChar != '\"'
				&& previousCommandChar != '\'' && previousCommandChar != ')' && previousCommandChar != ']');
	}

	/**
	 * check if the currently reached '+' or '-' character is part of an
	 * exponent, i.e. 0.2E-5.
	 *
	 * this method takes for granted that the current character is a '+' or '-'.
	 *
	 * @return whether the current '+' or '-' is in an exponent.
	 */
	private boolean isInExponent()
	{
		assert (currentChar == '+' || currentChar == '-');

		int formattedLineLength = formattedLine.length();
		if (formattedLineLength >= 2)
		{
			char prevPrevFormattedChar = formattedLine
					.charAt(formattedLineLength - 2);
			char prevFormattedChar = formattedLine
					.charAt(formattedLineLength - 1);

			return ((prevFormattedChar == 'e' || prevFormattedChar == 'E') && (prevPrevFormattedChar == '.' || Character
					.isDigit(prevPrevFormattedChar)));
		} else
			return false;
	}

	/**
	 * check if a one-line bracket has been reached, i.e. if the currently
	 * reached '{' character is closed with a complimentry '}' elsewhere on the
	 * current line, .
	 *
	 * @return has a one-line bracket been reached?
	 */
	private boolean isOneLineBlockReached()
	{
		boolean isInComment = false;
		boolean isInQuote = false;
		int bracketCount = 1;
		int currentLineLength = currentLine.length();
		char quoteChar = ' ';

		for (int i = charNum + 1; i < currentLineLength; ++i)
		{
			char ch = currentLine.charAt(i);

			if (isInComment)
			{
				if (currentLine.indexOf( "*/", i)==i)
				{
					isInComment = false;
					++i;
				}
				continue;
			}

			if (ch == '\\')
			{
				++i;
				continue;
			}

			if (isInQuote)
			{
				if (ch == quoteChar)
					isInQuote = false;
				continue;
			}

			if (ch == '"' || ch == '\'')
			{
				isInQuote = true;
				quoteChar = ch;
				continue;
			}

			if (currentLine.indexOf("//", i)==i)
				break;

			if (currentLine.indexOf("/*",i)==i)
			{
				isInComment = true;
				++i;
				continue;
			}

			if (ch == '{')
				++bracketCount;
			else if (ch == '}')
				--bracketCount;

			if (bracketCount == 0)
				return true;
		}

		return false;
	}

	/**
	 * check if a line begins with the specified character i.e. if the current
	 * line begins with a open bracket.
	 *
	 * @return true or false
	 */
	private boolean lineBeginsWith(char charToCheck)
	{
		boolean beginsWith = false;
		int i = ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, 0);

		if (i != -1)
			if (currentLine.charAt(i) == charToCheck && i == charNum)
				beginsWith = true;

		return beginsWith;
	}

	/**
	 * peek at the next word to determine if it is a C# non-paren header. will
	 * look ahead in the input file if necessary.
	 *
	 * @param char position on currentLine to start the search
	 * @return true if the next word is get or set.
	 */
	private boolean isNextWordSharpNonParenHeader(int startChar)
	{
		// look ahead to find the next non-comment text
		StringBuffer nextText = peekNextText(new StringBuffer(currentLine
				.substring(startChar)));
		if (nextText.length() == 0 || !isCharPotentialHeader(nextText, 0))
			return false;
		if (findKeyword(nextText, 0, ASResource.AS_GET)
				|| findKeyword(nextText, 0, ASResource.AS_SET)
				|| findKeyword(nextText, 0, ASResource.AS_ADD)
				|| findKeyword(nextText, 0, ASResource.AS_REMOVE))
			return true;
		return false;
	}

	/**
	 * peek at the next char to determine if it is an opening bracket. will look
	 * ahead in the input file if necessary. this determines a java static
	 * constructor.
	 *
	 * @param char position on currentLine to start the search
	 * @return true if the next word is an opening bracket.
	 */
	private boolean isNextCharOpeningBracket(int startChar)
	{
		StringBuffer nextText = peekNextText(new StringBuffer(currentLine
				.substring(startChar)));
		return (nextText.charAt(0)=='{');
	}

	/**
	 * get the next non-whitespace subString on following lines, bypassing all
	 * comments. endOnEmptyLine = false
	 *
	 * @param firstLine
	 *            the first line to check
	 * @return the next non-whitespace subString.
	 */
	private StringBuffer peekNextText(StringBuffer firstLine)
	{
		return peekNextText(firstLine, false);
	}

	/**
	 * get the next non-whitespace subString on following lines, bypassing all
	 * comments.
	 *
	 * @param the
	 *            first line to check
	 * @return the next non-whitespace subString.
	 */
	private StringBuffer peekNextText(StringBuffer firstLine, boolean endOnEmptyLine /* false */)
	{
		boolean isFirstLine = true;
		boolean needReset = false;
		StringBuffer nextLine = firstLine;
		int firstChar = -1;

		// find the first non-blank text, bypassing all comments.
		boolean isInComment = false;
		while (sourceIterator.hasMoreLines())
		{
			if (isFirstLine)
			{
				isFirstLine = false;
			}
			else
			{
				nextLine = sourceIterator.peekNextLine();
				needReset=true;
			}

			firstChar = ASUtils.findFirstNotOf(nextLine, ASUtils.WHITE_SPACE, 0);
			if (firstChar == -1)
			{
				if (endOnEmptyLine && !isInComment)
				{
					break;
				}
				else
					continue;
			}

			if (nextLine.indexOf("/*",firstChar)==firstChar)
				isInComment = true;

			if (isInComment)
			{
				firstChar = nextLine.indexOf("*/", firstChar);
				if (firstChar == -1)
					continue;
				firstChar += 2;
				isInComment = false;
				firstChar = ASUtils.findFirstNotOf(nextLine, ASUtils.WHITE_SPACE, firstChar);
				if (firstChar == -1)
					continue;
			}

			if (nextLine.indexOf("//", firstChar)==firstChar)
			{
				continue;
			}

			// found the next text
			break;
		}
		if (needReset)
			sourceIterator.peekReset();
		if (firstChar == -1)
			nextLine = new StringBuffer("");
		else
			nextLine = new StringBuffer(nextLine.substring(firstChar));
		return nextLine;
	}

	/**
	 * adjust comment position because of adding or deleting spaces the spaces
	 * are added or deleted to formattedLine spacePadNum contains the adjustment
	 */
	private void adjustComments()
	{
		assert (spacePadNum != 0);
		assert (currentLine.indexOf("//", charNum)==charNum || currentLine.indexOf("/*", charNum)==charNum);

		// block comment must be closed on this line with nothing after it
		if (currentLine.indexOf("/*", charNum)==charNum)
		{
			int endNum = currentLine.indexOf("*/", charNum + 2);
			if (endNum == -1)
				return;
			if (ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, endNum + 2) != -1)
				return;
		}

		int len = formattedLine.length();
		// if spaces were removed, need to add spaces before the comment
		if (spacePadNum < 0)
		{
			int adjust = -spacePadNum; // make the number positive
			if (formattedLine.charAt(len - 1) != '\t') // don't adjust if a tab
			{
				formattedLine.append(ASUtils.repeat(adjust, ' '));
			}
			// else // comment out to avoid compiler warning
			// adjust = 0;
		}
		// if spaces were added, need to delete spaces before the comment, if
		// possible
		else if (spacePadNum > 0)
		{
			int adjust = spacePadNum;
			if (ASUtils.findLastNotOf(formattedLine, " ") < (len - adjust - 1)
					&& formattedLine.charAt(len - 1) != '\t') // don't adjust a
				// tab
				formattedLine.setLength(len - adjust);
		}
	}

	/**
	 * append the current bracket inside the end of line comments currentChar
	 * contains the bracket, it will be appended to formattedLine
	 * formattedLineCommentNum is the comment location on formattedLine
	 */
	private void appendCharInsideComments()
	{
		if (formattedLineCommentNum == -1 // does the comment start on the
				// previous line?
				|| isBeforeComment()) // does a comment follow on this line?
		{
			appendCurrentChar(); // don't attach
			return;
		}
		assert (formattedLine.indexOf(
				 "//", (int)formattedLineCommentNum)==(int)formattedLineCommentNum || formattedLine
				.indexOf("/*",(int) formattedLineCommentNum)==(int)formattedLineCommentNum);

		// find the previous non space char
		int end = (int) formattedLineCommentNum;
		int beg = ASUtils.findLastNotOf(formattedLine, ASUtils.WHITE_SPACE, end - 1);
		if (beg == -1) // is the previous line comment only?
		{
			appendCurrentChar(); // don't attach
			return;
		}
		beg++;

		// insert the bracket
		if (end - beg < 3) // is there room to insert?
		{
			formattedLine.insert(beg, ASUtils.repeat(3 - end + beg, ' '));
		}
		if (formattedLine.charAt(beg) == '\t') // don't pad with a tab
		{
			formattedLine.insert(beg, ' ');
		}
		System.err.println(currentChar+": " + formattedLine);
		formattedLine.setCharAt(beg + 1, currentChar);
	}

	/**
	 * add or remove space padding to operators currentChar contains the paren
	 * the operators and necessary padding will be appended to formattedLine the
	 * calling function should have a continue statement after calling this
	 * method
	 *
	 * @param *newOperator the operator to be padded
	 */
	private void padOperators(String newOperator)
	{
		assert (newOperator != null);

		boolean shouldPad = (!newOperator.equals(ASResource.AS_COLON_COLON)
				&& !newOperator.equals(ASResource.AS_PAREN_PAREN)
				&& !newOperator.equals(ASResource.AS_BLPAREN_BLPAREN)
				&& !newOperator.equals(ASResource.AS_PLUS_PLUS)
				&& !newOperator.equals(ASResource.AS_MINUS_MINUS)
				&& !newOperator.equals(ASResource.AS_NOT)
				&& !newOperator.equals(ASResource.AS_BIT_NOT)
				&& !newOperator.equals(ASResource.AS_ARROW)
				&& !(newOperator.equals(ASResource.AS_MINUS) && isInExponent())
				&& !((newOperator.equals(ASResource.AS_PLUS) || newOperator
						.equals(ASResource.AS_MINUS)) // check for unary plus or
				// minus
				&& (previousNonWSChar == '(' || previousNonWSChar == '=' || previousNonWSChar == ','))
				&& !(newOperator.equals(ASResource.AS_PLUS) && isInExponent())
				&& !isCharImmediatelyPostOperator
				&& !((newOperator.equals(ASResource.AS_MULT) || newOperator
						.equals(ASResource.AS_BIT_AND)) && isPointerOrReference())
				&& !(newOperator.equals(ASResource.AS_MULT) && (previousNonWSChar == '.' || previousNonWSChar == '>')) // check
				// for
				// ->
				&& !((isInTemplate || isCharImmediatelyPostTemplate) && (newOperator
						.equals(ASResource.AS_LS) || newOperator
						.equals(ASResource.AS_GR)))
				&& !(newOperator.equals(ASResource.AS_GCC_MIN_ASSIGN) && peekNextChar(currentLine, charNum + 1) == '>')
				&& !(newOperator.equals(ASResource.AS_GR) && previousNonWSChar == '?') && !isInCase);

		// pad before operator
		if (shouldPad
				&& !isInBlParen
				&& !(newOperator.equals(ASResource.AS_COLON) && !foundQuestionMark)
				&& !(newOperator.equals(ASResource.AS_QUESTION)
						&& isSharpStyle() // check for C# nullable type (e.g.
				// int?)
				&& currentLine.indexOf(":", charNum + 1) == -1))
			appendSpacePad();
		appendSequence(newOperator);
		goForward(newOperator.length() - 1);

		// since this block handles '()' and '[]',
		// the parenStack must be updated here accordingly!
		if (newOperator.equals(ASResource.AS_PAREN_PAREN)
				|| newOperator.equals(ASResource.AS_BLPAREN_BLPAREN))
		{
			parenStack.set(parenStack.size() - 1, parenStack.peek() - 1);
		}
		currentChar = newOperator.charAt(newOperator.length() - 1);
		// pad after operator
		// but do not pad after a '-' that is a unary-minus.
		if (shouldPad && currentLine.length()<(charNum+1)
				&& !isInBlParen
				&& !isBeforeComment()
				&& !(newOperator.equals(ASResource.AS_PLUS) && isUnaryOperator())
				&& !(newOperator.equals(ASResource.AS_MINUS) && isUnaryOperator())
				&& !(currentLine.charAt(charNum + 1)==';')
				&& !(currentLine.indexOf("::", charNum + 1)==(charNum + 1))
				&& !(newOperator.equals(ASResource.AS_QUESTION)
						&& isSharpStyle() // check for C# nullable type (e.g.
				// int?)
				&& currentLine.charAt(charNum + 1) == '['))
			appendSpaceAfter();
	}

	/**
	 * add or remove space padding to parens currentChar contains the paren the
	 * parens and necessary padding will be appended to formattedLine the
	 * calling function should have a continue statement after calling this
	 * method
	 */
	private void padParens()
	{
		assert (currentChar == '(' || currentChar == ')');

		if (currentChar == '(')
		{
			int spacesOutsideToDelete = formattedLine.length() - 1;
			int spacesInsideToDelete = 0;

			// compute spaces outside the opening paren to delete
			if (shouldUnPadParens)
			{
				char lastChar = ' ';
				boolean prevIsParenHeader = false;
				int i = ASUtils.findLastNotOf(formattedLine, ASUtils.WHITE_SPACE);
				if (i != -1)
				{
					int end = i;
					spacesOutsideToDelete -= i;
					lastChar = formattedLine.charAt(i);
					// was last word a paren header?
					int start; // start of the previous word
					for (start = i; start > -1; start--)
					{
						if (!isLegalNameChar(formattedLine.charAt(start)))
							break;
					}
					start++;
					// if previous word is a header, it will be a paren header
					String prevWord = formattedLine
							.substring(start, end + 1);
					String prevWordH = null;
					if (prevWord.length() > 0
							&& isCharPotentialHeader(new StringBuffer(prevWord), 0))
						prevWordH = findHeader(formattedLine, start,
								headers);
					if (prevWordH != null)
					{
						prevIsParenHeader = true;
					} else if (prevWord.equals("return") // don't unpad return
							// statements
							|| prevWord.equals("*")) // don't unpad multiply or
					// pointer
					{
						prevIsParenHeader = true;
					}
					// don't unpad variables
					else if (prevWord.equals("boolean")
							|| prevWord.equals("int")
							|| prevWord.equals("void")
							|| prevWord.equals("void*")
							|| (prevWord.length() >= 6 // check end of word for
							// _t
							&& prevWord.indexOf(
									"_t",prevWord.length() - 2)==(prevWord.length() - 2))
							|| prevWord.equals("BOOL")
							|| prevWord.equals("DWORD")
							|| prevWord.equals("HWND")
							|| prevWord.equals("INT")
							|| prevWord.equals("LPSTR")
							|| prevWord.equals("VOID")
							|| prevWord.equals("LPVOID"))
					{
						prevIsParenHeader = true;
					}
				}
				// do not unpad operators, but leave them if already padded
				if (shouldPadParensOutside || prevIsParenHeader)
					spacesOutsideToDelete--;
				else if (lastChar == '|' // check for ||
						|| lastChar == '&' // check for &&
						|| lastChar == ','
						|| (lastChar == '>' && !foundCastOperator)
						|| lastChar == '<'
						|| lastChar == '?'
						|| lastChar == ':'
						|| lastChar == ';'
						|| lastChar == '='
						|| lastChar == '+'
						|| lastChar == '-'
						|| (lastChar == '*' && isInPotentialCalculation)
						|| lastChar == '/' || lastChar == '%')
					spacesOutsideToDelete--;

				if (spacesOutsideToDelete > 0)
				{
					formattedLine.delete(i + 1, i + 1 + spacesOutsideToDelete);
					spacePadNum -= spacesOutsideToDelete;
				}
			}

			// pad open paren outside
			char peekedCharOutside = peekNextChar();
			if (shouldPadParensOutside)
				if (!(currentChar == '(' && peekedCharOutside == ')'))
					appendSpacePad();

			appendCurrentChar();

			// unpad open paren inside
			if (shouldUnPadParens)
			{
				int j = ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, charNum + 1);
				if (j != -1)
					spacesInsideToDelete = j - charNum - 1;
				if (shouldPadParensInside)
					spacesInsideToDelete--;
				if (spacesInsideToDelete > 0)
				{
					currentLine.delete(charNum + 1, charNum + 1
							+ spacesInsideToDelete);
					spacePadNum -= spacesInsideToDelete;
				}
				// convert tab to space if requested
				if (shouldConvertTabs && currentLine.length() > charNum
						&& currentLine.charAt(charNum + 1) == '\t')
					currentLine.setCharAt(charNum + 1, ' ');

			}

			// pad open paren inside
			char peekedCharInside = peekNextChar();
			if (shouldPadParensInside)
				if (!(currentChar == '(' && peekedCharInside == ')'))
					appendSpaceAfter();

		} else if (currentChar == ')' /* || currentChar == ']' */)
		{
			int spacesOutsideToDelete = 0;
			int spacesInsideToDelete = formattedLine.length();

			// unpad close paren inside
			if (shouldUnPadParens)
			{
				int i = ASUtils.findLastNotOf(formattedLine, ASUtils.WHITE_SPACE);
				if (i != -1)
					spacesInsideToDelete = formattedLine.length() - 1 - i;
				if (shouldPadParensInside)
					spacesInsideToDelete--;
				if (spacesInsideToDelete > 0)
				{
					formattedLine.delete(i + 1, i + 1 + spacesInsideToDelete);
					spacePadNum -= spacesInsideToDelete;
				}
			}

			// pad close paren inside
			if (shouldPadParensInside)
				if (!(previousChar == '(' && currentChar == ')'))
					appendSpacePad();

			appendCurrentChar();

			// unpad close paren outside
			if (shouldUnPadParens)
			{
				// may have end of line comments
				int j = ASUtils.findFirstNotOf(currentLine, ASUtils.WHITE_SPACE, charNum + 1);
				if (j != -1)
					if (currentLine.charAt(j) == '['
							|| currentLine.charAt(j) == ']')
						spacesOutsideToDelete = j - charNum - 1;
				if (shouldPadParensOutside)
					spacesOutsideToDelete--;

				if (spacesOutsideToDelete > 0)
				{
					currentLine.delete(charNum + 1, charNum + 1
							+ spacesOutsideToDelete);
					spacePadNum -= spacesOutsideToDelete;
				}
			}

			// pad close paren outside
			char peekedCharOutside = peekNextChar();
			if (shouldPadParensOutside)
				if (peekedCharOutside != ';' && peekedCharOutside != ','
						&& peekedCharOutside != '.' && peekedCharOutside != '-') // check
					// for
					// ->
					appendSpaceAfter();

		}
	}

	/**
	 * format brackets as attached or broken currentChar contains the bracket
	 * the brackets will be appended to the current formattedLine or a new
	 * formattedLine as necessary the calling function should have a continue
	 * statement after calling this method
	 *
	 * @param bracketType
	 *            the type of bracket to be formatted.
	 */
	private void formatBrackets(int bracketType)
	{
		assert (!isBracketType(bracketType, BracketType.ARRAY_TYPE));
		assert (currentChar == '{' || currentChar == '}');

		if (currentChar == '{')
		{
			parenStack.add(0);
		} else if (currentChar == '}')
		{
			// parenStack must contain one entry
			if (parenStack.size() > 1)
			{
				parenStack.pop();
			}
		}

		if (currentChar == '{')
		{
			// break or attach the bracket
			boolean breakBracket = false;
			if (bracketFormatMode == BracketMode.NONE_MODE)
			{
				if (lineBeginsWith('{')) // is opening bracket broken?
					breakBracket = true;
			} else if (bracketFormatMode == BracketMode.BREAK_MODE)
			{
				breakBracket = true;
			} else if (bracketFormatMode == BracketMode.LINUX_MODE
					|| bracketFormatMode == BracketMode.STROUSTRUP_MODE)
			{
				// first entry in bracketTypeStack is NULL_TYPE
				int bracketTypeStackEnd = bracketTypeStack.size() - 1;

				// break a class if Linux
				if (isBracketType(bracketTypeStack.get(bracketTypeStackEnd),
						BracketType.CLASS_TYPE))
				{
					if (bracketFormatMode == BracketMode.LINUX_MODE)
						breakBracket = true;
				}
				// break a namespace or interface if Linux
				else if (isBracketType(bracketTypeStack
						.get(bracketTypeStackEnd), BracketType.NAMESPACE_TYPE)
						|| isBracketType(bracketTypeStack
								.get(bracketTypeStackEnd),
								BracketType.INTERFACE_TYPE))
				{
					if (bracketFormatMode == BracketMode.LINUX_MODE)
						breakBracket = true;
				}
				// break the first bracket if a function
				else if (bracketTypeStackEnd == 1
						&& isBracketType(bracketTypeStack
								.get(bracketTypeStackEnd),
								BracketType.COMMAND_TYPE))
				{
					breakBracket = true;
				} else if (bracketTypeStackEnd > 1)
				{
					// break the first bracket after a namespace if a function
					if (isBracketType(bracketTypeStack
							.get(bracketTypeStackEnd - 1),
							BracketType.NAMESPACE_TYPE))
					{
						if (isBracketType(bracketTypeStack
								.get(bracketTypeStackEnd),
								BracketType.COMMAND_TYPE))
							breakBracket = true;
					}
					// if not C style then break the first bracket after a class
					// if a function
					else if (!isCStyle())
					{
						if (isBracketType(bracketTypeStack
								.get(bracketTypeStackEnd - 1),
								BracketType.CLASS_TYPE)
								&& isBracketType(bracketTypeStack
										.get(bracketTypeStackEnd),
										BracketType.COMMAND_TYPE))
							breakBracket = true;
					}
				}
			}

			if (breakBracket)
			{
				if (isBeforeComment()
						&& (shouldBreakOneLineBlocks || !isBracketType(
								bracketType, BracketType.SINGLE_LINE_TYPE)))
				{
					// if comment is at line end leave the comment on this line
					if (isBeforeLineEndComment(charNum) && !lineBeginsWith('{'))
					{
						currentChar = ' '; // remove bracket from current line
						appendOpeningBracket = true; // append bracket to
						// following line
					}
					// else put comment after the bracket
					else
						breakLine();
				} else if (!isBracketType(bracketType,
						BracketType.SINGLE_LINE_TYPE))
					breakLine();
				else if (shouldBreakOneLineBlocks && peekNextChar() != '}')
					breakLine();
				else if (!isInLineBreak)
					appendSpacePad();

				appendCurrentChar();
			} else
			// attach bracket
			{
				// are there comments before the bracket?
				if (isCharImmediatelyPostComment
						|| isCharImmediatelyPostLineComment)
				{
					if ((shouldBreakOneLineBlocks || !isBracketType(
							bracketType, BracketType.SINGLE_LINE_TYPE))
							&& peekNextChar() != '}'
							&& previousCommandChar != '{' // don't attach { {
							&& previousCommandChar != '}' // don't attach } {
							&& previousCommandChar != ';') // don't attach ; {
						appendCharInsideComments();
					else
						appendCurrentChar(); // don't attach
				} else if (previousCommandChar == '{'
						|| previousCommandChar == '}'
						|| previousCommandChar == ';') // '}' , ';' chars added
				// for proper handling
				// of '{' immediately
				// after a '}' or ';'
				{
					appendCurrentChar(); // don't attach
				} else
				{
					// if a blank line preceeds this don't attach
					int firstChar = ASUtils.findFirstNotOf(formattedLine,
							ASUtils.WHITE_SPACE, 0);
					if (firstChar == -1)
						appendCurrentChar(); // don't attach
					else if ((shouldBreakOneLineBlocks
							|| !isBracketType(bracketType,
									BracketType.SINGLE_LINE_TYPE) || peekNextChar() == '}')
							&& !(isImmediatelyPostPreprocessor && lineBeginsWith('{')))
					{
						appendSpacePad();
						appendCurrentChar(false); // OK to attach
					} else
					{
						if (!isInLineBreak)
							appendSpacePad();
						appendCurrentChar(); // don't attach
					}
				}
			}
		} else if (currentChar == '}')
		{
			// mark state of immediately after empty block
			// this state will be used for locating brackets that appear
			// immedately AFTER an empty block (e.g. '{} \n}').
			if (previousCommandChar == '{')
				isImmediatelyPostEmptyBlock = true;

			if ((!(previousCommandChar == '{' && isPreviousBracketBlockRelated)) // this
					// '{'
					// does
					// not
					// close
					// an
					// empty
					// block
					&& (shouldBreakOneLineBlocks || !isBracketType(bracketType,
							BracketType.SINGLE_LINE_TYPE)) // astyle is allowed
					// to break on line
					// blocks
					&& !isImmediatelyPostEmptyBlock) // this '}' does not
			// immediately follow an
			// empty block
			{
				breakLine();
				appendCurrentChar();
			} else
			{
				if (!isCharImmediatelyPostComment
				// && !bracketFormatMode == NONE_MODE
						&& !isImmediatelyPostEmptyBlock)
					isInLineBreak = false;

				appendCurrentChar();
			}

			// if a declaration follows a definition, space pad
			if (isLegalNameChar(peekNextChar()))
				appendSpaceAfter();

			if (shouldBreakBlocks && currentHeader != null
					&& parenStack.peek() == 0)
			{
				isAppendPostBlockEmptyLineRequested = true;
			}
		}
	}

	/**
	 * format array brackets as attached or broken determine if the brackets can
	 * have an inStatement indent currentChar contains the bracket the brackets
	 * will be appended to the current formattedLine or a new formattedLine as
	 * necessary the calling function should have a continue statement after
	 * calling this method
	 *
	 * @param bracketType
	 *            the type of bracket to be formatted, must be an ARRAY_TYPE.
	 * @param isOpeningArrayBracket
	 *            indicates if this is the opening bracket for the array block.
	 */
	private void formatArrayBrackets(int bracketType, boolean isOpeningArrayBracket)
	{
		assert (isBracketType(bracketType, BracketType.ARRAY_TYPE));
		assert (currentChar == '{' || currentChar == '}');

		if (currentChar == '{')
		{
			// is this the first opening bracket in the array?
			if (isOpeningArrayBracket)
			{
				if (bracketFormatMode == BracketMode.ATTACH_MODE
						|| bracketFormatMode == BracketMode.LINUX_MODE
						|| bracketFormatMode == BracketMode.STROUSTRUP_MODE)
				{
					// don't attach to a preprocessor directive
					if (isImmediatelyPostPreprocessor && lineBeginsWith('{'))
					{
						isInLineBreak = true;
						appendCurrentChar(); // don't attach
					}
					// are there comments before the bracket?
					else if (isCharImmediatelyPostComment
							|| isCharImmediatelyPostLineComment)
					{
						appendCharInsideComments();
					} else
					{
						// if a blank line preceeds this don't attach
						int firstChar = ASUtils.findFirstNotOf(formattedLine,
								ASUtils.WHITE_SPACE, 0);
						if (firstChar == -1)
							appendCurrentChar(); // don't attach
						else
						{
							// if bracket is broken or not an assignment
							if (lineBeginsWith('{') || previousNonWSChar != '=')
								appendSpacePad();
							appendCurrentChar(false); // OK to attach
						}
					}
				} else if (bracketFormatMode == BracketMode.BREAK_MODE)
				{
					if (isWhiteSpace(peekNextChar()))
						breakLine();
					else if (isBeforeComment())
					{
						// do not break unless comment is at line end
						if (isBeforeLineEndComment(charNum))
						{
							currentChar = ' '; // remove bracket from current
							// line
							appendOpeningBracket = true; // append bracket to
							// following line
						}
					}
					if (!isInLineBreak && previousNonWSChar != '=')
						appendSpacePad();
					appendCurrentChar();
				} else if (bracketFormatMode == BracketMode.NONE_MODE)
				{
					if (lineBeginsWith('{')) // is opening bracket broken?
					{
						appendCurrentChar(); // don't attach
					} else
					{
						// if bracket is broken or not an assignment
						if (lineBeginsWith('{') || previousNonWSChar != '=')
							appendSpacePad();
						appendCurrentChar(false); // OK to attach
					}
				}
			} else
				appendCurrentChar(); // not the first opening bracket - don't
			// change

			// if an opening bracket ends the line there will be no inStatement
			// indent
			char nextChar = peekNextChar();
			if (isWhiteSpace(nextChar)
					|| isBeforeLineEndComment(charNum) || nextChar == '{')
				isNonInStatementArray = true;
			// Java "new Type [] {...}" IS an inStatement indent
			if (isJavaStyle() && previousNonWSChar == ']')
				isNonInStatementArray = false;

		} else if (currentChar == '}')
		{
			// does this close the first opening bracket in the array?
			if (isOpeningArrayBracket
					&& !isBracketType(bracketType, BracketType.SINGLE_LINE_TYPE))
			{
				breakLine();
				appendCurrentChar();
			} else
				appendCurrentChar();

			// if a declaration follows an enum definition, space pad
			if (isLegalNameChar(peekNextChar()))
				appendSpaceAfter();
		}
	}

	/**
	 * convert tabs to spaces. charNum points to the current character to
	 * convert to spaces. tabIncrementIn is the increment that must be added for
	 * tab indent characters to get the correct column for the current tab.
	 * replaces the tab in currentLine with the required number of spaces.
	 * replaces the value of currentChar.
	 */
	private void convertTabToSpaces()
	{
		assert (currentLine.charAt(charNum) == '\t');

		// do NOT replace if in quotes
		if (isInQuote || isInQuoteContinuation)
			return;

		int indent = getIndentLength();
		int numSpaces = indent - ((tabIncrementIn + charNum) % indent);
		String spaces = ASUtils.repeat(numSpaces, ' ');
		currentLine.replace(charNum, charNum + numSpaces, spaces);
		currentChar = currentLine.charAt(charNum);
	}

	/**
	 * check for a following header when a comment is reached. if a header
	 * follows, the comments are kept as part of the header block. firstLine
	 * must contain the start of the coment.
	 */
	private void checkForFollowingHeader(StringBuffer firstLine)
	{
		// look ahead to find the next non-comment text
		StringBuffer nextText = peekNextText(firstLine, true);
		if (nextText.length() == 0 || !isCharPotentialHeader(nextText, 0))
			return;

		String newHeader = findHeader(nextText, 0, headers);

		if (newHeader == null)
			return;

		// may need to break if a header follows
		boolean isClosingHeader = (newHeader.equals(ASResource.AS_ELSE)
				|| newHeader.equals(ASResource.AS_CATCH) || newHeader
				.equals(ASResource.AS_FINALLY));

		// if a closing header, reset break unless break is requested
		if (isClosingHeader)
		{
			if (!shouldBreakClosingHeaderBlocks)
				isPrependPostBlockEmptyLineRequested = false;
		}
		// if an opening header, break before the comment
		else
		{
			isPrependPostBlockEmptyLineRequested = true;
		}
	}

	/**
	 * process preprocessor statements. charNum should be the index of the
	 * preprocessor directive.
	 *
	 * delete bracketTypeStack entries added by #if if a #else is found.
	 * prevents double entries in the bracketTypeStack.
	 */
	private void processPreprocessor()
	{
		assert (currentLine.charAt(charNum) == '#');

		int preproc = charNum + 1;

		if (currentLine.indexOf("if", preproc)==preproc)
		{
			preprocBracketTypeStackSize = bracketTypeStack.size();
		} else if (currentLine.indexOf("else", preproc)==preproc)
		{
			// delete stack entries added in #if
			// should be replaced by #else
			int addedPreproc = bracketTypeStack.size()
					- preprocBracketTypeStackSize;
			if (addedPreproc > 0)
			{
				for (int i = 0; i < addedPreproc; i++)
					bracketTypeStack.pop();
			}
		}
	}

	/**
	 * determine if the next line starts a comment and a header follows the
	 * comment or comments
	 *
	 * @throws IOException
	 */
	private boolean commentAndHeaderFollows()
	{
		// is the next line a comment
		StringBuffer nextLine = sourceIterator.peekNextLine();
		int firstChar = ASUtils.findFirstNotOf(nextLine, ASUtils.WHITE_SPACE, 0);
		if (firstChar == -1
				|| !(nextLine.indexOf("//", firstChar)==firstChar || nextLine
						.indexOf("/*", firstChar)==firstChar))
		{
			sourceIterator.peekReset();
			return false;
		}

		// if next line is a comment, find the next non-comment text
		// peekNextText will do the peekReset
		StringBuffer nextText = peekNextText(nextLine, true);
		if (nextText.length() == 0 || !isCharPotentialHeader(nextText, 0))
			return false;

		String newHeader = findHeader(nextText, 0, headers);

		if (newHeader == null)
			return false;

		boolean isClosingHeader = (newHeader.equals(ASResource.AS_ELSE)
				|| newHeader.equals(ASResource.AS_CATCH) || newHeader
				.equals(ASResource.AS_FINALLY));

		if (isClosingHeader && !shouldBreakClosingHeaderBlocks)
			return false;

		return true;
	}

	// call ASBase::findHeader for the current character
	private String findHeader(List<String> headers)
	{
		return findHeader(currentLine, charNum, headers);
	}

	// call ASBase::findOperator for the current character
	private String findOperator(List<String> headers)
	{
		return findOperator(currentLine.toString(), charNum, headers);
	}

	// append a character to the current formatted line.
	private void appendChar(char ch, boolean canBreakLine)
	{
		if (canBreakLine && isInLineBreak)
			breakLine();
		formattedLine.append(ch);
		isImmediatelyPostCommentOnly = false;
	}

	// append the CURRENT character (curentChar) to the current formatted line.
	private void appendCurrentChar()
	{
		appendChar(currentChar, false);
	}

	// append the CURRENT character (curentChar) to the current formatted line.
	private void appendCurrentChar(boolean canBreakLine)
	{
		appendChar(currentChar, canBreakLine);
	}

	// check if a specific sequence exists in the current placement of the
	// current line
	private boolean isSequenceReached(String sequence)
	{
		return currentLine.indexOf(sequence,charNum)==charNum;
	}

}
