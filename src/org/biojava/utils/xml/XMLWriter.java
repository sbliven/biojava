package org.biojava.utils.xml;

import java.io.*;

/**
 * Simple interface for building XML documents.
 *
 * @author Thomas Down
 */

public interface XMLWriter {
    public void printRaw(String s) throws IOException;
    public void openTag(String qName) throws IOException;
    public void attribute(String name, String value) throws IOException;
    public void print(String data) throws IOException;
    public void println(String data) throws IOException;
    public void closeTag(String qName) throws IOException;
}
