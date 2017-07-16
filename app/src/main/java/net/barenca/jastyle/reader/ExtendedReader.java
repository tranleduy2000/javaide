// Copyright 2004, 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.barenca.jastyle.reader;

import java.io.IOException;
import java.io.Reader;

/**
 * A Reader that provides some additional functionality, such as peek(). This is
 * a modified version of original ExtendedReader used in the Apache Tapestry Project.
 *
 * @author mb, barenca
 * @since 4.0
 */
public class ExtendedReader extends Reader
{
	private Reader _reader;
	private boolean _hasBufferedChar = false;
	private char _bufferedChar;
	private char _bufferedCharSaved;
	private boolean _hasBufferedCharSaved = false;

	/**
	 * Creates a new extended reader that reads from the provided object.
	 *
	 * @param in
	 *            the Reader to get data from
	 */
	public ExtendedReader(Reader in)
	{
		super(in);
		_reader = in;
	}

	/**
	 * Returns the next character in the stream without actually comitting the
	 * read. Multiple consequtive invocations of this method should return the
	 * same value.
	 *
	 * @return the next character waiting in the stream or -1 if the end of the
	 *         stream is reached
	 * @throws IOException
	 *             if an error occurs
	 */
	public synchronized int peek() throws IOException
	{
		if (!_hasBufferedChar)
		{
			int bufferedChar = read();
			if (bufferedChar < 0)
				return bufferedChar;
			_bufferedChar = (char) bufferedChar;
			_hasBufferedChar = true;
		}
		return _bufferedChar;
	}

	/**
	 * Determines whether the end of the stream is reached.
	 *
	 * @return true if at the end of stream
	 * @throws IOException
	 *             if an error occurs
	 */
	public synchronized boolean isEndOfStream() throws IOException
	{
		return peek() < 0;
	}

	/**
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	public synchronized int read(char[] cbuf, int off, int len)
			throws IOException
	{
		int offset = off;
		if (len <= 0)
			return 0;
		int readLength = len;

		boolean extraChar = _hasBufferedChar;
		if (_hasBufferedChar)
		{
			_hasBufferedChar = false;
			cbuf[offset++] = _bufferedChar;
			readLength--;
		}

		int read = _reader.read(cbuf, offset, readLength);
		if (extraChar)
			read++;
		return read;
	}

	/**
	 * @see java.io.FilterReader#ready()
	 */
	public synchronized boolean ready() throws IOException
	{
		if (_hasBufferedChar)
			return true;
		return _reader.ready();
	}

	/**
	 * @see java.io.FilterReader#markSupported()
	 */
	public synchronized boolean markSupported()
	{
		return true;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException
	{
		if (_hasBufferedChar)
		{
			_bufferedCharSaved = _bufferedChar;
			_hasBufferedCharSaved = true;
		}
		_reader.mark(readAheadLimit);
	}

	/**
	 * @see java.io.FilterReader#reset()
	 */
	public synchronized void reset() throws IOException
	{
		if (_hasBufferedCharSaved)
		{
			_bufferedChar = _bufferedCharSaved;
			_hasBufferedChar = true;
			_hasBufferedCharSaved = false;
		} else
		{
			_hasBufferedChar = false;
		}
		_reader.reset();
	}

	/**
	 * @see java.io.FilterReader#skip(long)
	 */
	public synchronized long skip(long n) throws IOException
	{
		long skipChars = n;
		if (_hasBufferedChar && skipChars > 0)
		{
			_hasBufferedChar = false;
			skipChars--;
		}
		return _reader.skip(skipChars);
	}

	/**
	 * @see Reader#close()
	 */
	public synchronized void close() throws IOException
	{
		_hasBufferedChar = false;
		_hasBufferedCharSaved = true;
		_reader.close();
	}

}