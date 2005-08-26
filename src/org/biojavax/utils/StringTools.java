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

package org.biojavax.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for formatting strings into regular-sized blocks.
 * @author Richard Holland
 */
public class StringTools {
    
    // Static methods so should never be instantiated.
    private StringTools() {}
    
    /**
     * Takes an input string and appends spaces to the left. Ignores
     * any existing leading whitespace when counting the indent size.
     * @param input the input string
     * @param leftIndent the number of spaces to indent it by.
     * @return the indented string.
     */
    public static String leftIndent(String input, int leftIndent) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < leftIndent; i++) b.append(" "); // yuck!
        b.append(input);
        return b.toString();
    }
    
    /**
     * Pads a string to be a certain width by prepending spaces.
     * @param input the string to pad.
     * @param totalWidth the final width required including padded space.
     */
    public static String leftPad(String input, int totalWidth) {
        return leftPad(input, ' ', totalWidth);
    }
    
    /**
     * Pads a string to be a certain width by prepending given symbols.
     * @param input the string to pad.
     * @param padChar the symbol to pad with.
     * @param totalWidth the final width required including padded symbols.
     */
    public static String leftPad(String input, char padChar, int totalWidth) {
        StringBuffer b = new StringBuffer();
        b.append(input);
        while(b.length()<totalWidth) b.insert(0,padChar); // yuck!
        return b.toString();
    }
    
    /**
     * Pads a string to be a certain width by appending spaces.
     * @param input the string to pad.
     * @param totalWidth the final width required including padded space.
     */
    public static String rightPad(String input, int totalWidth) {
        return rightPad(input, ' ', totalWidth);
    }
    
    /**
     * Pads a string to be a certain width by appending given symbols.
     * @param input the string to pad.
     * @param padChar the symbol to pad with.
     * @param totalWidth the final width required including padded symbols.
     */
    public static String rightPad(String input, char padChar, int totalWidth) {
        StringBuffer b = new StringBuffer();
        b.append(input);
        while(b.length()<totalWidth) b.append(padChar); // yuck!
        return b.toString();
    }
    
    /**
     * Word-wraps a string into an array of lines of no more than the given width.
     * The string is split into chunks using the regex supplied to identify the
     * points where it can be broken. If a word is longer than the width required,
     * it is broken mid-word, otherwise the string is always broken between words.
     * @param input the string to format
     * @param sepRegex the regex identifying the break points in the string, to be
     * compiled using Pattern.
     * @param width the width of the lines required
     * @return an array of strings, one per line, containing the wrapped output.
     * @see Pattern
     */
    public static String[] writeWordWrap(String input, String sepRegex, int width) {
        List lines = new ArrayList();
        Pattern p = Pattern.compile(sepRegex);
        int start = 0;
        while (start < input.length()) {
            //skip leading newline symbol as it is a waste of space
            if (input.charAt(start)=='\n') start++;
            //begin from start+width
            int splitPoint = start+width;
            //if has newline before end, use it
            int newline = input.indexOf('\n',start);
            if (newline>start && newline<splitPoint) {
                splitPoint = newline;
            }
            //easy case where only small portion of line remains
            if (splitPoint > input.length()) splitPoint=input.length();
            //hard case, have to split it!
            else {
                //if not match sep, find first point that does
                while (splitPoint>start) {
                    char c = input.charAt(splitPoint);
                    Matcher m = p.matcher(""+c);
                    if (m.matches()) break;
                    splitPoint--;
                }
                //if ended up at splitPoint=start, splitPoint=start+width
                //in order to break word mid-way through
                if (splitPoint==start) splitPoint = start+width;
            }
            //output chunk from start to splitPoint
            lines.add(input.substring(start, splitPoint).trim());
            //start = splitPoint
            start=splitPoint;
        }
        return (String[])lines.toArray(new String[0]);
    }
}
