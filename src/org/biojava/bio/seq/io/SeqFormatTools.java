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
import java.lang.StringBuffer;
import java.util.StringTokenizer;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;


public class SeqFormatTools
{

    private static final int FIRST    = 0;
    private static final int OVERWIDE = 1;
    private static final int NOFIT    = 2;
    private static final int FIT      = 3;

    public static String wrapWithLeader(String text,
					String leader,
					int    wrapWidth)
    {
	int tokenType = FIRST;
	int position  = leader.length();

	StringBuffer output = new StringBuffer(text.length());
	output.append(leader);

	StringTokenizer t = new StringTokenizer(text);

    TOKEN:
	while (t.hasMoreTokens())
	{
	    String s = t.nextToken();
	    String separator = "";

	    // The first token has to be treated differently. It
	    // always starts immediately after the '=' character
	    if (! (tokenType == FIRST))
	    {
		separator = " ";

		if (s.length() + 1 > wrapWidth)
		    tokenType = OVERWIDE;
		else if (position + s.length() + 1 > wrapWidth)
		    tokenType = NOFIT;
		else
		    tokenType = FIT;
	    }

	    switch (tokenType)
	    {
		case FIRST:
		    // The first line always always starts immediately
		    // after the '=' character, even if it means
		    // forcing a break
		    if (! (position + s.length() > wrapWidth))
		    {
			output.append(s);
			position += s.length();
			tokenType = FIT;
			continue TOKEN;
		    }
		    separator = " ";

		case OVERWIDE:
		    // Force breaks in the token until the end is
		    // reached
		    for (int i = 0; i < s.length(); ++i)
		    {
			if (position == wrapWidth)
			{
			    output.append("\n" + leader);
			    position = leader.length();
			}
			output.append(s.charAt(i));
			++position;
		    }

		    position = s.length() % wrapWidth;
		    break;

		case NOFIT:
		    // Token won't fit, so pass it to the next line
		    output.append("\n" + leader + s);
		    position = s.length() + leader.length();
		    break;

		case FIT:
		    // Token fits on this line
		    output.append(separator + s);
		    position += (s.length() + 1);
		    break;

		default:
		    // Nothing
		    break;
	    } // end switch
	} // end while
	return output.toString();
    }

    public static String formatTokenBlocks(Symbol [] syms,
					   int       blockSize)
    {
	StringBuffer sb = new StringBuffer(syms.length);

	for (int i = 0; i < syms.length; i++)
	{
	    sb.append(syms[i].getToken());
	    if ((i > 0) && ((i + 1) % blockSize == 0))
		sb.append(' ');
	}
	return sb.toString();
    }
}
