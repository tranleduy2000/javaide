/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2011 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;


/**
 * A <code>WordReader</code> that returns words from a file or a URL.
 *
 * @author Eric Lafortune
 */
public class FileWordReader extends WordReader
{
    private final String           name;
    private LineNumberReader reader;


    /**
     * Creates a new FileWordReader for the given file.
     */
    public FileWordReader(File file) throws IOException
    {
        super(file.getParentFile());

        this.name   = file.getPath();
        this.reader = new LineNumberReader(
                      new BufferedReader(
                      new FileReader(file)));
    }


    /**
     * Creates a new FileWordReader for the given URL.
     */
    public FileWordReader(URL url) throws IOException
    {
        super(null);

        this.name   = url.toString();
        this.reader = new LineNumberReader(
                       new BufferedReader(
                       new InputStreamReader(url.openStream())));
    }


    // Implementations for WordReader.

    protected String nextLine() throws IOException
    {
        return reader.readLine();
    }


    protected String lineLocationDescription()
    {
        return "line " + reader.getLineNumber() + " of file '" + name + "'";
    }


    public void close() throws IOException
    {
        super.close();

        if (reader != null)
        {
            reader.close();
        }
    }
}
