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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread that will a process's standard output or standard error and print it to a PrintStream.
 */
class StandardStreamReader extends Thread {

    protected BufferedReader reader;

    protected PrintStream out;

    protected List buffer;

    protected boolean doBuffer = true;

    protected Process proc;

    private boolean stopped = false;

    /**
     * Create a new StandardStreamReader. This is in a separate thread in order to prevent problems with blocking. Need to call
     * start() to actually start it.
     * 
     * @param stream The stream to read; will be one of Process.getInputStream() or Process.getErrorStream()
     * @param out The output stream to print to (e.g. System.out)
     * @param buffer If true, store output to be retrieved by getBufferedOutput()
     */
    public StandardStreamReader(InputStream stream, PrintStream out, boolean doBuffer) {

        this.out = out;
        this.doBuffer = doBuffer;

        buffer = new ArrayList();

        reader = new BufferedReader(new InputStreamReader(stream));

    }

    // -------------------------------------------------------------------------
    /**
     * Start listening to the process.
     */
    public void run() {

        try {

            while (!reader.ready()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            String line;
            while (!stopped && reader != null && reader.ready() && (line = reader.readLine()) != null) {

                if (doBuffer) {
                    buffer.add(line);
                }
            }

            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Close the reader and terminate the thread.
     */
    public void close() throws IOException {

        reader.close();
        reader = null; // breaks while loop in run()

    }

    // -------------------------------------------------------------------------
    /**
     * Get the buffered output as a List of Strings. Will be empty if buffer flag to constructor was false.
     * 
     * @return The output, as a list of Strings.
     */
    public List getBuffer() {

        return buffer;

    }

    // -------------------------------------------------------------------------

    public boolean ready() {

        boolean result = false;

        if (reader == null) {
            return false;
        }

        try {

            result = reader.ready();

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }

        return result;

    }

    // -------------------------------------------------------------------------
    /**
     * Cause the thread to stop running in an orderly manner. Safer than just calling Thread.stop.
     */
    public void stopThread() {

        this.stopped = true;

    }

    // -------------------------------------------------------------------------

} // StandardReader
