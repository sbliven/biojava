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

import org.apache.xerces.parsers.DOMParser;

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
import org.biojava.bio.symbol.Symbol;

/**
 * <code>SeqFormatTools</code> is a utility class for common sequence
 * formatting operations required when writing flat files.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SeqFormatTools
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
    private static final int       RANGE = 0;
    private static final int       POINT = 1;
    private static final int FUZZY_RANGE = 2;
    private static final int FUZZY_POINT = 3;

    /**
     * <code>SeqFormatTools</code> can not be instantiated.
     */
    private SeqFormatTools() { };


    /**
     * <code>writeFeatureAsTable</code> writes a single
     * <code>Feature</code> as a feature table block. The leader
     * string is set by a parameter to allow this to work for both
     * EMBL and Genbank formats. There is some duplication of
     * functionality from the <code>EmblFileFormer</code> and
     * <code>GenbankFileFormer</code> classes. However, they print the
     * data in response to SeqIO events. This convenience method dumps
     * an entire feature at single method call.
     *
     * @param f a <code>Feature</code> to write.
     * @param leader a <code>String</code> to attach to the start of
     * each line. If this is shorter than the value returned by the
     * <code>Feature</code>'s <code>getType()</code> method, you may
     * get unexpected results. This is checked for.
     * @param stream a <code>PrintStream</code> to write to.
     */
    public static void writeFeatureAsTable(final Feature     f,
					   final String      leader,
					   final PrintStream stream)
    {
        int strand = 0;
	if (StrandedFeature.class.isInstance(f))
	    strand = ((StrandedFeature) f).getStrand().getValue();

	StringBuffer lb = formatLocationBlock(f.getLocation(),
					      strand,
					      leader,
					      80);
	String type = f.getType();

	// Only substitute in the type name if there is room
	if (leader.length() >= type.length())
	    lb.replace(5, 5 + type.length(), type);

	stream.println(lb);

	Annotation fa = f.getAnnotation();

	StringBuffer ab = new StringBuffer();

	for (Iterator ai = fa.keys().iterator(); ai.hasNext();)
	{
	    Object key   = ai.next();
	    Object value = fa.getProperty(key);

	    if (key.equals(Feature.PROPERTY_DATA_KEY))
		continue;

	    if (Collection.class.isInstance(value))
	    {
		for (Iterator vi = ((Collection) value).iterator(); vi.hasNext();)
		{
		    stream.println(formatQualifierBlock(formatQualifier(key, vi.next()),
							leader,
							80));
		}
	    }
	    else
	    {
		stream.println(formatQualifierBlock(formatQualifier(key, value),
						    leader,
						    80));
	    }
	}
    }

    /**
     * <code>formatQualifierBlock</code> formats text into
     * EMBL/Genbank style qualifiers.
     *
     * @param text a <code>String</code> to format.
     * @param leader a <code>String</code> to append to the start of
     * each line.
     * @param wrapWidth an <code>int</code> indicating the number of
     * columns per line.
     * @return a <code>StringBuffer</code> value.
     */
    public static StringBuffer formatQualifierBlock(final String text,
						    final String leader,
						    final int    wrapWidth)
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

    /**
     * <code>formatQualifier</code> creates a qualifier string and
     * adds quotes or parens to EMBL/Genbank qualifier values as
     * specified in the internally loaded XML feature table
     * description.
     *
     * @param key an <code>Object</code> value (the qualifier key).
     * @param value an <code>Object</code> value (the qualifier content).
     *
     * @return a <code>String</code> bounded by the correct tokens.
     */
    public static String formatQualifier(final Object key,
					 final Object value)
    {
	StringBuffer tb = new StringBuffer("/" + key);

	// Default is to quote unknown qualifiers
	String form = "quoted";
	if (QUALIFIER_DATA.containsKey(key))
            form = (String) ((Map) QUALIFIER_DATA.get(key)).get("form");

	// This is a slight simplification. There are some types of
        // qualifier which are unquoted unless they contain
        // spaces. We all love special cases, don't we?
        if (form.equals("quoted"))
            tb.append("=\"" + value + "\"");
        else if (form.equals("bare"))
            tb.append("=" + value);
        else if (form.equals("paren"))
            tb.append("(" + value + ")");
        else if (! form.equals("empty"))
	{
            System.err.println("Unrecognised qualifier format: " + form);
	    tb.append("=" + value);
	}

        return tb.toString();
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
    public static StringBuffer formatTokenBlock(final Symbol [] syms,
						final int       blockSize)
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

    /**
     * <code>formatLocationBlock</code> creates an EMBL/Genbank style
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
     * </pre>
     *
     * Specifically not supported are:
     * <pre>
     *   123^567
     *   AL123465:(123..567)
     * </pre>
     *
     * Use of 'order' rather than 'join' is not retained over a
     * read/write cycle. i.e. 'order' is converted to 'join'.
     *
     * @param loc a <code>Location</code> object to use as a template.
     * @param strand an <code>int</code> value indicating the
     * <code>Location</code>'s strand.
     * @param leader a <code>String</code> to append to the start of
     * each line.
     * @param wrapWidth an <code>int</code> indicating the number of
     * columns per line.
     *
     * @return a <code>StringBuffer</code>.
     */
    public static StringBuffer formatLocationBlock(final Location loc,
						   final int      strand,
						   final String   leader,
						   final int      wrapWidth)
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

	/* There are issues here about choosing various forms:
	 * join(complement(...),complement(...))
	 * complement(join(...,...))
	 * 
	 * The former has the locations sorted in reverse order.
	 */

	Collections.sort(locs, Location.naturalOrder);

	StringBuffer sb = new StringBuffer(leader);

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
	    else
		locType = RANGE;

	    switch (locType)
	    {
		case POINT:
		    PointLocation pl = (PointLocation) thisLoc;

		    sb.append(complement                    ?
			      toComplement(formatPoint(pl)) :
			      formatPoint(pl));
		    break;

		case FUZZY_RANGE:
		    FuzzyLocation fl = (FuzzyLocation) thisLoc;

		    sb.append(complement                         ?
			      toComplement(formatFuzzyRange(fl)) :
			      formatFuzzyRange(fl));
		    break;

		case FUZZY_POINT:
		    FuzzyPointLocation fpl = (FuzzyPointLocation) thisLoc;

		    sb.append(complement                          ?
			      toComplement(formatFuzzyPoint(fpl)) :
			      formatFuzzyPoint(fpl));
		    break;

		case RANGE:
		    RangeLocation rl = (RangeLocation) thisLoc;

		    sb.append(complement                    ?
			      toComplement(formatRange(rl)) :
			      formatRange(rl));
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
	    // If adding the ")" has made the line too long, move the
	    // last range to the next line
	    if ((position + 1) > wrapWidth)
	    {
		sb.insert((sb.length() - diff), "\n" + leader);
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
     * @param fl a <code>FuzzyLocation</code> object.
     *
     * @return a <code>String</code> representation of the location.
     */
    public static String formatFuzzyRange(final FuzzyLocation fl)
    {
	StringBuffer fb = new StringBuffer();

	if (! fl.hasBoundedMin())
	{
	    // <123
	    fb.append("<");
	    fb.append(fl.getMin());
	}
	else if (fl.getOuterMin() != fl.getInnerMin())
	{
	    // (123.567)
	    fb.append("(" + fl.getOuterMin());
	    fb.append(".");
	    fb.append(fl.getInnerMin() + ")");
	}
	else
	{
	    // 123
	    fb.append(fl.getMin());
	}

	fb.append("..");

	if (! fl.hasBoundedMax())
	{
	    // >567
	    fb.append(">");
	    fb.append(fl.getMax());
	}
	else if (fl.getInnerMax() != fl.getOuterMax())
	{
	    // (567.789)
	    fb.append("(" + fl.getInnerMax());
	    fb.append(".");
	    fb.append(fl.getOuterMax() + ")");
	}
	else
	{
	    // 567
	    fb.append(fl.getMax());
	}

	return fb.toString();
    }

    /**
     * <code>formatFuzzyPoint</code> creates an EMBL/Genbank style
     * String representation of a <code>FuzzyPointLocation</code>.
     *
     * @param fpl a <code>FuzzyPointLocation</code> object.
     *
     * @return a <code>String</code> representation of the location.
     */
    public static String formatFuzzyPoint(final FuzzyPointLocation fpl)
    {
	StringBuffer fb = new StringBuffer();

	if (! fpl.hasBoundedMin())
	{
	    // <123
	    fb.append("<");
	    fb.append(fpl.getMax());
	}
	else if (! fpl.hasBoundedMax())
	{
	    // >567
	    fb.append(">");
	    fb.append(fpl.getMin());
	}
	else
	{
	    // (567.789)
	    fb.append("(" + fpl.getMin());
	    fb.append(".");
	    fb.append(fpl.getMax() + ")");
	}

	return fb.toString();
    }

    /**
     * <code>formatRange</code> creates an EMBL/Genbank style String
     * representation of a <code>RangeLocation</code>.
     *
     * @param rl a <code>RangeLocation</code> object.
     *
     * @return a <code>String</code> representation of the location.
     */
    public static String formatRange(final RangeLocation rl)
    {
	StringBuffer fb = new StringBuffer();

	// 123..567
	fb.append(rl.getMin());
	fb.append("..");
	fb.append(rl.getMax());

	return fb.toString();
    }

    /**
     * <code>formatPoint</code> creates an EMBL/Genbank style String
     * representation of a <code>PointLocation</code>.
     *
     * @param pl a <code>PointLocation</code> object.
     *
     * @return a <code>String</code> representation of the location.
     */
    public static String formatPoint(final PointLocation pl)
    {
	return Integer.toString(pl.getMin());
    }

    /**
     * <code>toComplement</code> accepts an EMBL/Genbank style String
     * representation of a <code>Location</code> and returns the
     * complementary strand version.
     *
     * @param value a <code>String</code> value.
     *
     * @return a <code>String</code> representation of the
     * complementary strand location.
     */
    static String toComplement(final String value)
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
     * @param featureData a <code>Map</code> object to populate with
     * feature data.
     * @param qualifierData a <code>Map</code> object to populate with
     * qualifier data.
     */
    public static void loadFeatureData(final String featureDataFile,
				       final Map    featureData,
				       final Map    qualifierData)
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
	}
    }
}
