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

/*
 * StringTools.java
 *
 * Created on August 24, 2005, 1:51 PM
 */

package org.biojavax.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Richard Holland
 */
public class StringTools {
    
    /** Creates a new instance of StringTools */
    private StringTools() {}
    
    public static String leftIndent(String input, int leftIndent) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < leftIndent; i++) b.append(" "); // yuck!
        b.append(input);
        return b.toString();
    }
    
    public static String leftPad(String input, int totalWidth) {
        return leftPad(input, ' ', totalWidth);
    }
    
    public static String leftPad(String input, char padChar, int totalWidth) {
        StringBuffer b = new StringBuffer();
        b.append(input);
        while(b.length()<totalWidth) b.insert(0,padChar); // yuck!
        return b.toString();
    }
    
    public static String rightPad(String input, int totalWidth) {
        return rightPad(input, ' ', totalWidth);
    }
    
    public static String rightPad(String input, char padChar, int totalWidth) {
        StringBuffer b = new StringBuffer();
        b.append(input);
        while(b.length()<totalWidth) b.append(padChar); // yuck!
        return b.toString();
    }
    
    public static String[] writeWordWrap(String input, String sepRegex, int width) {
        List lines = new ArrayList();
        Pattern p = Pattern.compile(sepRegex);
        int start = 0;
        while (start < input.length()) {
            while (input.charAt(start)=='\n') start++;
            //go from start+width
            int splitPoint = start+width;
            //if has newline before end, use it
            int newline = input.indexOf('\n',start);
            if (newline>start && newline<splitPoint) {
                splitPoint = newline;
            }
            // easy case
            if (splitPoint > input.length()) splitPoint=input.length();
            else {
                //if not match sep, find splitPoint first point that does (min=start)
                while (splitPoint>start) {
                    char c = input.charAt(splitPoint);
                    Matcher m = p.matcher(""+c);
                    if (m.matches()) break;
                    splitPoint--;
                }
                //if ended up at splitPoint=start, splitPoint=start+width
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
