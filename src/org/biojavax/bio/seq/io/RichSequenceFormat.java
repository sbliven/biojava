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

package org.biojavax.bio.seq.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ParseErrorSource;
import org.biojavax.bio.seq.RichSequence;

/**
 *
 * @author Richard Holland
 */

public interface RichSequenceFormat extends SequenceFormat,ParseErrorSource {
    public boolean readRichSequence(BufferedReader     reader,
            SymbolTokenization symParser,
            RichSeqIOListener      listener)
            throws BioException, IllegalSymbolException, IOException;
    
    public void writeRichSequence(RichSequence seq, PrintStream os)
    throws IOException;
    
    public void writeRichSequence(RichSequence seq, String format, PrintStream os)
    throws IOException;
    
    public static class Tools {
        public static String leftIndent(String input, int leftIndent) {
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < leftIndent; i++) b.append(" "); // yuck!
            b.append(input);
            return b.toString();
        }
        public static String leftPad(String input, int totalWidth) {
            StringBuffer b = new StringBuffer();
            b.append(input);
            while(b.length()<totalWidth) b.insert(0," "); // yuck!
            return b.toString();
        }
        public static String rightPad(String input, int totalWidth) {
            StringBuffer b = new StringBuffer();
            b.append(input);
            while(b.length()<totalWidth) b.append(" "); // yuck!
            return b.toString();
        }
        public static String[] writeWordWrap(String input, String sepRegex, String separator, int width) {
            String[] parts = input.split(sepRegex);
            StringBuffer currentLine = new StringBuffer();
            List lines = new ArrayList();
            for (int i = 0; i < parts.length; i++) {
                String word = parts[i];
                if (word!=null && word.length()>0) {
                    int wordLength = word.length();
                    if (wordLength+currentLine.length() > width) {
                        if (wordLength > width) {
                            // must split word
                            do {
                                int chunkSize = Math.min(width-currentLine.length(),wordLength);
                                currentLine.append(word.substring(0,chunkSize));
                                if (currentLine.length()>=width) {
                                    lines.add(currentLine.toString());
                                    currentLine.setLength(0);
                                }
                                word = word.substring(chunkSize);
                                wordLength = word.length();
                            } while (wordLength+currentLine.length() > width);
                        } else {
                            lines.add(currentLine.toString());
                            currentLine.setLength(0);
                        }
                    }
                    currentLine.append(word);
                    if (i<(parts.length-1)) currentLine.append(separator);
                }
            }
            if (currentLine.length()>0) lines.add(currentLine.toString());
            return (String[])lines.toArray(new String[0]);
        }
    }
}
