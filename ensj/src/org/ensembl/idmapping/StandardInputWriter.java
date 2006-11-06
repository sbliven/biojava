/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.idmapping;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Thread that will write to a process's standard input.
 */
class StandardInputWriter extends Thread {

    private BufferedWriter writer;

    /**
     * Create a new StandardInputWriter as a separate thread. Needs to be started with
     * Thread.start().
     * 
     * @param proc The process to write to.
     */
    public StandardInputWriter(Process proc) {

        writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));

    }

    // -------------------------------------------------------------------------
    /**
     * Write to the process's standard input.
     * 
     * @param s The string to write.
     */
    public void write(String s) throws IOException {

        writer.write(s);

    }

    // -------------------------------------------------------------------------
    /**
     * Write a line to the process's standard input.
     * 
     * @param s The String to write. A newline is automatically appended.
     */
    public void writeln(String s) throws IOException {

        write(s + "\n");

    }

    // -------------------------------------------------------------------------
    /**
     * Close the writer.
     */
    public void close() throws IOException {

        writer.close();

    }

    // -------------------------------------------------------------------------

} // StandardInputWriter
