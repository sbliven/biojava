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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioError;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.CompoundLocation;
import org.biojava.bio.symbol.FuzzyLocation;
import org.biojava.bio.symbol.FuzzyPointLocation;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.BetweenLocation;
import org.biojava.bio.symbol.Symbol;

/**
 * <code>AbstractGenEmblFileFormer</code> contain file formatting code
 * common to both GenBank and EMBL file formats.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @author Greg Cox
 * @since 1.2
 */
class AbstractGenEmblFileFormer
{
    private static final String FEATURE_DATA_FILE =
	"org/biojava/bio/seq/io/FeatureQualifier.xml";

    private static final Map   FEATURE_DATA = new HashMap();
    private static final Map QUALIFIER_DATA = new HashMap();

    static
    {
	// This loads an XML file containing information on which
        // qualifiers are valid (or even mandatory) for a particular
        // feature key. It also indicates whether the value should be
        // contained within quotes.
        loadFeatureData(FEATURE_DATA_FILE, FEATURE_DATA, QUALIFIER_DATA);
    }

    /* Defines types of qualifier lines encountered: FIRST - the first
     * line of the qualifier, OVERWIDE - contains a very wide token
     * which can not be wrapped without breaking it, NOFIT - a line
     * which is too wide, but may be wrapped without breaking tokens,
     * FIT - a line which is short enough to add without wrapping it.
     */
    private static final int FIRST    = 0;
    private static final int OVERWIDE = 1;
    private static final int NOFIT    = 2;
    private static final int FIT      = 3;

    /* Defines the various types of Location available. Each is
     * represented differently within an EMBL/Genbank flatfile.
     */
    private static final int            RANGE = 0;
    private static final int            POINT = 1;
    private static final int      FUZZY_RANGE = 2;
    private static final int      FUZZY_POINT = 3;
    private static final int BETWEEN_LOCATION = 4;

    // Utility formatting buffer
    private StringBuffer ub = new StringBuffer();

    /**
     * <code>formatQualifierBlock</code> formats text into
     * EMBL/Genbank style qualifiers.
     *
     * @param text a <code>String</code> to format.
     * @param leader a <code>String</code> to append to the start of
     * each line.
     * @param wrapWidth an <code>int</code> indicating the number of
     * columns per line.
     * @return a <code>StringBuffer</code>.
     */
    StringBuffer formatQualifierBlock(final StringBuffer sb,
				      final String       text,
				      final String       leader,
				      final int          wrapWidth)
    {
	// Get separator for system
	String nl = System.getProperty("line.separator");

	int tokenType = FIRST;
	int  position = leader.length();

	sb.append(leader);

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
			sb.append(s);
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
			    sb.append(nl + leader);
			    position = leader.length();
			}
			sb.append(s.charAt(i));
			position++;
		    }

		    position = s.length() % wrapWidth;
		    break;

		case NOFIT:
		    // Token won't fit, so pass it to the next line
		    sb.append(nl + leader + s);
		    position = s.length() + leader.length();
		    break;

		case FIT:
		    // Token fits on this line
		    sb.append(separator + s);
		    position += (s.length() + 1);
		    break;

		default:
		    // Nothing
		    break;
	    } // end switch
	} // end while

	return sb;
    }

    /**
     * <code>formatQualifier</code> creates a qualifier string and
     * adds quotes or parens to EMBL/Genbank qualifier values as
     * specified in the internally loaded XML feature table
     * description.
     *
     * @param key an <code>Object</code> (the qualifier key).
     * @param value an <code>Object</code> (the qualifier content).
     *
     * @return a <code>String</code> bounded by the correct tokens.
     */
    StringBuffer formatQualifier(final StringBuffer sb, final Object key, final Object value)
    {
	sb.append("/" + key);

	// Default is to quote unknown qualifiers
	String form = "quoted";
	if (QUALIFIER_DATA.containsKey(key))
            form = (String) ((Map) QUALIFIER_DATA.get(key)).get("form");

	// This is a slight simplification. There are some types of
        // qualifier which are unquoted unless they contain
        // spaces. We all love special cases, don't we?
        if (form.equals("quoted"))
            sb.append("=\"" + value + "\"");
        else if (form.equals("bare"))
            sb.append("=" + value);
        else if (form.equals("paren"))
            sb.append("(" + value + ")");
        else if (! form.equals("empty"))
	{
            System.err.println("Unrecognised qualifier format: " + form);
	    sb.append("=" + value);
	}

        return sb;
    }

    /**
     * <code>formatTokenBlock</code> divides up the tokens
     * representing the <code>Symbols</code> into blocks of the
     * specified length, with a single space delimeter.
     *
     * @param syms a <code>Symbol []</code> array whose tokens are to
     * be formatted.
     * @param blockSize an <code>int</code> indicating the size of
     * each block.
     *
     * @return a <code>StringBuffer</code>.
     */
    StringBuffer formatTokenBlock(final StringBuffer sb,
				  final Symbol []    syms,
				  final int          blockSize)
    {
	for (int i = 0; i < syms.length; i++)
	{
	    sb.append(syms[i].getToken());
	    if ((i > 0) && ((i + 1) % blockSize == 0))
		sb.append(' ');
	}
	return sb;
    }

    /**
     * <code>formatLocation</code> creates an EMBL/Genbank style
     * representation of a <code>Location</code>. This is a
     * convenience method only. The version which has a
     * <code>StringBuffer</code> parameter (and returns the
     * <code>StringBuffer</code>) is preferred.
     *
     * @param loc a <code>Location</code> to format.
     * @param strand a <code>StrandedFeature.Strand</code>
     * indicating the <code>Location</code>'s strand.
     *
     * @return a <code>StringBuffer</code>.
     */
    public String formatLocation(final Location               loc,
				 final StrandedFeature.Strand strand)
    {
	// Using arbitrary leader and wrapwidth wide enough to always
	// make one line
	StringBuffer sb = formatLocationBlock(new StringBuffer(),
					      loc,
					      strand.getValue(),
					      "",
					      Integer.MAX_VALUE);

	return sb.toString();
    }

    /**
     * <code>formatLocation</code> creates an EMBL/Genbank style
     * representation of a <code>Location</code>. Supported location
     * forms:
     *     
     * <pre>
     *   123
     *  <123 or >123
     *  (123.567)
     *  (123.567)..789
     *   123..(567.789)
     *  (123.345)..(567.789)
     *   123..456
     *  <123..567 or 123..>567 or <123..>567
     *  123^567
     * </pre>
     *
     * Specifically not supported is:
     * <pre>
     *   AL123465:(123..567)
     * </pre>
     *
     * Use of 'order' rather than 'join' is not retained over a
     * read/write cycle. i.e. 'order' is converted to 'join'.
     *
     * @param sb a <code>StringBuffer</code to which the location will
     * be appended.
     * @param loc a <code>Location</code> to format.
     * @param strand a <code>StrandedFeature.Strand</code>
     * indicating the <code>Location</code>'s strand.
     *
     * @return a <code>StringBuffer</code> value.
     */
    public StringBuffer formatLocation(final StringBuffer           sb,
				       final Location               loc,
				       final StrandedFeature.Strand strand)
    {
	// Using arbitrary leader and wrapwidth wide enough to always
	// make one line
	return formatLocationBlock(sb, loc, strand.getValue(), "", Integer.MAX_VALUE);
    }

    /**
     * <code>formatLocationBlock</code> creates an EMBL/Genbank style
     * representation of a <code>Location</code> wrapped to a specific
     * width.
     *
     * @param loc a <code>Location</code> to use as a template.
     * @param strand an <code>int</code> indicating the
     * <code>Location</code>'s strand.
     * @param leader a <code>String</code> to append to the start of
     * each line.
     * @param wrapWidth an <code>int</code> indicating the number of
     * columns per line.
     *
     * @return a <code>StringBuffer</code>.
     */
    StringBuffer formatLocationBlock(final StringBuffer sb,
				     final Location     loc,
				     final int          strand,
				     final String       leader,
				     final int          wrapWidth)
    {
	// Get separator for system
	String nl = System.getProperty("line.separator");

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

	/* There are issues here about choosing various forms:
	 * join(complement(...),complement(...))
	 * complement(join(...,...))
	 * 
	 * The former has the locations sorted in reverse order.
	 */

	Collections.sort(locs, Location.naturalOrder);

	if (loc instanceof CompoundLocation)
	{
	    join = true;
	    sb.append("join(");
	    position += 5;
	}

	if (strand == -1)
	{
	    Collections.reverse(locs);
	    complement = true;
	}

	int locType = 0;

	// Records the length of the String(s) added to the buffer to
	// determine whether we need to wrap the line
	int pre, post;
	int diff = 0;

	for (Iterator li = locs.iterator(); li.hasNext();)
	{
	    Location thisLoc = (Location) li.next();

	    pre = sb.length();

	    if (PointLocation.class.isInstance(thisLoc))
		locType = POINT;
	    else if (FuzzyLocation.class.isInstance(thisLoc))
		locType = FUZZY_RANGE;
	    else if (FuzzyPointLocation.class.isInstance(thisLoc))
		locType = FUZZY_POINT;
	    else if (BetweenLocation.class.isInstance(thisLoc))
		locType = BETWEEN_LOCATION;
	    else
		locType = RANGE;

	    ub.setLength(0);
	    switch (locType)
	    {
		case POINT:
		    PointLocation pl = (PointLocation) thisLoc;

		    sb.append(complement                                   ?
			      toComplement(formatPoint(ub, pl).toString()) :
			      formatPoint(ub, pl).toString());
		    break;

		case FUZZY_RANGE:
		    FuzzyLocation fl = (FuzzyLocation) thisLoc;

		    sb.append(complement                                        ?
			      toComplement(formatFuzzyRange(ub, fl).toString()) :
			      formatFuzzyRange(ub, fl).toString());
		    break;

		case FUZZY_POINT:
		    FuzzyPointLocation fpl = (FuzzyPointLocation) thisLoc;

		    sb.append(complement                                         ?
			      toComplement(formatFuzzyPoint(ub, fpl).toString()) :
			      formatFuzzyPoint(ub, fpl).toString());
		    break;

		case RANGE:
		    RangeLocation rl = (RangeLocation) thisLoc;

		    sb.append(complement                                   ?
			      toComplement(formatRange(ub, rl).toString()) :
			      formatRange(ub, rl).toString());
		    break;

		case BETWEEN_LOCATION:
		    BetweenLocation tempLocation = (BetweenLocation) thisLoc;
		    String formattedLocation = formatBetween(ub, tempLocation).toString();
		    if (complement)
		    {
			formattedLocation = toComplement(formattedLocation);
		    }
		    sb.append(formattedLocation);
		    break;

		default:
		    // Maybe exception here?
		    break;
	    }

	    // If there is another location after this
	    if ((locs.indexOf(thisLoc) + 1) < locs.size())
		sb.append(",");

	    post = sb.length();

	    // The number of characters just added
	    diff = post - pre;

	    // If we have exceeded the line length
	    if ((position + diff) > wrapWidth)
	    {
		// Insert a newline just prior to this location string
		sb.insert((sb.length() - diff), nl + leader);
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
	    // If adding the ")" has made the line too long, move the
	    // last range to the next line
	    if ((position + 1) > wrapWidth)
	    {
		sb.insert((sb.length() - diff), nl + leader);
		position++;
		diff++;
	    }
	}

	return sb;
    }

    /**
     * <code>formatFuzzyRange</code> creates an EMBL/Genbank style
     * String representation of a <code>FuzzyLocation</code>.
     *
     * @param sb a <code>StringBuffer</code> to format the location into.
     * @param fl a <code>FuzzyLocation</code>.
     *
     * @return a <code>String</code> representation of the location.
     */
    private StringBuffer formatFuzzyRange(final StringBuffer sb, final FuzzyLocation fl)
    {
	if (! fl.hasBoundedMin())
	{
	    // <123
	    sb.append("<");
	    sb.append(fl.getMin());
	}
	else if (fl.getOuterMin() != fl.getInnerMin())
	{
	    // (123.567)
	    sb.append("(" + fl.getOuterMin());
	    sb.append(".");
	    sb.append(fl.getInnerMin() + ")");
	}
	else
	{
	    // 123
	    sb.append(fl.getMin());
	}

	sb.append("..");

	if (! fl.hasBoundedMax())
	{
	    // >567
	    sb.append(">");
	    sb.append(fl.getMax());
	}
	else if (fl.getInnerMax() != fl.getOuterMax())
	{
	    // (567.789)
	    sb.append("(" + fl.getInnerMax());
	    sb.append(".");
	    sb.append(fl.getOuterMax() + ")");
	}
	else
	{
	    // 567
	    sb.append(fl.getMax());
	}

	return sb;
    }

    /**
     * <code>formatFuzzyPoint</code> creates an EMBL/Genbank style
     * String representation of a <code>FuzzyPointLocation</code>.
     *
     * @param sb a <code>StringBuffer</code> to format the location into.
     * @param fpl a <code>FuzzyPointLocation</code>.
     *
     * @return a <code>String</code> representation of the location.
     */
    private StringBuffer formatFuzzyPoint(final StringBuffer sb, final FuzzyPointLocation fpl)
    {
	if (! fpl.hasBoundedMin())
	{
	    // <123
	    sb.append("<");
	    sb.append(fpl.getMax());
	}
	else if (! fpl.hasBoundedMax())
	{
	    // >567
	    sb.append(">");
	    sb.append(fpl.getMin());
	}
	else
	{
	    // (567.789)
	    sb.append("(" + fpl.getMin());
	    sb.append(".");
	    sb.append(fpl.getMax() + ")");
	}

	return sb;
    }

    /**
     * <code>formatRange</code> creates an EMBL/Genbank style String
     * representation of a <code>RangeLocation</code>.
     *
     * @param sb a <code>StringBuffer</code> to format the location into.
     * @param rl a <code>RangeLocation</code>.
     *
     * @return a <code>String</code> representation of the location.
     */
    private StringBuffer formatRange(final StringBuffer sb, final RangeLocation rl)
    {
	// 123..567
	sb.append(rl.getMin());
	sb.append("..");
	sb.append(rl.getMax());

	return sb;
    }

    /**
     * <code>formatPoint</code> creates an EMBL/Genbank style String
     * representation of a <code>PointLocation</code>.
     *
     * @param sb a <code>StringBuffer</code> to format the location into.
     * @param pl a <code>PointLocation</code>.
     *
     * @return a <code>String</code> representation of the location.
     */
    private StringBuffer formatPoint(final StringBuffer sb, final PointLocation pl)
    {
	sb.append(Integer.toString(pl.getMin()));
	return sb;
    }

    /**
     * Formats a between location x y into x^y.
     *
     * @param sb a <code>StringBuffer</code> to format the location into.
     * @param theLocation The between location object to be formatted
     *
     * @return A string representation of the location
     */
    private StringBuffer formatBetween(final StringBuffer sb, final BetweenLocation theLocation)
    {
	sb.append(theLocation.getMin());
	sb.append('^');
	sb.append(theLocation.getMax());
	return sb;
    }

    /**
     * <code>toComplement</code> accepts an EMBL/Genbank style String
     * representation of a <code>Location</code> and returns the
     * complementary strand version.
     *
     * @param value a <code>String</code>.
     *
     * @return a <code>String</code> representation of the
     * complementary strand location.
     */
    private String toComplement(final String value)
    {
	return "complement(" + value + ")";
    }

    /**
     * <code>loadFeatureData</code> reads data describing EMBL/Genbank
     * features and qualifiers from an XML file and populates two data
     * Maps, one for features, one for qualifiers. The file describes
     * which qualifiers are optional or mandatory for a feature type
     * and which qualifiers are written within quotes (or
     * parantheses). The DTD used by the XML file is stored in the
     * file's internal subset.
     *
     * @param featureDataFile a <code>String</code> indicating the
     * name of the file.
     * @param featureData a <code>Map</code> to populate with
     * feature data.
     * @param qualifierData a <code>Map</code> to populate with
     * qualifier data.
     */
    static void loadFeatureData(final String featureDataFile,
				final Map    featureData,
				final Map    qualifierData)
    {
	try
	{
	    InputStream featureDataStream  =
		EmblFileFormer.class.getClassLoader().getResourceAsStream(featureDataFile);
	    if (featureDataStream == null)
		throw new BioError("Failed to find resource: "
				   + featureDataFile);

	    InputSource   is = new InputSource(featureDataStream);
	    DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();

	    // Get document and then the root element
	    Document doc          = parser.parse(is);
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

                featureDataStream.close();
	    }
	}
	catch (IOException ioe)
	{
	    ioe.printStackTrace();
	}
	catch (SAXException se)
	{
	    se.printStackTrace();
	}
	catch (BioError be)
	{
	    be.printStackTrace();
	} catch (ParserConfigurationException ex) {
	    ex.printStackTrace();
	}
    }
}
