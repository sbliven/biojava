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

package org.biojava.bio.program.phred;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.NoSuchElementException;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.StreamParser;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.IntegerAlphabet;

/**
 * Format object representing Phred Quality files.
 * The only `sequence property' reported by this parser
 * is PROPERTY_DESCRIPTIONLINE, which is the contents of the
 * sequence's description line (the line starting with a '>'
 * character).
 *
 * Essentially a rework of FastaFormat to cope with the quirks of Phred Quality data.<p>
 * Copyright (c) 2001<p>
 * Company:      AgResearch<p>
 *
 * @author Mark Schreiber
 * @author Greg Cox
 * @since 1.1
 */

public class PhredFormat implements SequenceFormat, Serializable {

    public static final String DEFAULT = "PHRED";

    /**
     * Constant string which is the property key used to notify
     * listeners of the description lines of Phred sequences.
     */

    public final static String PROPERTY_DESCRIPTIONLINE = "description_line";

    /**
     * The line width for output.
     */
    private int lineWidth = 60;

    /**
     * Retrive the current line width.
     *
     * @return the line width
     */

    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * Set the line width.
     * <p>
     * When writing, the lines of sequence will never be longer than the line
     * width.
     *
     * @param width the new line width
     */

    public void setLineWidth(int width) {
        this.lineWidth = width;
    }

    public boolean readSequence(BufferedReader reader,
                                SymbolTokenization symParser,
                                SeqIOListener siol)
        throws IllegalSymbolException, IOException, ParseException
    {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Premature stream end");
        }
        if (!line.startsWith(">")) {
            throw new IOException("Stream does not appear to contain Phred formatted data: " + line);
        }

        siol.startSequence();

        String description = line.substring(1).trim();
        siol.addSequenceProperty(PROPERTY_DESCRIPTIONLINE, description);

        boolean seenEOF = readSequenceData(reader, symParser, siol);
        siol.endSequence();

        return !seenEOF;
    }

    private boolean readSequenceData(BufferedReader br,
                                     SymbolTokenization parser,
                                     SeqIOListener listener)
        throws IOException, IllegalSymbolException
    {
       char[] buffer = new char[256];
       StreamParser sparser = parser.parseStream(listener);
       boolean seenEOF = false; //reached the end of the file
       boolean reachedEnd = false; //reached the end of this sequence

       while(reachedEnd == false){// while more sequence
         br.mark(buffer.length); // mark the read ahead limit
         int bytesRead = br.read(buffer,0,buffer.length); // read into the buffer
         while(Character.isDigit(buffer[buffer.length -1])){// may have ended halfway through a number
           br.reset();// if so reset
           buffer = new char[buffer.length+64]; //make the buffer a little bigger
           br.mark(buffer.length); //mark the new read ahead limit
           bytesRead = br.read(buffer,0,buffer.length); //read into buffer
         }
         if(bytesRead < 0){ //ie -1 indicates end of file
            seenEOF = reachedEnd = true;
         }else{ // otherwise
           int parseStart = 0;
           int parseEnd = 0;

           // while more sequence and more chars in the buffer and not a new sequence
           while(!reachedEnd && parseStart < bytesRead && buffer[parseStart] != '>'){
             parseEnd = parseStart; //begin looking from parse start
             //while more in buffer and the character read is not whitespace
             while(parseEnd < bytesRead && Character.isWhitespace(buffer[parseEnd]) == false){
               ++parseEnd; //look to the next character
             }

             sparser.characters(buffer,parseStart,parseEnd-parseStart);
             parseStart = parseEnd+1; // start at the character after the valid chunk
             while(parseStart < bytesRead && //more in buffer
                Character.isDigit(buffer[parseStart]) == false && // not yet up to a digit
                buffer[parseStart] != '>'){// Not started a new sequence
                ++parseStart; // look to the next character
             }
          }

           //If found the start of a new sequence
           if(parseStart < bytesRead && buffer[parseStart] == '>'){
             br.reset(); // reset the reader
             // then skip the file reading pointer to the start of the new sequence ready for the
             //next read (if required).
             if(br.skip(parseStart) != parseStart) throw new IOException("Couldn't reset to start of next sequence");
             reachedEnd = true; //found the end of this sequence.
           }
         }
       }

       sparser.close();
       return seenEOF;
    }

    /**
     * Return a suitable description line for a Sequence. If the
     * sequence's annotation bundle contains PROPERTY_DESCRIPTIONLINE,
     * this is used verbatim.  Otherwise, the sequence's name is used.
     */

    protected String describeSequence(Sequence seq) {
        String description = null;
        try {
            description = seq.getAnnotation().getProperty(PROPERTY_DESCRIPTIONLINE).toString();
        } catch (NoSuchElementException ex) {
            description = seq.getName();
        }
        return description;
    }

    /**
     * This method will print symbols to the line width followed by a
     * new line etc.  NOTE that an integer symbol does not always
     * correspond to one character therefore a line width of sixty
     * will print sixty characters followed by a new line. Not
     * necessarily sixty integers.
     */
    public void writeSequence(Sequence seq, PrintStream os)
        throws IOException
    {
        os.print(">");
        os.println(describeSequence(seq));

        StringBuffer line = new StringBuffer();
        int seqLen = seq.length();

        for (int i = 1, linesPrinted = 1; i <= seqLen; i++) {
            int val = ((IntegerAlphabet.IntegerSymbol)seq.symbolAt(i)).intValue();
            String s = Integer.toString(val);
            if ((line.length() + s.length()) > lineWidth) {
                os.println(line.substring(0));
                line = new StringBuffer();
            }
            line.append(s + " ");
        }
    }

    /**
     * <code>writeSequence</code> writes a sequence to the specified
     * <code>PrintStream</code>, using the specified format.
     *
     * @param seq a <code>Sequence</code> to write out.
     * @param format a <code>String</code> indicating which sub-format
     * of those available from a particular
     * <code>SequenceFormat</code> implemention to use when
     * writing.
     * @param os a <code>PrintStream</code> object.
     *
     * @exception IOException if an error occurs.
     * @deprecated use writeSequence(Sequence seq, PrintStream os)
     */
    public void writeSequence(Sequence seq, String format, PrintStream os)
        throws IOException
    {
        if (! format.equalsIgnoreCase(getDefaultFormat()))
            throw new IllegalArgumentException("Unknown format '"
                                               + format
                                               + "'");
        writeSequence(seq, os);
    }

    /**
     * <code>getDefaultFormat</code> returns the String identifier for
     * the default format.
     *
     * @return a <code>String</code>.
     * @deprecated
     */
    public String getDefaultFormat()
    {
        return DEFAULT;
    }
}
