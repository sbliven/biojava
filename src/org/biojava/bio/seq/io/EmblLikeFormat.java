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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * <p>
 * Format processor for handling EMBL records and similar files.  This
 * takes a very simple approach: all `normal' attribute lines are
 * passed to the listener as a tag (first two characters) and a value
 * (the rest of the line from the 6th character onwards).  Any data
 * between the special `SQ' line and the "//" entry terminator is
 * passed as a SymbolReader.
 * </p>
 *
 * <p>
 * This low-level format processor should normally be used in
 * conjunction with one or more `filter' objects, such as
 * EmblProcessor.
 * </p>
 *
 * <p>
 * Many ideas borrowed from the old EmblFormat processor by Thomas
 * Down and Thad Welch.
 * </p>
 *
 * @author Thomas Down
 * @author Greg Cox
 * @author Keith James
 * @since 1.1
 */

public class EmblLikeFormat
    implements
            SequenceFormat,
            Serializable,
            ParseErrorSource,
            ParseErrorListener
{
    static
    {
        Set validFormats = new HashSet();
        validFormats.add("Embl");
        validFormats.add("Swissprot");

        SequenceFormat.FORMATS.put(EmblLikeFormat.class.getName(),
                                   validFormats);
    }

    private boolean elideSymbols = false;
    private Vector mListeners = new Vector();

    /**
     * <p>Specifies whether the symbols (SQ) part of the entry should
     * be ignored. If this property is set to <code>true</code>, the
     * parser will never call addSymbols on the
     * <code>SeqIOListener</code>, but parsing will be faster if
     * you're only interested in header information.</p>
     *
     * <p> This property also allows the header to be parsed for files
     * which have invalid sequence data.</p>
     */
    public void setElideSymbols(boolean b)
    {
        elideSymbols = b;
    }

    /**
     * Return a flag indicating if symbol data will be skipped
     * when parsing streams.
     */
    public boolean getElideSymbols()
    {
        return elideSymbols;
    }

    public boolean readSequence(BufferedReader     reader,
                                SymbolTokenization symParser,
                                SeqIOListener      listener)
        throws IllegalSymbolException, IOException, ParseException
    {
	if (listener instanceof ParseErrorSource) {
	    ((ParseErrorSource)(listener)).addParseErrorListener(this);
	}

        String            line;
        StreamParser    sparser       = null;
        boolean hasMoreSequence       = true;
        boolean hasInternalWhitespace = false;

        listener.startSequence();

        while ((line = reader.readLine()) != null)
        {
            if (line.startsWith("//"))
            {
                if (sparser != null)
                {
                    // End of symbol data
                    sparser.close();
                    sparser = null;
                }

                // Allows us to tolerate trailing whitespace without
                // thinking that there is another Sequence to follow
                while (true)
                {
                    reader.mark(1);
                    int c = reader.read();

                    if (c == -1)
                    {
                        hasMoreSequence = false;
                        break;
                    }

                    if (Character.isWhitespace((char) c))
                    {
                        hasInternalWhitespace = true;
                        continue;
                    }

                    if (hasInternalWhitespace)
                        System.err.println("Warning: whitespace found between sequence entries");

                    reader.reset();
                    break;
                }

                listener.endSequence();
                return hasMoreSequence;
            }
            else if (line.startsWith("SQ"))
            {
                // Adding a null property to flush the last feature;
                // Needed for Swissprot files because there is no gap
                // between the feature table and the sequence data
                listener.addSequenceProperty("XX", "");

                sparser = symParser.parseStream(listener);
            }
            else
            {
                if (sparser == null)
                {
                    // Normal attribute line
                    String tag  = line.substring(0, 2);
                    String rest = null;
                    if (line.length() > 5)
                    {
                        rest = line.substring(5);
                    }
                    listener.addSequenceProperty(tag, rest);
                }
                else
                {
                    // Sequence line
                    if (! elideSymbols)
                        processSequenceLine(line, sparser);
                }
            }
        }

        if (sparser != null)
            sparser.close();

        throw new IOException("Premature end of stream or missing end tag '//' for EMBL");
    }

    /**
     * Dispatch symbol data from SQ-block line of an EMBL-like file.
     */
    protected void processSequenceLine(String line, StreamParser parser)
        throws IllegalSymbolException, ParseException
    {
        char[] cline = line.toCharArray();
        int parseStart = 0;
        int parseEnd   = 0;

        while (parseStart < cline.length)
        {
            while (parseStart < cline.length && cline[parseStart] == ' ')
                ++parseStart;
            if (parseStart >= cline.length)
                break;

            if (Character.isDigit(cline[parseStart]))
                return;

            parseEnd = parseStart + 1;
            while (parseEnd < cline.length && cline[parseEnd] != ' ') {
                if (cline[parseEnd] == '.' || cline[parseEnd] == '~') {
                   cline[parseEnd] = '-';
                }
                ++parseEnd;
            }

            // Got a segment of read sequence data
            parser.characters(cline, parseStart, parseEnd - parseStart);

            parseStart = parseEnd;
        }
    }

    public void writeSequence(Sequence seq, PrintStream os)
        throws IOException
    {
        String defaultFormat = getDefaultFormat();

        try
        {
            SeqFileFormer former = SeqFileFormerFactory.makeFormer(defaultFormat);
            former.setPrintStream(os);

            SeqIOEventEmitter.getSeqIOEvents(seq, former);
        }
        catch (BioException be)
        {
            throw new IOException(be.getMessage());
        }
    }

    public void writeSequence(Sequence seq, String format, PrintStream os)
	throws IOException
    {
        String requestedFormat = new String(format);
        boolean          found = false;

        String [] formats = (String []) getFormats().toArray(new String[0]);

        // Allow client programmers to use whichever case they like
        for (int i = 0; i < formats.length; i++)
        {
            if (formats[i].equalsIgnoreCase(format))
            {
                requestedFormat = formats[i];
                found = true;
            }
        }

        if (! found)
            throw new IOException("Failed to write: an invalid file format '"
                                  + format
                                  + "' was requested");

        try
        {
            SeqFileFormer former = SeqFileFormerFactory.makeFormer(requestedFormat);
            former.setPrintStream(os);

            SeqIOEventEmitter.getSeqIOEvents(seq, former);
        }
        catch (BioException be)
        {
            throw new IOException(be.getMessage());
        }
    }

    public Set getFormats()
    {
        return (Set) SequenceFormat.FORMATS.get(this.getClass().getName());
    }

    public String getDefaultFormat()
    {
        return "Embl";
    }

    /**
     * <p>
     * This method determines the behaviour when a bad line is processed.
     * Some options are to log the error, throw an exception, ignore it
     * completely, or pass the event through.
     * </p>
     *
     * <p>
     * This method should be overwritten when different behavior is desired.
     * </p>
     *
     * @param theEvent The event that contains the bad line and token.
     */
    public void BadLineParsed(ParseErrorEvent theEvent)
    {
        notifyParseErrorEvent(theEvent);
    }

    /**
     * Adds a parse error listener to the list of listeners if it isn't already
     * included.
     *
     * @param theListener Listener to be added.
     */
    public synchronized void addParseErrorListener(ParseErrorListener theListener)
    {
        if(mListeners.contains(theListener) == false)
        {
            mListeners.addElement(theListener);
        }
    }

    /**
     * Removes a parse error listener from the list of listeners if it is
     * included.
     *
     * @param theListener Listener to be removed.
     */
    public synchronized void removeParseErrorListener(ParseErrorListener theListener)
    {
        if(mListeners.contains(theListener) == true)
        {
            mListeners.removeElement(theListener);
        }
    }
 
    // Protected methods
    /**
     * Passes the event on to all the listeners registered for ParseErrorEvents.
     *
     * @param theEvent The event to be handed to the listeners.
     */
    protected void notifyParseErrorEvent(ParseErrorEvent theEvent)
    {
        Vector listeners;
        synchronized(this)
        {
            listeners = (Vector)mListeners.clone();
        }

        for (int index = 0; index < listeners.size(); index++)
        {
            ParseErrorListener client = (ParseErrorListener)listeners.elementAt(index);
            client.BadLineParsed(theEvent);
        }
    }
}
