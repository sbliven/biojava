package org.biojava.utils.xml;

import java.util.*;
import java.io.*;

/**
 * Implementation of XMLWriter which emits nicely formatted documents
 * to a Writer.
 *
 * @author Thomas Down
 */

public class PrettyXMLWriter implements XMLWriter {
    private int indentUnit = 2;

    private PrintWriter writer;
    private boolean isOpeningTag = false;
    private boolean afterNewline = true;
    private int indent = 0;

    public PrettyXMLWriter(PrintWriter writer) {
	this.writer = writer;
    }

    protected void writeIndent()
        throws IOException
    {
	for (int i = 0; i < indent * indentUnit; ++i) {
	    writer.write(' ');
	}
    }

    public void openTag(String qName)
        throws IOException
    {
	if (isOpeningTag) {
	    writer.println('>');
	    afterNewline = true;
	}
	if (afterNewline) {
	    writeIndent();
	}
	writer.print('<');
	writer.print(qName);

	indent++;
	isOpeningTag = true;
	afterNewline = false;
    }

    public void attribute(String qName, String value)
        throws IOException
    {
	if (! isOpeningTag) {
	    throw new IOException("attributes must follow an openTag");
	}

	writer.print(' ');
	writer.print(qName);
	writer.print("=\"");
	printAttributeValue(value);
	writer.print('"');
    }

    public void closeTag(String qName) 
        throws IOException
    {
	indent--;

	if (isOpeningTag) {
	    writer.println(" />");
	} else {
	    if (afterNewline) {
		writeIndent();
	    }
	    writer.print("</");
	    writer.print(qName);
	    writer.println('>');
	}

	isOpeningTag = false;
	afterNewline = true;
    }

    public void println(String data)
        throws IOException
    {
	if (isOpeningTag) {
	    writer.println('>');
	    isOpeningTag = false;
	}
	printChars(data);
	writer.println();
	afterNewline = true;
    }

    public void print(String data)
        throws IOException
    {
	if (isOpeningTag) {
	    writer.print('>');
	    isOpeningTag = false;
	}
	printChars(data);
	afterNewline = false;
    }

    public void printRaw(String data)
        throws IOException
    {
	writer.println(data);
    }

    protected void printChars(String data) 
        throws IOException
    {
	if (data == null) {
	    printChars("null");
	    return;
	}

	for (int pos = 0; pos < data.length(); ++pos) {
	    char c = data.charAt(pos);
	    if (c == '<' || c == '>' || c == '&') {
		numericalEntity(c);
	    } else {
		writer.write(c);
	    }
	}
    }

    protected void printAttributeValue(String data) 
        throws IOException
    {
	if (data == null) {
	    printAttributeValue("null");
	    return;
	}

	for (int pos = 0; pos < data.length(); ++pos) {
	    char c = data.charAt(pos);
	    if (c == '<' || c == '>' || c == '&' || c == '"') {
		numericalEntity(c);
	    } else {
		writer.write(c);
	    }
	}
    }

    protected void numericalEntity(char c)
        throws IOException
    {
	writer.print("&#");
	writer.print((int) c);
	writer.print(';');
    }
}
