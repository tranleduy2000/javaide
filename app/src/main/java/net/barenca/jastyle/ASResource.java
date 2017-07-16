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

import java.util.Collections;
import java.util.List;

import net.barenca.jastyle.constants.FileType;

public class ASResource
{
	public final static String AS_IF = "if";
	public final static String AS_ELSE = "else";
	public final static String AS_FOR = "for";
	public final static String AS_DO = "do";
	public final static String AS_WHILE = "while";
	public final static String AS_SWITCH = "switch";
	public final static String AS_CASE = "case";
	public final static String AS_DEFAULT = "default";
	public final static String AS_CLASS = "class";
	public final static String AS_STRUCT = "struct";
	public final static String AS_UNION = "union";
	public final static String AS_INTERFACE = "interface";
	public final static String AS_NAMESPACE = "namespace";
	// public final static String AS_EXTERN = "extern";
	// public final static String AS_PUBLIC = "public";
	// public final static String AS_PROTECTED = "protected";
	// public final static String AS_PRIVATE = "private";
	public final static String AS_STATIC = "static";
	public final static String AS_SYNCHRONIZED = "synchronized";
	public final static String AS_OPERATOR = "operator";
	public final static String AS_TEMPLATE = "template";
	public final static String AS_TRY = "try";
	public final static String AS_CATCH = "catch";
	public final static String AS_FINALLY = "finally";
	public final static String AS_THROWS = "throws";
	public final static String AS_CONST = "";
	public final static String AS_WHERE = "where";
	public final static String AS_NEW = "new";

	// public final static String AS_ASM = "asm";

	// public final static String AS_BAR_DEFINE = "#define";
	// public final static String AS_BAR_INCLUDE = "#include";
	// public final static String AS_BAR_IF = "#if";
	public final static String AS_BAR_EL = "#el";
	public final static String AS_BAR_ENDIF = "#endif";

	public final static String AS_OPEN_BRACKET = "{";
	public final static String AS_CLOSE_BRACKET = "}";
	public final static String AS_OPEN_LINE_COMMENT = "//";
	public final static String AS_OPEN_COMMENT = "/*";
	public final static String AS_CLOSE_COMMENT = "*/";

	public final static String AS_ASSIGN = "=";
	public final static String AS_PLUS_ASSIGN = "+=";
	public final static String AS_MINUS_ASSIGN = "-=";
	public final static String AS_MULT_ASSIGN = "*=";
	public final static String AS_DIV_ASSIGN = "/=";
	public final static String AS_MOD_ASSIGN = "%=";
	public final static String AS_OR_ASSIGN = "|=";
	public final static String AS_AND_ASSIGN = "&=";
	public final static String AS_XOR_ASSIGN = "^=";
	public final static String AS_GR_GR_ASSIGN = ">>=";
	public final static String AS_LS_LS_ASSIGN = "<<=";
	public final static String AS_GR_GR_GR_ASSIGN = ">>>=";
	public final static String AS_LS_LS_LS_ASSIGN = "<<<=";
	public final static String AS_GCC_MIN_ASSIGN = "<?";
	public final static String AS_GCC_MAX_ASSIGN = ">?";

	public final static String AS_RETURN = "return";
	public final static String AS_CIN = "cin";
	public final static String AS_COUT = "cout";
	public final static String AS_CERR = "cerr";

	public final static String AS_EQUAL = "==";
	public final static String AS_PLUS_PLUS = "++";
	public final static String AS_MINUS_MINUS = "--";
	public final static String AS_NOT_EQUAL = "!=";
	public final static String AS_GR_EQUAL = ">=";
	public final static String AS_GR_GR = ">>";
	public final static String AS_GR_GR_GR = ">>>";
	public final static String AS_LS_EQUAL = "<=";
	public final static String AS_LS_LS = "<<";
	public final static String AS_LS_LS_LS = "<<<";
	public final static String AS_QUESTION_QUESTION = "??";
	public final static String AS_EQUAL_GR = "=>"; // C# lambda expression arrow
	public final static String AS_ARROW = "->";
	public final static String AS_AND = "&&";
	public final static String AS_OR = "||";
	public final static String AS_COLON_COLON = "::";
	public final static String AS_PAREN_PAREN = "()";
	public final static String AS_BLPAREN_BLPAREN = "[]";

	public final static String AS_PLUS = "+";
	public final static String AS_MINUS = "-";
	public final static String AS_MULT = "*";
	public final static String AS_DIV = "/";
	public final static String AS_MOD = "%";
	public final static String AS_GR = ">";
	public final static String AS_LS = "<";
	public final static String AS_NOT = "!";
	public final static String AS_BIT_OR = "|";
	public final static String AS_BIT_AND = "&";
	public final static String AS_BIT_NOT = "~";
	public final static String AS_BIT_XOR = "^";
	public final static String AS_QUESTION = "?";
	public final static String AS_COLON = ":";
	public final static String AS_COMMA = ",";
	public final static String AS_SEMICOLON = ";";

	public final static String AS_FOREACH = "foreach";
	public final static String AS_LOCK = "lock";
	public final static String AS_UNSAFE = "unsafe";
	public final static String AS_FIXED = "fixed";
	public final static String AS_GET = "get";
	public final static String AS_SET = "set";
	public final static String AS_ADD = "add";
	public final static String AS_REMOVE = "remove";

	public final static String AS_CONST_CAST = "const_cast";
	public final static String AS_DYNAMIC_CAST = "dynamic_cast";
	public final static String AS_REINTERPRET_CAST = "reinterpret_cast";
	public final static String AS_STATIC_CAST = "static_cast";

	/**
	 * Build the vector of assignment operators. Used by BOTH ASFormatter.cpp
	 * and ASBeautifier.cpp
	 *
	 * @param assignmentOperators
	 *            a reference to the vector to be built.
	 */
	public static void buildAssignmentOperators(
			List<String> assignmentOperators)
	{
		assignmentOperators.add(AS_ASSIGN);
		assignmentOperators.add(AS_PLUS_ASSIGN);
		assignmentOperators.add(AS_MINUS_ASSIGN);
		assignmentOperators.add(AS_MULT_ASSIGN);
		assignmentOperators.add(AS_DIV_ASSIGN);
		assignmentOperators.add(AS_MOD_ASSIGN);
		assignmentOperators.add(AS_OR_ASSIGN);
		assignmentOperators.add(AS_AND_ASSIGN);
		assignmentOperators.add(AS_XOR_ASSIGN);

		// Java
		assignmentOperators.add(AS_GR_GR_GR_ASSIGN);
		assignmentOperators.add(AS_GR_GR_ASSIGN);
		assignmentOperators.add(AS_LS_LS_ASSIGN);

		// Unknown
		assignmentOperators.add(AS_LS_LS_LS_ASSIGN);

		Collections.sort(assignmentOperators, new SortOnLength());
	}

	/**
	 * Build the vector of C++ cast operators. Used by ONLY ASFormatter.cpp
	 *
	 * @param castOperators
	 *            a reference to the vector to be built.
	 */
	public static void buildCastOperators(List<String> castOperators)
	{
		castOperators.add(AS_CONST_CAST);
		castOperators.add(AS_DYNAMIC_CAST);
		castOperators.add(AS_REINTERPRET_CAST);
		castOperators.add(AS_STATIC_CAST);
	}

	/**
	 * Build the vector of header words. Used by BOTH ASFormatter.cpp and
	 * ASBeautifier.cpp
	 *
	 * @param headers
	 *            a reference to the vector to be built.
	 */
	public static void buildHeaders(List<String> headers, int fileType,
			boolean beautifier)
	{
		headers.add(AS_IF);
		headers.add(AS_ELSE);
		headers.add(AS_FOR);
		headers.add(AS_WHILE);
		headers.add(AS_DO);
		headers.add(AS_SWITCH);
		headers.add(AS_TRY);
		headers.add(AS_CATCH);

		if (fileType == FileType.JAVA_TYPE)
		{
			headers.add(AS_FINALLY);
			headers.add(AS_SYNCHRONIZED);
		}

		if (fileType == FileType.SHARP_TYPE)
		{
			headers.add(AS_FINALLY);
			headers.add(AS_FOREACH);
			headers.add(AS_LOCK);
			// headers.add(AS_UNSAFE);
			headers.add(AS_FIXED);
			headers.add(AS_GET);
			headers.add(AS_SET);
			headers.add(AS_ADD);
			headers.add(AS_REMOVE);
		}

		if (beautifier)
		{
			headers.add(AS_CASE);
			headers.add(AS_DEFAULT);

			if (fileType == FileType.C_TYPE)
			{
				headers.add(AS_CONST);
				headers.add(AS_TEMPLATE);
			}

			if (fileType == FileType.JAVA_TYPE)
			{
				headers.add(AS_STATIC); // for static constructor
			}
		}
		Collections.sort(headers, new SortOnName());
	}

	/**
	 * Build the vector of indentable headers. Used by ONLY ASBeautifier.cpp
	 *
	 * @param indentableHeaders
	 *            a reference to the vector to be built.
	 */
	public static void buildIndentableHeaders(
			List<String> indentableHeaders)
	{
		indentableHeaders.add(AS_RETURN);
		indentableHeaders.add(AS_COUT);
		indentableHeaders.add(AS_CERR);
		indentableHeaders.add(AS_CIN);

		Collections.sort(indentableHeaders, new SortOnName());
	}

	/**
	 * Build the vector of non-assignment operators. Used by ONLY
	 * ASBeautifier.cpp
	 *
	 * @param nonAssignmentOperators
	 *            a reference to the vector to be built.
	 */
	public static void buildNonAssignmentOperators(
			List<String> nonAssignmentOperators)
	{
		nonAssignmentOperators.add(AS_EQUAL);
		nonAssignmentOperators.add(AS_PLUS_PLUS);
		nonAssignmentOperators.add(AS_MINUS_MINUS);
		nonAssignmentOperators.add(AS_NOT_EQUAL);
		nonAssignmentOperators.add(AS_GR_EQUAL);
		nonAssignmentOperators.add(AS_GR_GR_GR);
		nonAssignmentOperators.add(AS_GR_GR);
		nonAssignmentOperators.add(AS_LS_EQUAL);
		nonAssignmentOperators.add(AS_LS_LS_LS);
		nonAssignmentOperators.add(AS_LS_LS);
		nonAssignmentOperators.add(AS_ARROW);
		nonAssignmentOperators.add(AS_AND);
		nonAssignmentOperators.add(AS_OR);

		Collections.sort(nonAssignmentOperators, new SortOnLength());
	}

	/**
	 * Build the vector of header non-paren headers. Used by BOTH
	 * ASFormatter.cpp and ASBeautifier.cpp
	 *
	 * @param nonParenHeaders
	 *            a reference to the vector to be built.
	 */
	public static void buildNonParenHeaders(List<String> nonParenHeaders,
			int fileType, boolean beautifier)
	{
		nonParenHeaders.add(AS_ELSE);
		nonParenHeaders.add(AS_DO);
		nonParenHeaders.add(AS_TRY);

		if (fileType == FileType.JAVA_TYPE)
		{
			nonParenHeaders.add(AS_FINALLY);
		}
		else
		if (fileType == FileType.SHARP_TYPE)
		{
			nonParenHeaders.add(AS_CATCH); // can be a paren or non-paren header
			nonParenHeaders.add(AS_FINALLY);
			// nonParenHeaders.add(AS_UNSAFE);
			nonParenHeaders.add(AS_GET);
			nonParenHeaders.add(AS_SET);
			nonParenHeaders.add(AS_ADD);
			nonParenHeaders.add(AS_REMOVE);
		}

		if (beautifier)
		{
			nonParenHeaders.add(AS_CASE);
			nonParenHeaders.add(AS_DEFAULT);
			if (fileType == FileType.C_TYPE)
			{
				nonParenHeaders.add(AS_CONST);
				nonParenHeaders.add(AS_TEMPLATE);
			}else
			if (fileType == FileType.JAVA_TYPE)
			{
				nonParenHeaders.add(AS_STATIC);
			}
		}
		Collections.sort(nonParenHeaders, new SortOnName());
	}

	/**
	 * Build the vector of operators. Used by ONLY ASFormatter.cpp
	 *
	 * @param operators
	 *            a reference to the vector to be built.
	 */
	public static void buildOperators(List<String> operators)
	{
		operators.add(AS_PLUS_ASSIGN);
		operators.add(AS_MINUS_ASSIGN);
		operators.add(AS_MULT_ASSIGN);
		operators.add(AS_DIV_ASSIGN);
		operators.add(AS_MOD_ASSIGN);
		operators.add(AS_OR_ASSIGN);
		operators.add(AS_AND_ASSIGN);
		operators.add(AS_XOR_ASSIGN);
		operators.add(AS_EQUAL);
		operators.add(AS_PLUS_PLUS);
		operators.add(AS_MINUS_MINUS);
		operators.add(AS_NOT_EQUAL);
		operators.add(AS_GR_EQUAL);
		operators.add(AS_GR_GR_GR_ASSIGN);
		operators.add(AS_GR_GR_ASSIGN);
		operators.add(AS_GR_GR_GR);
		operators.add(AS_GR_GR);
		operators.add(AS_LS_EQUAL);
		operators.add(AS_LS_LS_LS_ASSIGN);
		operators.add(AS_LS_LS_ASSIGN);
		operators.add(AS_LS_LS_LS);
		operators.add(AS_LS_LS);
		operators.add(AS_QUESTION_QUESTION);
		operators.add(AS_EQUAL_GR);
		operators.add(AS_GCC_MIN_ASSIGN);
		operators.add(AS_GCC_MAX_ASSIGN);
		operators.add(AS_ARROW);
		operators.add(AS_AND);
		operators.add(AS_OR);
		operators.add(AS_COLON_COLON);
		operators.add(AS_PLUS);
		operators.add(AS_MINUS);
		operators.add(AS_MULT);
		operators.add(AS_DIV);
		operators.add(AS_MOD);
		operators.add(AS_QUESTION);
		operators.add(AS_COLON);
		operators.add(AS_ASSIGN);
		operators.add(AS_LS);
		operators.add(AS_GR);
		operators.add(AS_NOT);
		operators.add(AS_BIT_OR);
		operators.add(AS_BIT_AND);
		operators.add(AS_BIT_NOT);
		operators.add(AS_BIT_XOR);

		Collections.sort(operators, new SortOnLength());
	}

	/**
	 * Build the vector of pre-block statements. Used by ONLY ASBeautifier.cpp
	 * NOTE: Cannot be both a header and a preBlockStatement.
	 *
	 * @param preBlockStatements
	 *            a reference to the vector to be built.
	 */
	public static void buildPreBlockStatements(
			List<String> preBlockStatements, int fileType)
	{
		preBlockStatements.add(AS_CLASS);
		if (fileType == FileType.C_TYPE)
		{
			preBlockStatements.add(AS_STRUCT);
			preBlockStatements.add(AS_UNION);
			preBlockStatements.add(AS_NAMESPACE);
		}
		else
		if (fileType == FileType.JAVA_TYPE)
		{
			preBlockStatements.add(AS_INTERFACE);
			preBlockStatements.add(AS_THROWS);
		}
		else
		if (fileType == FileType.SHARP_TYPE)
		{
			preBlockStatements.add(AS_INTERFACE);
			preBlockStatements.add(AS_NAMESPACE);
			preBlockStatements.add(AS_WHERE);
		}
		Collections.sort(preBlockStatements, new SortOnName());
	}

	/**
	 * Build the vector of pre-command headers. Used by ONLY ASFormatter.cpp
	 *
	 * @param preCommandHeaders
	 *            a reference to the vector to be built.
	 */
	public static void buildPreCommandHeaders(
			List<String> preCommandHeaders, int fileType)
	{
		if (fileType == FileType.C_TYPE)
		{
			preCommandHeaders.add(AS_CONST);
		}
		else
		if (fileType == FileType.JAVA_TYPE)
		{
			preCommandHeaders.add(AS_THROWS);
		}
		else
		if (fileType == FileType.SHARP_TYPE)
		{
			preCommandHeaders.add(AS_WHERE);
		}

		Collections.sort(preCommandHeaders, new SortOnName());
	}

	/**
	 * Build the vector of pre-definition headers. Used by ONLY ASFormatter.cpp
	 * NOTE: Do NOT add 'enum' here. It is an array type bracket. NOTE: Do NOT
	 * add 'extern' here. Do not want an extra indent.
	 *
	 * @param preDefinitionHeaders
	 *            a reference to the vector to be built.
	 */
	public static void buildPreDefinitionHeaders(
			List<String> preDefinitionHeaders, int fileType)
	{
		preDefinitionHeaders.add(AS_CLASS);
		if (fileType == FileType.C_TYPE)
		{
			preDefinitionHeaders.add(AS_STRUCT);
			preDefinitionHeaders.add(AS_UNION);
			preDefinitionHeaders.add(AS_NAMESPACE);
		}else
		if (fileType == FileType.JAVA_TYPE)
		{
			preDefinitionHeaders.add(AS_INTERFACE);
		}else
		if (fileType == FileType.SHARP_TYPE)
		{
			preDefinitionHeaders.add(AS_INTERFACE);
			preDefinitionHeaders.add(AS_NAMESPACE);
		}
		Collections.sort(preDefinitionHeaders, new SortOnName());
	}

}
