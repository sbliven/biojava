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

import java.io.*;
import java.util.*;
import java.net.*;

import org.biojava.utils.StaticMemberPlaceHolder;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

/**
 * Format object representing Phred Quality files.
 * The only `sequence property' reported by this parser
 * is PROPERTY_DESCRIPTIONLINE, which is the contents of the
 * sequence's description line (the line starting with a '>'
 * character).
 *
 * Essentially a rework of FastaFormat to cope with the quirks of Phred Quality data.
 * Copyright:    Copyright (c) 2001
 * Company:      AgResearch
 *
 * @author Mark Schreiber
 * @since 1.1
 */

public class PhredFormat implements SequenceFormat, Serializable {

    static
    {
        Set validFormats = new HashSet();
        validFormats.add("Phred");

        SequenceFormat.FORMATS.put(PhredFormat.class.getName(),
                                   validFormats);
    }

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
     * <P>
     * When writing, the lines of sequence will never be longer than the line
     * width.
     *
     * @param width the new line width
     */

    public void setLineWidth(int width) {
        this.lineWidth = lineWidth;
    }

    public boolean readSequence(BufferedReader reader,
                                SymbolParser symParser,
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
                                     SymbolParser parser,
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
     * This method will print symbols to the line width followed by a new line etc.
     * NOTE that an integer symbol does not always correspond to one character therefore
     * a line width of sixty will print sixty characters followed by a new line. Not nescessarily
     * sixty integers.
     */
    public void writeSequence(Sequence seq, PrintStream os)
        throws IOException
    {
        os.print(">");
        os.println(describeSequence(seq));

        StringBuffer line = new StringBuffer();
        for(int i = 1, linesPrinted = 1; i <= seq.length(); i++){
          int val = ((IntegerAlphabet.IntegerSymbol)seq.symbolAt(i)).intValue();
          String s = Integer.toString(val);
          if((line.length() + s.length()) > lineWidth){
            os.println(line.toString());
            line = new StringBuffer();
          }
          line.append(s + " ");
        }
    }

    public void writeSequence(Sequence seq, String format, PrintStream os)
        throws IOException
    {
        String requestedFormat = new String(format);
        boolean          found = false;

        String [] formats = (String []) getFormats().toArray(new String[0]);

        if (! found)
            throw new IOException("Unable to write: an invalid file format '"
                                  + format
                                  + "' was requested");

        writeSequence(seq, os);
    }

    public Set getFormats()
    {
        return (Set) SequenceFormat.FORMATS.get(this.getClass().getName());
    }

    public String getDefaultFormat()
    {
        return "Phred";
    }
}
