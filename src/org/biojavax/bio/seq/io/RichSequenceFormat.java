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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.utils.ParseErrorSource;
import org.biojavax.Namespace;
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
    public void writeRichSequence(RichSequence seq, PrintStream os, Namespace ns)
    throws IOException;
    
    public void writeRichSequence(RichSequence seq, String format, PrintStream os)
    throws IOException;
    public void writeRichSequence(RichSequence seq, String format, PrintStream os, Namespace ns)
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
        public static String[] writeWordWrap(String input, String sepRegex, int width) {
            List lines = new ArrayList();            
            Pattern p = Pattern.compile(sepRegex);            
            int start = 0;
            while (start < input.length()) {
                if (input.charAt(start)=='\n') start++;
                //go from start+width
                int splitPoint = start+width;
                // easy case
                if (splitPoint > input.length()) splitPoint=input.length();
                else {
                    //if has newline before end, use it
                    int newline = input.indexOf('\n',start);
                    if (newline>start && newline<splitPoint) {
                        splitPoint = newline;
                    }
                    //if not match sep, find splitPoint first point that does (min=start)
                    else {
                        while (splitPoint>start) {
                            char c = input.charAt(splitPoint);
                            Matcher m = p.matcher(""+c);
                            if (m.matches()) break;
                            splitPoint--;
                        }
                        //if ended up at splitPoint=start, splitPoint=start+width
                        if (splitPoint==start) splitPoint = start+width;
                    }
                }
                //output chunk from start to splitPoint
                lines.add(input.substring(start, splitPoint).trim());
                //start = splitPoint
                start=splitPoint;
            }
            
            return (String[])lines.toArray(new String[0]);
        }
    }
}
