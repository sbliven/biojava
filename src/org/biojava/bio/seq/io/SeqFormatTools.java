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
import java.util.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;


public class SeqFormatTools
{

    private static final int FIRST    = 0;
    private static final int OVERWIDE = 1;
    private static final int NOFIT    = 2;
    private static final int FIT      = 3;

    private SeqFormatTools() { };

    public static StringBuffer formatQualifierBlock(String text,
						    String leader,
						    int    wrapWidth)
    {
	int tokenType = FIRST;
	int  position = leader.length();

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
		    for (int i = 0; i < s.length(); i++)
		    {
			if (position == wrapWidth)
			{
			    output.append("\n" + leader);
			    position = leader.length();
			}
			output.append(s.charAt(i));
			position++;
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
	return output;
    }

    public static StringBuffer formatTokenBlock(Symbol [] syms,
						int       blockSize)
    {
	StringBuffer sb = new StringBuffer(syms.length);

	for (int i = 0; i < syms.length; i++)
	{
	    sb.append(syms[i].getToken());
	    if ((i > 0) && ((i + 1) % blockSize == 0))
		sb.append(' ');
	}
	return sb;
    }

    public static StringBuffer formatLocationBlock(Location loc,
						   int      strand,
						   String   leader,
						   int      wrapWidth)
    {
	// Indicates how many characters have been added to the
	// current line
	int       position = leader.length();
	boolean       join = false;
	boolean complement = false;

	List locs = new ArrayList();
	for (Iterator li = loc.blockIterator(); li.hasNext();)
	{
	    locs.add(li.next());
	}

	Location [] la = (Location[]) locs.toArray(new Location [0]);
	// Arrays.sort(la, Location.naturalOrder);

	StringBuffer sb = new StringBuffer(leader);

	if (strand == -1)
	{
	    complement = true;
	    sb.append("complement(");
	    position += 11;
	}

	if (loc instanceof CompoundLocation)
	{
	    join = true;
	    sb.append("join(");
	    position += 5;
	}

	int pre, post;
	int diff = 0;
	for (int i = 0; i < la.length; i++)
	{
	    pre = sb.length();

	    boolean point = (PointLocation.class.isInstance(la[i])) ? true : false;
	    boolean fuzzy = (FuzzyLocation.class.isInstance(la[i])) ? true : false;

	    if (point)
	    {
		if (fuzzy && ! ((FuzzyLocation) la[i]).hasBoundedMin())
		    sb.append("<");
		if (fuzzy && ! ((FuzzyLocation) la[i]).hasBoundedMax())
		    sb.append(">");

		sb.append(la[i].getMin());
	    }
	    else
	    {
		if (fuzzy && ! ((FuzzyLocation) la[i]).hasBoundedMin())
		    sb.append("<");
		sb.append(la[i].getMin());

		sb.append("..");

		if (fuzzy && ! ((FuzzyLocation) la[i]).hasBoundedMax())
		    sb.append(">");
		sb.append(la[i].getMax());
	    }

	    // If there is another location after this
	    if (i + 1 < la.length)
		sb.append(",");

	    post = sb.length();

	    // The number of characters just added
	    diff = post - pre;

	    // If we have exceeded the line length
	    if ((position + diff) > wrapWidth)
	    {
		// Insert a newline just prior to the this loaction
		// string
		sb.insert((sb.length() - diff), "\n" + leader);
		position = leader.length() + diff;
	    }
	    else
	    {
		position += diff;
	    }
	}

	if (join)
	{
	    sb.append(")");
	    if ((position + 1) > wrapWidth)
	    {
		sb.insert((sb.length() - diff), "\n" + leader);
		position++;
		diff++;
	    }
	}

	if (complement)
	{
	    sb.append(")");
	    if ((position + 1) > wrapWidth)
	    {
		sb.insert((sb.length() - diff), "\n" + leader);
	    }
	}

	return sb;
    }

    public static void loadFeatureData(String featureDataFile,
				       Map    featureData,
				       Map    qualifierData)
    {
	try
	{
	    InputStream featureDataStream  =
		EmblFileFormer.class.getClassLoader().getResourceAsStream(featureDataFile);
	    if (featureDataStream == null)
		throw new BioError("Unable to find resource: "
				   + featureDataFile);

	    InputSource   is = new InputSource(featureDataStream);
	    DOMParser parser = new DOMParser();
	    parser.parse(is);

	    // Get document and then the root element
	    Document doc          = parser.getDocument();
	    NodeList featureNodes = doc.getDocumentElement().getChildNodes();

	    // For nodes in root element (features)
	    for (int i = 0; i < featureNodes.getLength(); i++)
	    {
		Node featureNode = featureNodes.item(i);
		if (! (featureNode instanceof Element))
		    continue;

		Element  feature = (Element) featureNode;
		String fNodeName = feature.getNodeName();

		if (fNodeName.equals("feature"))
		{
		    String featureKey = feature.getAttribute("key");

		    NodeList qualifierNodes = feature.getChildNodes();

		    // For nodes in each feature (qualifiers)
		    for (int j = 0; j < qualifierNodes.getLength(); j++)
		    {
			Node qualifierNode = qualifierNodes.item(j);
			if (! (qualifierNode instanceof Element))
			    continue;

			Element qualifier = (Element) qualifierNode;
			String  qNodeName = qualifier.getNodeName();

			if (qNodeName.equals("qualifier"))
			{
			    Map qData = new HashMap();

			    qData.put("form", qualifier.getAttribute("form"));
			    qData.put("mandatory",
				      new Boolean(qualifier.getAttribute("mandatory")));

			    qualifierData.put(qualifier.getAttribute("name"), qData);
			}
		    }

		    featureData.put(featureKey, qualifierData.keySet());
		}
	    }
	}
	catch (IOException ie)
	{
	    ie.printStackTrace();
	}
	catch (SAXException sxe)
	{
	    sxe.printStackTrace();
	}
	catch (BioError be)
	{
	    be.printStackTrace();
	}
    }
}
