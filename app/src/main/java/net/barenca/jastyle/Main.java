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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.barenca.jastyle.ASFormatter.BracketMode;

public class Main
{
	private final static String TEMP_SUFFIX = ".tmp";
	private final static String JASTYLE_VERSION = findVersion("version.txt");
	private boolean modeManuallySet = false; // file mode is set by an option

	private final static void printVersion()
	{
		System.out.println("\n                                 jAstyle "
				+ JASTYLE_VERSION);
		System.out.println("                         Maintained by: Hector Suarez Barenca\n");
	}

	private final static void printHelp()
	{
		printVersion();
		printText("help.txt", new PrintWriter(System.out));

	}

	private final static String findVersion(String filename)
	{
		StringWriter writer = new StringWriter();
		printText(filename, new PrintWriter(writer));
		return writer.toString();
	}

	private final static void printText(String filename, PrintWriter out)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				Main.class.getResourceAsStream(filename)));
		String line;
		try
		{
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					out.println(line);
				}
			} while (line != null);
			out.flush();
			reader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void isOptionError(final String arg, final String errorInfo)
	{
		if (errorInfo.length() > 0) // to avoid a compiler warning
		{
			System.out.println("Error in param: " + arg);
		}

	}

	private boolean isParamOption(final String arg, final String option)
	{
		boolean retVal = arg.startsWith(option);
		// if comparing for short option, 2nd char of arg must be numeric
		if (retVal && option.length() == 1 && arg.length() > 1
				&& !Character.isDigit(arg.charAt(1)))
		{
			retVal = false;
		}
		return retVal;
	}

	private boolean isParamOption(final String arg, final String option1,
			final String option2)
	{
		return isParamOption(arg, option1) || isParamOption(arg, option2);
	}

	private boolean parseOption(ASFormatter formatter, final String arg,
			String errorInfo)
	{
		if (arg.equals("style=allman") || arg.equals("style=ansi")
				|| arg.equals("style=bsd"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_ALLMAN);
		} else if (arg.equals("style=java"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_JAVA);
		} else if (arg.equals("style=k&r") || arg.equals("style=k/r"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_KandR);
		} else if (arg.equals("style=stroustrup"))
		{
			formatter
					.setFormattingStyle(ASFormatter.FormatStyle.STYLE_STROUSTRUP);
		} else if (arg.equals("style=whitesmith"))
		{
			formatter
					.setFormattingStyle(ASFormatter.FormatStyle.STYLE_WHITESMITH);
		} else if (arg.equals("style=banner"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_BANNER);
		} else if (arg.equals("style=gnu"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_GNU);
		} else if (arg.equals("style=linux"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_LINUX);
		} else if (isParamOption(arg, "A"))
		{
			int style = 0;
			final String styleParam = arg.substring("A".length());
			if (styleParam.length() > 0)
			{
				style = Integer.parseInt(styleParam);
			}
			if (style < 1 || style > 8)
			{
				isOptionError(arg, errorInfo);
			} else if (style == 1)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_ALLMAN);
			} else if (style == 2)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_JAVA);
			} else if (style == 3)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_KandR);
			} else if (style == 4)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_STROUSTRUP);
			} else if (style == 5)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_WHITESMITH);
			} else if (style == 6)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_BANNER);
			} else if (style == 7)
			{
				formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_GNU);
			} else if (style == 8)
			{
				formatter
						.setFormattingStyle(ASFormatter.FormatStyle.STYLE_LINUX);
			}
		}
		// must check for mode=cs before mode=c !!!
		else if (arg.equals("mode=cs"))
		{
			formatter.setSharpStyle();
			modeManuallySet = true;
		} else if (arg.equals("mode=c"))
		{
			formatter.setCStyle();
			modeManuallySet = true;
		} else if (arg.equals("mode=java"))
		{
			formatter.setJavaStyle();
			modeManuallySet = true;
		} else if (isParamOption(arg, "t", "indent=tab="))
		{
			int spaceNum = 4;

			final String spaceNumParam = isParamOption(arg, "t") ? arg
					.substring("t".length()) : arg.substring("indent=tab="
					.length());
			if (spaceNumParam.length() > 0)
			{
				spaceNum = Integer.parseInt(spaceNumParam);
			}
			if (spaceNum < 1 || spaceNum > 20)
			{
				isOptionError(arg, errorInfo);
			} else
			{
				formatter.setTabIndentation(spaceNum, false);
			}
		} else if (arg.equals("indent=tab"))
		{
			formatter.setTabIndentation(4);
		} else if (isParamOption(arg, "T", "indent=force-tab="))
		{
			int spaceNum = 4;
			final String spaceNumParam = isParamOption(arg, "T") ? arg
					.substring("T".length()) : arg
					.substring("indent=force-tab=".length());
			if (spaceNumParam.length() > 0)
			{
				spaceNum = Integer.parseInt(spaceNumParam);
			}
			if (spaceNum < 1 || spaceNum > 20)
			{
				isOptionError(arg, errorInfo);
			} else
			{
				formatter.setTabIndentation(spaceNum, true);
			}
		} else if (arg.equals("indent=force-tab"))
		{
			formatter.setTabIndentation(4, true);
		} else if (isParamOption(arg, "s", "indent=spaces="))
		{
			int spaceNum = 4;
			final String spaceNumParam = isParamOption(arg, "s") ? arg
					.substring("s".length()) : arg.substring("indent=spaces="
					.length());
			if (spaceNumParam.length() > 0)
			{
				spaceNum = Integer.parseInt(spaceNumParam);
			}
			if (spaceNum < 1 || spaceNum > 20)
			{
				isOptionError(arg, errorInfo);
			} else
			{
				formatter.setSpaceIndentation(spaceNum);
			}
		} else if (arg.equals("indent=spaces"))
		{
			formatter.setSpaceIndentation(4);
		} else if (isParamOption(arg, "m", "min-conditional-indent="))
		{
			int minIndent = 8;
			final String minIndentParam = isParamOption(arg, "m") ? arg
					.substring("m".length()) : arg
					.substring("min-conditional-indent=".length());
			if (minIndentParam.length() > 0)
			{
				minIndent = Integer.parseInt(minIndentParam);
			}
			if (minIndent > 40)
			{
				isOptionError(arg, errorInfo);
			} else
			{
				formatter.setMinConditionalIndentLength(minIndent);
			}
		} else if (isParamOption(arg, "M", "max-instatement-indent="))
		{
			int maxIndent = 40;
			final String maxIndentParam = isParamOption(arg, "M") ? arg
					.substring("M".length()) : arg
					.substring("max-instatement-indent=".length());
			if (maxIndentParam.length() > 0)
			{
				maxIndent = Integer.parseInt(maxIndentParam);
			}
			if (maxIndent > 80)
			{
				isOptionError(arg, errorInfo);
			} else
			{
				formatter.setMaxInStatementIndentLength(maxIndent);
			}
		} else if (arg.equals("B") || arg.equals("indent-brackets"))
		{
			formatter.setBracketIndent(true);
		} else if (arg.equals("G") || arg.equals("indent-blocks"))
		{
			formatter.setBlockIndent(true);
		} else if (arg.equals("N") || arg.equals("indent-namespaces"))
		{
			formatter.setNamespaceIndent(true);
		} else if (arg.equals("C") || arg.equals("indent-classes"))
		{
			formatter.setClassIndent(true);
		} else if (arg.equals("S") || arg.equals("indent-switches"))
		{
			formatter.setSwitchIndent(true);
		} else if (arg.equals("K") || arg.equals("indent-cases"))
		{
			formatter.setCaseIndent(true);
		} else if (arg.equals("L") || arg.equals("indent-labels"))
		{
			formatter.setLabelIndent(true);
		} else if (arg.equals("y") || arg.equals("break-closing-brackets"))
		{
			formatter.setBreakClosingHeaderBracketsMode(true);
		} else if (arg.equals("b") || arg.equals("brackets=break"))
		{
			formatter.setBracketFormatMode(BracketMode.BREAK_MODE);
		} else if (arg.equals("a") || arg.equals("brackets=attach"))
		{
			formatter.setBracketFormatMode(BracketMode.ATTACH_MODE);
		} else if (arg.equals("l") || arg.equals("brackets=linux"))
		{
			formatter.setBracketFormatMode(BracketMode.LINUX_MODE);
		} else if (arg.equals("u") || arg.equals("brackets=stroustrup"))
		{
			formatter.setBracketFormatMode(BracketMode.STROUSTRUP_MODE);
		} else if (arg.equals("O") || arg.equals("keep-one-line-blocks"))
		{
			formatter.setBreakOneLineBlocksMode(false);
		} else if (arg.equals("o") || arg.equals("keep-one-line-statements"))
		{
			formatter.setSingleStatementsMode(false);
		} else if (arg.equals("P") || arg.equals("pad-paren"))
		{
			formatter.setParensOutsidePaddingMode(true);
			formatter.setParensInsidePaddingMode(true);
		} else if (arg.equals("d") || arg.equals("pad-paren-out"))
		{
			formatter.setParensOutsidePaddingMode(true);
		} else if (arg.equals("D") || arg.equals("pad-paren-in"))
		{
			formatter.setParensInsidePaddingMode(true);
		} else if (arg.equals("U") || arg.equals("unpad-paren"))
		{
			formatter.setParensUnPaddingMode(true);
		} else if (arg.equals("p") || arg.equals("pad-oper"))
		{
			formatter.setOperatorPaddingMode(true);
		} else if (arg.equals("E") || arg.equals("fill-empty-lines"))
		{
			formatter.setEmptyLineFill(true);
		} else if (arg.equals("w") || arg.equals("indent-preprocessor"))
		{
			formatter.setPreprocessorIndent(true);
		} else if (arg.equals("c") || arg.equals("convert-tabs"))
		{
			formatter.setTabSpaceConversionMode(true);
		} else if (arg.equals("F") || arg.equals("break-blocks=all"))
		{
			formatter.setBreakBlocksMode(true);
			formatter.setBreakClosingHeaderBlocksMode(true);
		} else if (arg.equals("f") || arg.equals("break-blocks"))
		{
			formatter.setBreakBlocksMode(true);
		} else if (arg.equals("e") || arg.equals("break-elseifs"))
		{
			formatter.setBreakElseIfsMode(true);
		} else if (arg.equals("x") || arg.equals("delete-empty-lines"))
		{
			formatter.setDeleteEmptyLinesMode(true);
		}
		// depreciated options
		// /////////////////////////////////////////////////////////////////////////////////////
		// depreciated in release 1.22 - may be removed at an appropriate time
		else if (arg.equals("style=kr"))
		{
			formatter.setFormattingStyle(ASFormatter.FormatStyle.STYLE_JAVA);
		} else if (isParamOption(arg, "T", "force-indent=tab="))
		{
			// the 'T' option will already have been processed
			int spaceNum = 4;
			final String spaceNumParam = isParamOption(arg, "T") ? arg
					.substring("T".length()) : arg
					.substring("force-indent=tab=".length());
			if (spaceNumParam.length() > 0)
			{
				spaceNum = Integer.parseInt(spaceNumParam);
			}
			if (spaceNum < 1 || spaceNum > 20)
			{
				isOptionError(arg, errorInfo);
			} else
			{
				formatter.setTabIndentation(spaceNum, true);
			}
		} else if (arg.equals("brackets=break-closing"))
		{
			formatter.setBreakClosingHeaderBracketsMode(true);
		}

		else if (arg.equals("one-line=keep-blocks"))
		{
			formatter.setBreakOneLineBlocksMode(false);
		} else if (arg.equals("one-line=keep-statements"))
		{
			formatter.setSingleStatementsMode(false);
		} else if (arg.equals("pad=paren"))
		{
			formatter.setParensOutsidePaddingMode(true);
			formatter.setParensInsidePaddingMode(true);
		} else if (arg.equals("pad=paren-out"))
		{
			formatter.setParensOutsidePaddingMode(true);
		} else if (arg.equals("pad=paren-in"))
		{
			formatter.setParensInsidePaddingMode(true);
		} else if (arg.equals("unpad=paren"))
		{
			formatter.setParensUnPaddingMode(true);
		} else if (arg.equals("pad=oper"))
		{
			formatter.setOperatorPaddingMode(true);
		}
		//
		// // Options used by only console
		// else if ( arg.equals("n") || arg.equals("suffix=none") )
		// {
		// g_console.noBackup = true;
		// }
		// else if ( isParamOption(arg, "suffix=") )
		// {
		// String suffixParam = arg.substring("suffix=".length());
		// if (suffixParam.length() > 0)
		// {
		// g_console.origSuffix = suffixParam;
		// }
		// }
		// else if ( isParamOption(arg, "exclude=") )
		// {
		// String suffixParam = arg.substring("exclude=".length());
		// if (suffixParam.length() > 0)
		// {
		// g_console.excludeVector.push_back(suffixParam);
		// g_console.excludeHitsVector.push_back(false);
		// }
		// }
		// else if ( arg.equalsIgnoreCase("r") || arg.equals("recursive") )
		// {
		// g_console.isRecursive = true;
		// }
		// else if ( arg.equals("Z") || arg.equals("preserve-date") )
		// {
		// g_console.preserveDate = true;
		// }
		// else if ( arg.equals("v") || arg.equals("verbose") )
		// {
		// g_console.isVerbose = true;
		// }
		// else if ( arg.equals("Q") || arg.equals("formatted") )
		// {
		// g_console.isFormattedOnly = true;
		// }
		// else if ( arg.equals("q") || arg.equals("quiet") )
		// {
		// g_console.isQuiet = true;
		// }
		// else if ( arg.equals("X") || arg.equals("errors-to-stdout") )
		// {
		// _err = cout;
		// }
		else
		{
			System.err.println(errorInfo + arg);
			return false; // invalid option
		}
		// End of parseOption function
		return true; // o.k.
	}

	/**
	 * parse the options vector ITER can be either a fileOptionsVector (options
	 * file) or an optionsVector (command line)
	 *
	 * @return true if no errors, false if errors
	 */
	boolean parseOptions(ASFormatter formatter, List<String> options,
			final String errorInfo)
	{
		boolean ok = true;

		for (String arg : options)
		{
			if (arg.startsWith("--"))
			{
				ok &= parseOption(formatter, arg.substring(2), errorInfo);
			} else if (arg.charAt(0) == '-')
			{
				StringBuffer subArg = new StringBuffer();
				for (int i = 1; i < arg.length(); ++i)
				{
					if (Character.isLetter(arg.charAt(i)) && i > 1)
					{
						ok &= parseOption(formatter, subArg.toString(),
								errorInfo);
						subArg.delete(0, subArg.length());
					}
					subArg.append(arg.charAt(i));
				}
				ok &= parseOption(formatter, subArg.toString(), errorInfo);
			} else
			{
				ok &= parseOption(formatter, arg, errorInfo);
			}
		}
		return ok;
	}

	public void error(final String why, String what)
	{
		System.out.println(why + ' ' + what + '\n'
				+ "JArtistic Style has terminated!");
		System.exit(1);
	}

	/**
	 * Open input file, format it, and close the output.
	 *
	 * @param fileName
	 *            The path and name of the file to be processed.
	 * @param formatter
	 *            The formatter object.
	 * @return true if the file was formatted, false if it was not (no changes).
	 */
	public void formatFile(String fileName, ASFormatter formatter)
			throws IOException
	{
		// open input file
		Reader in = new BufferedReader(new FileReader(fileName));

		// open tmp file
		String tmpFileName = fileName + TEMP_SUFFIX;
		File file = new File(tmpFileName);
		// remove the pre-existing temp file, if present
		file.delete();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				tmpFileName)));

		// Unless a specific language mode has been set, set the language mode
		// according to the file's suffix.
		if (!modeManuallySet)
		{
			if (fileName.endsWith(".java"))
			{
				formatter.setJavaStyle();
			} else if (fileName.endsWith(".cs"))
			{
				formatter.setSharpStyle();
			} else
			{
				formatter.setCStyle();
			}
		}

		ASStreamIterator streamIterator = new ASStreamIterator(in);
		formatter.init(streamIterator);

		// format the file
		while (formatter.hasMoreLines())
		{
			out.println(formatter.nextLine().toString());
			// if (formatter.hasMoreLines())
			// out.print(streamIterator.getOutputEOL());

		}
		out.flush();
		out.close();
		in.close();
		new File(fileName).renameTo(new File(fileName + ".orig"));
		new File(tmpFileName).renameTo(new File(fileName));
	}

	// /**
	// * LINUX function to resolve wildcards and recurse into sub directories.
	// * The fileName vector is filled with the path and names of files to
	// process.
	// *
	// * @param directory The path of the directory to be processed.
	// * @param wildcard The wildcard to be processed (e.g. *.cpp).
	// * @param fileName An empty vector which will be filled with the path and
	// names of files to process.
	// */
	// void getFileNames( String directory, String wildcard, List<String>
	// fileName)
	// {
	// dirent *entry; // entry from readdir()
	// stat statbuf; // entry from stat()
	// vector<String> subDirectory; // sub directories of this directory
	//
	// // errno is defined in <errno.h> and is set for errors in opendir,
	// readdir, or stat
	// errno = 0;
	//
	// DIR *dp = opendir(directory);
	// if (errno)
	// error("Cannot open directory", directory);
	//
	// // save the first fileName entry for this recursion
	// unsigned firstEntry = fileName.size();
	//
	// // save files and sub directories
	// while ((entry = readdir(dp)) != null)
	// {
	// // get file status
	// String entryFilepath = directory + FILE_SEPARATOR + entry->d_name;
	// stat(entryFilepath, statbuf);
	// if (errno)
	// {
	// if (errno == EOVERFLOW) // file over 2 GB is OK
	// {
	// errno = 0;
	// continue;
	// }
	// perror("errno message");
	// error("Error getting file status in directory", directory);
	// }
	// // skip hidden or read only
	// if (entry->d_name[0] == '.' || !(statbuf.st_mode & S_IWUSR))
	// continue;
	// // if a sub directory and recursive, save sub directory
	// if (S_ISDIR(statbuf.st_mode) && isRecursive)
	// {
	// if (isPathExclued(entryFilepath))
	// System.out.println("exclude " +
	// entryFilepath.substr(mainDirectoryLength));
	// else
	// subDirectory.push_back(entryFilepath);
	// continue;
	// }
	//
	// // if a file, save file name
	// if (S_ISREG(statbuf.st_mode))
	// {
	// // check exclude before wildcmp to avoid "unmatched exclude" error
	// boolean isExcluded = isPathExclued(entryFilepath);
	// // save file name if wildcard match
	// if (wildcmp(wildcard, entry->d_name))
	// {
	// if (isExcluded)
	// System.out.println("exclude " +
	// entryFilepath.substr(mainDirectoryLength));
	// else
	// fileName.push_back(entryFilepath);
	// }
	// }
	// }
	// closedir(dp);
	//
	// if (errno)
	// {
	// perror("errno message");
	// error("Error reading directory", directory);
	// }
	//
	// // sort the current entries for fileName
	// if (firstEntry < fileName.size())
	// sort(fileName[firstEntry], fileName[fileName.size()]);
	//
	// // recurse into sub directories
	// // if not doing recursive, subDirectory is empty
	// if (subDirectory.size() > 1)
	// sort(subDirectory.begin(), subDirectory.end());
	// for (unsigned i = 0; i < subDirectory.size(); i++)
	// {
	// getFileNames(subDirectory[i], wildcard, fileName);
	// }
	//
	// return;
	// }
	//
	//
	// void preserveFileDate(String oldFileName, String newFileName)
	// {
	// stat stBuf;
	// boolean statErr = false;
	// if (stat (oldFileName, stBuf) == -1)
	// statErr = true;
	// else
	// {
	// utimbuf outBuf;
	// outBuf.actime = stBuf.st_atime;
	// // add 2 so 'make' will recoginze a change
	// // Visual Studio 2008 needs 2
	// outBuf.modtime = stBuf.st_mtime + 2;
	// if (utime (newFileName, outBuf) == -1)
	// statErr = true;
	// }
	// if (statErr)
	// System.out.println("    Could not preserve file date");
	// }

	// // process a command-line file path, including wildcards
	// void processFilePath(String filePath, ASFormatter formatter)
	// {
	// vector<String> fileName; // files to be processed including path
	// String targetDirectory; // path to the directory being processed
	// String targetFilename; // file name being processed
	//
	// // standardize the file separators
	// standardizePath(filePath);
	//
	// // separate directory and file name
	// size_t separator = filePath.find_last_of(FILE_SEPARATOR);
	// if (separator == -1)
	// {
	// // if no directory is present, use the currently active directory
	// targetDirectory = getCurrentDirectory(filePath);
	// targetFilename = filePath;
	// mainDirectoryLength = targetDirectory.length() + 1; // +1 includes
	// trailing separator
	// }
	// else
	// {
	// targetDirectory = filePath.substr(0, separator);
	// targetFilename = filePath.substr(separator+1);
	// mainDirectoryLength = targetDirectory.length() + 1; // +1 includes
	// trailing separator
	// }
	//
	// if (targetFilename.length() == 0)
	// error("Missing filename in", filePath);
	//
	// // check filename for wildcards
	// hasWildcard = false;
	// if (targetFilename.find_first_of( "*?") != -1)
	// hasWildcard = true;
	//
	// // clear exclude hits vector
	// for (size_t ix = 0; ix < excludeHitsVector.size(); ix++)
	// excludeHitsVector[ix] = false;
	//
	// // display directory name for wildcard processing
	// if (hasWildcard && ! isQuiet)
	// {
	// System.out.println("--------------------------------------------------");
	// System.out.println("directory " + targetDirectory + FILE_SEPARATOR +
	// targetFilename;
	// }
	//
	// // create a vector of paths and file names to process
	// if (hasWildcard || isRecursive)
	// getFileNames(targetDirectory, targetFilename, fileName);
	// else
	// fileName.push_back(filePath);
	//
	// if (hasWildcard && ! isQuiet)
	// System.out.println("--------------------------------------------------");
	//
	// // check for unprocessed excludes
	// boolean excludeErr = false;
	// for (size_t ix = 0; ix < excludeHitsVector.size(); ix++)
	// {
	// if (excludeHitsVector[ix] == false)
	// {
	// System.out.println("Unmatched exclude " + excludeVector[ix]);
	// excludeErr = true;
	// }
	// }
	// if (excludeErr)
	// exit(EXIT_FAILURE);
	//
	// // check if files were found (probably an input error if not)
	// if (fileName.size() == 0)
	// System.out.println("No file to process " + filePath);
	//
	// // loop thru fileName vector to format the files
	// for (size_t j = 0; j < fileName.size(); j++)
	// {
	// // format the file
	// boolean isFormatted = formatFile(fileName[j], formatter);
	//
	// // remove targetDirectory from filename if required
	// String displayName;
	// if (hasWildcard)
	// displayName = fileName[j].substr(targetDirectory.length() + 1);
	// else
	// displayName = fileName[j];
	//
	// if (isFormatted)
	// {
	// filesFormatted++;
	// if (!isQuiet)
	// System.out.println("formatted  " + displayName);
	// }
	// else
	// {
	// filesUnchanged++;
	// if (!isQuiet && !isFormattedOnly)
	// System.out.println("unchanged* " + displayName);
	// }
	// }
	// }

	private final static String OPTIONS = "--options=";
	private final static int EXIT_SUCCESS = 0;
	private final static int EXIT_FAILURE = -1;

	/**
	 * Useful when options are in a String line (applets for example). This
	 * method suppose that options will not be loaded from options file.<br>
	 *
	 * @param args
	 *            options separated by spaces
	 * @param formatter
	 *            Formatter to be used
	 * @throws IOException
	 */
	public void processOptions(String args, ASFormatter formatter)
			throws IOException
	{
		processOptions(args.split("\\s+"), formatter, false);
	}

	//
	// , optionsVector, and fileOptionsVector

	/**
	 * Process options from the command line and options file, build the vectors
	 * fileNameVector and set the formatter properties
	 *
	 * @param args
	 *            String[] with the options and files that will be formatted
	 * @param formatter
	 *            The ASFormatter to use
	 * @throws IOException
	 */
	public List<String> processOptions(String[] args, ASFormatter formatter)
			throws IOException
	{
		return processOptions(args, formatter, true);
	}

	/**
	 * Process options from the command line and options file, build the vectors
	 * fileNameVector and set the formatter properties
	 *
	 * @param args
	 * @param formatter
	 * @param shouldParseOptionsFile
	 * @return List List with the filenames to be processed
	 * @throws IOException
	 */
	private List<String> processOptions(String args[], ASFormatter formatter,
			boolean shouldParseOptionsFile) throws IOException
	{
		boolean ok = true;

		String optionsFileName = null;

		modeManuallySet = false;
		List<String> optionsVector = new ArrayList<String>();
		List<String> fileNameVector = new ArrayList<String>();

		if (args != null && args.length > 0)

			// get command line options
			for (String arg : args)
			{
				if (arg.equals("--options=none"))
				{
					shouldParseOptionsFile = false;
				} else if (isParamOption(arg, OPTIONS))
				{
					optionsFileName = arg.substring(OPTIONS.length());
				} else if (arg.equals("-h") || arg.equals("--help")
						|| arg.equals("-?"))
				{
					printHelp();
					System.exit(EXIT_SUCCESS);
				} else if (arg.equals("-V") || arg.equals("--version"))
				{
					printVersion();
					System.exit(EXIT_SUCCESS);
				}

				else if (arg.charAt(0) == '-')
				{
					optionsVector.add(arg);
				} else
				// file-name
				{
					fileNameVector.add(arg);
				}
			}

		// get options file path and name
		if (shouldParseOptionsFile)
		{

			if (optionsFileName != null && optionsFileName.trim().length() > 0)
			{
				String env = System.getenv("ARTISTIC_STYLE_OPTIONS");
				if (env != null && env.trim().length() > 0)
					optionsFileName = env;
			}
			if (optionsFileName != null && optionsFileName.trim().length() > 0)
			{
				List<String> fileOptions = importOptions(optionsFileName);
				if (fileOptions != null && fileOptions.size() > 0)
				{
					ok = parseOptions(formatter, fileOptions,
							"Invalid option in default options file: ");
				}
			}

		}

		if (!ok)
		{
			System.out
					.println("For help on options, type 'java -jar jastyle.jar -h' ");
			System.exit(EXIT_FAILURE);
		}

		// parse the command line options vector for errors
		ok = parseOptions(formatter, optionsVector,
				"Invalid command line option: ");
		if (!ok)
		{
			System.out
					.println("For help on options, type 'java -jar jastyle.jar -h' \n");
			System.exit(EXIT_FAILURE);
		}
		return fileNameVector;
	}

	// // rename a file and check for an error
	// void renameFile(String oldFileName, String newFileName, String errMsg)
	// {
	// rename(oldFileName, newFileName);
	// // if file still exists the remove needs more time - retry
	// if (errno == EEXIST)
	// {
	// errno = 0;
	// waitForRemove(newFileName);
	// rename(oldFileName, newFileName);
	// }
	// if (errno)
	// {
	// perror("errno message");
	// error(errMsg, oldFileName);
	// }
	// }

	/**
	 * Read jAstyle options from a file <br>
	 * Read a file form the file system, it skips the lines that start with #<br>
	 *
	 * <pre>
	 * A default options file may be used to set your favorite source style options.
	 * The command line options have precedence. If there is a conflict between a command line option and an option in the default options file, the command line option will be used.
	 * Artistic Style looks for this file in the following locations (in order):
	 * the file indicated by the --options= command line option;
	 * the file and directory indicated by the environment variable ARTISTIC_STYLE_OPTIONS (if it exists);
	 * This option file lookup can be disabled by specifying --options=none on the command line.
	 * Options may be set apart by new-lines, tabs, commas, or spaces.
	 * Long options in the options file may be written without the preceding '--'.
	 * Lines within the options file that begin with '#' are considered line-comments.
	 * Example of a default options file:
	 * <em>
	 * # this line is a comment
	 * --brackets=attach   # this is a line-end comment
	 * # long options can be written without the preceding '--'
	 * indent-switches     # cannot do this on the command line
	 * # short options must have the preceding '-'
	 * -t -p
	 * # short options can be concatenated together
	 * -M65Ucv
	 * </em>
	 * </pre>
	 *
	 * @param filename
	 *            The name of the file that will be readed
	 * @return
	 * @throws IOException
	 */
	private final static List<String> importOptions(String filename)
			throws IOException
	{
		String line = null;
		List<String> fileOptions = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		do
		{
			line = reader.readLine();
			if (line != null)
			{
				String cleanLine = line.trim();
				if (cleanLine.charAt(0) != '#' && cleanLine.length() > 0)
				{
					fileOptions.add(cleanLine);
				}
			}
		} while (line != null);
		reader.close();
		return fileOptions;
	}

	/**
	 * Not supported options:<br>
	 *
	 * <pre>
	 * --suffix=####
	 * --suffix=none / -n
	 * --recursive / -r / -R
	 * --exclude=####
	 * --errors-to-stdout / -X
	 * --preserve-date / -Z
	 * --verbose / -v
	 * --formatted / -Q
	 * --quiet / -q
	 * </pre>
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
		{
			printHelp();
			System.exit(EXIT_SUCCESS);
		}
		String fileSeparator = System.getProperty("file.separator");
		String dir;
		String filename;
		Main console = new Main();
		ASFormatter formatter = new ASFormatter();
		List<String> filenames = console.processOptions(args, formatter);
		for (String filepath : filenames)
		{
			int index = filepath.lastIndexOf(fileSeparator);
			if (index < 0)
			{
				dir = ".";
				filename = filepath;
			} else
			{
				if (filepath.length() <= index)
				{
					System.err.println("The filename " + filepath
							+ " is invalid");
					System.exit(EXIT_FAILURE);
				}
				dir = filepath.substring(0, index);
				filename = filepath.substring(index + 1);
			}

			File file;
			File[] files;
			if (filename.indexOf('*') != -1 || filename.indexOf('?') != -1)
			{
				file = new File(dir);
				files = file.listFiles(new FileWildcardFilter(filename));
			} else
			{
				files = new File[] { new File(dir, filename) };
			}
			if (files != null)
			{
				for (File currFile : files)
				{
					System.out.println("Converting "
							+ currFile.getAbsolutePath() + " ...\n");
					Reader reader = new BufferedReader(new FileReader(currFile
							.getAbsolutePath()));

					console.formatFile(currFile.getAbsolutePath(), formatter);
					reader.close();

				}
			}
		}

	}
}
