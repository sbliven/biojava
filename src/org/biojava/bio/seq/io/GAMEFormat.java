/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.io;

import java.io.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.io.game12.*;

/**
 * A rudimentary read-only GAME 1.2 Format object.
 *
 * @author David Huen
 */
class GAMEFormat implements SequenceFormat
{
    static {
        Set validFormats = new HashSet();
        validFormats.add("GAME1.2");
        SequenceFormat.FORMATS.put(FastaFormat.class.getName(),
                                   validFormats);
    }

    public String getDefaultFormat()
    {
        return "GAME1.2";
    }

    public Set getFormats()
    {
        return (Set) SequenceFormat.FORMATS.get(this.getClass().getName());
    }

    /**
     * this version only reads annotations (no symbols)
     */
    public boolean readSequence(BufferedReader reader, SymbolTokenization symParser, SeqIOListener listener)
        throws IOException
    {
        try {
            // set up processing pipeline
            InputSource is = new InputSource(reader);

            GAMEHandler handler = new GAMEHandler(listener);

            SAXParser parser = new SAXParser();
            parser.setContentHandler(new SAX2StAXAdaptor(handler));

            parser.parse(is);

            return false;
        }
        catch (SAXException se) {
            se.printStackTrace();
            throw new IOException("SAXException encountered during parsing");
        }
    }

    public void writeSequence(Sequence seq, PrintStream os)
    {

    }

    public void writeSequence(Sequence seq, String format, PrintStream os)
    {

    }
}

