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

package org.biojava.bio.program.sax;

import java.util.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import org.biojava.bio.BioException;
import org.biojava.utils.ParserException;

import org.biojava.bio.program.search.SearchParser;
import org.biojava.bio.program.search.SearchContentHandler;
import org.biojava.bio.program.search.FastaSearchParser;
import org.biojava.bio.program.search.SearchContentHandler;

/**
 * <code>FastaSearchSAXParser</code> preliminary implementation.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class FastaSearchSAXParser extends AbstractNativeAppSAXParser
    implements SearchContentHandler
{
    private SearchParser  fastaParser;
    private Map      searchProperties;
    private Map         hitProperties;

    private String  querySeqIdentifier;
    private String subjectDBIdentifier;

    private AttributesImpl attributes;
    private QName               qName;

    private boolean    firstHit = true;

    // For formatting rounded numbers
    private NumberFormat nFormat;

    // For creating character events
    private String  stringOut;
    private char []   charOut;

    /**
     * Creates a new <code>FastaSearchSAXParser</code> instance.
     *
     */
    public FastaSearchSAXParser()
    {
	this.setNamespacePrefix("biojava");
	this.addPrefixMapping("biojava", "http://www.biojava.org");

	fastaParser = new FastaSearchParser();
	attributes  = new AttributesImpl();
	qName       = new QName(this);
	nFormat     = new DecimalFormat("###.0");
    }

    public void parse(final InputSource source) throws IOException, SAXException
    {
	BufferedReader content = getContentStream(source);

	if (oHandler == null)
	    throw new SAXException("Running FastaSearchSAXParser with null ContentHandler");

	try
	{
	    attributes.clear();
	    // Namespace attribute
	    qName.setQName("xmlns");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    "");
	    // Namespace attribute
	    qName.setQName("xmlns:biojava");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    "http://www.biojava.org");

	    // Start the BlastLikeDataSetCollection
	    startElement(new QName(this, this.prefix("BlastLikeDataSetCollection")),
			 (Attributes) attributes);

	    boolean moreSearchesAvailable = true;

	    while (moreSearchesAvailable)
	    {
		moreSearchesAvailable = fastaParser.parseSearch(content, this);
	    }

	    // End the BlastLikeDataSetCollection
	    endElement(new QName(this, this.prefix("BlastLikeDataSetCollection")));
	}
	catch (BioException bex)
	{
	    throw new SAXException(bex);
	}
	catch (ParserException pex)
	{
	    throw new SAXException(pex);
	}
    }

    public void setQuerySeq(String identifier)
    {
	querySeqIdentifier = identifier;
    }

    public void setSubjectDB(String identifier)
    {
	subjectDBIdentifier = identifier;
    }

    public void startSearch()
    {
	searchProperties = new HashMap();
    }

    public void addSearchProperty(final Object key, final Object value)
    {
	searchProperties.put(key, value);
    }

    public void endSearch()
    {
	try
	{
	    // If we found any hits then we need to close a Detail
	    // element too
	    if (! firstHit)
	    {
		endElement(new QName(this, this.prefix("Detail")));

		// Prime to get next firstHit
		firstHit = true;
	    }

	    endElement(new QName(this, this.prefix("BlastLikeDataSet")));
	}
	catch (SAXException se)
	{
	    se.printStackTrace();
	}
    }

    public void startHeader() { }

    public void endHeader()
    {
	try
	{
	    attributes.clear();
	    // Program name attribute
	    qName.setQName("program");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    (String) searchProperties.get("pg_name"));

	    // Program version attribute
	    qName.setQName("version");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    (String) searchProperties.get("pg_ver"));

	    // Start the BlastLikeDataSet
	    startElement(new QName(this, this.prefix("BlastLikeDataSet")),
			 (Attributes) attributes);

	    attributes.clear();
	    // Start the Header
	    startElement(new QName(this, this.prefix("Header")),
			 (Attributes) attributes);

	    // Whitespace attribute for raw data
	    qName.setQName("xml:space");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    "preserve");

	    // Start the RawOutput
	    startElement(new QName(this, this.prefix("RawOutput")),
			 (Attributes) attributes);

	    // Reconstitute the 'raw' header from the properties Map
	    String [] searchPropKeys =
		(String []) searchProperties.keySet().toArray(new String [0]);
	    Arrays.sort(searchPropKeys);

	    StringBuffer props = new StringBuffer(2048);

	    props.append("\n");
	    for (int i = 0; i < searchPropKeys.length; i++)
	    {
		props.append(searchPropKeys[i] + ": ");
		props.append((String) searchProperties.get(searchPropKeys[i]) + "\n");
	    }

	    charOut = new char [props.length()];
	    props.getChars(0, props.length(), charOut, 0);

	    // Characters of raw header
	    characters(charOut, 0, charOut.length);

	    // End the RawOutput
	    endElement(new QName(this, this.prefix("RawOutput")));

	    // End the Header
	    endElement(new QName(this, this.prefix("Header")));
	}
	catch (SAXException se)
	{
	    se.printStackTrace();
	}
    }

    public void startHit()
    {
	// Hit elements must be wrapped in a Detail element so we
	// start one at the first hit
	if (firstHit)
	{
	    firstHit = false;
	    attributes.clear();

	    try
	    {
		startElement(new QName(this, this.prefix("Detail")),
			     (Attributes) attributes);
	    }
	    catch (SAXException se)
	    {
		se.printStackTrace();
	    }
	}

	hitProperties = new HashMap();
    }

    public void addHitProperty(final Object key, final Object value)
    {
	hitProperties.put(key, value);
    }

    public void endHit() { }

    public void startSubHit() { }

    public void addSubHitProperty(final Object key, final Object value)
    {
	hitProperties.put(key, value);
    }

    public void endSubHit()
    {
	// System.out.println("Using: " + hitProperties);

	attributes.clear();
	// Query sequence length attribute
	qName.setQName("sequenceLength");
	attributes.addAttribute(qName.getURI(),
				qName.getLocalName(),
				qName.getQName(),
				"CDATA",
				hitProperties.get("query_sq_len").toString());

	try
	{
	    // Start the Hit
	    startElement(new QName(this, this.prefix("Hit")),
			 (Attributes) attributes);

	    attributes.clear();
	    // Hit id attribute
	    qName.setQName("id");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    hitProperties.get("id").toString());
	    // Metadata attribute
	    qName.setQName("metaData");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    "none");
	    // Start the HitId
	    startElement(new QName(this, this.prefix("HitId")),
			 (Attributes) attributes);

	    // End the HitId
	    endElement(new QName(this, this.prefix("HitId")));

	    attributes.clear();
	    // Start the HitDescription
	    startElement(new QName(this, this.prefix("HitDescription")),
			 (Attributes) attributes);

	    stringOut = (String) hitProperties.get("desc");

	    charOut = new char [stringOut.length()];
	    stringOut.getChars(0, stringOut.length(), charOut, 0);

	    // Characters of description
	    characters(charOut, 0, charOut.length);

	    // End the HitDescription
	    endElement(new QName(this, this.prefix("HitDescription")));

	    // Start the HSPCollection
	    startElement(new QName(this, this.prefix("HSPCollection")),
			 (Attributes) attributes);

	    // Start the HSP (for Fasta, we use one "HSP" to represent the hit
	    startElement(new QName(this, this.prefix("HSP")),
			 (Attributes) attributes);

	    String score;
	    if (hitProperties.containsKey("fp_score"))
		score = hitProperties.get("fp_score").toString();
	    else if (hitProperties.containsKey("sw_score"))
		score = hitProperties.get("sw_score").toString();
	    else
		score = "none";

	    // Score attribute
	    qName.setQName("score");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    score);
	    // expectValue attribute
	    qName.setQName("expectValue");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    hitProperties.get("fa_expect").toString());
	    // numberOfIdentities attribute
	    qName.setQName("numberOfIdentities");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    countTokens(':', (String) hitProperties.get("matchTokens")));

	    String overlap;
	    if (hitProperties.containsKey("fa_overlap"))
		overlap = hitProperties.get("fa_overlap").toString();
	    else
		overlap = hitProperties.get("sw_overlap").toString();

	    // alignmentSize attribute
	    qName.setQName("alignmentSize");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    overlap);

	    float percentId;
	    if (hitProperties.containsKey("fa_ident"))
		percentId = ((Float) hitProperties.get("fa_ident")).floatValue();
	    else
	 	percentId = ((Float) hitProperties.get("sw_ident")).floatValue();

	    // percentageIdentity attribute
	    qName.setQName("percentageIdentity");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    nFormat.format(percentId * 100));

	    // Start the HSPSummary
	    startElement(new QName(this, this.prefix("HSPSummary")),
			 (Attributes) attributes);

	    attributes.clear();
	    // Start the RawOutput
	    startElement(new QName(this, this.prefix("RawOutput")),
			 (Attributes) attributes);

	    // Reconstitute the 'raw' header from the properties Map
	    String [] hitPropKeys =
		(String []) hitProperties.keySet().toArray(new String [0]);
	    Arrays.sort(hitPropKeys);

	    StringBuffer props = new StringBuffer(2048);

	    props.append("\n");
	    for (int i = 0; i < hitPropKeys.length; i++)
	    {
		// Skip the sequence and consensus tokens
		if (((String) hitPropKeys[i]).endsWith("Tokens"))
		    continue;
		props.append(hitPropKeys[i] + ": ");
		props.append(hitProperties.get(hitPropKeys[i]).toString() + "\n");
	    }

	    charOut = new char [props.length()];
	    props.getChars(0, props.length(), charOut, 0);

	    // Characters of raw header
	    characters(charOut, 0, charOut.length);

	    // End the RawOutput
	    endElement(new QName(this, this.prefix("RawOutput")));

	    // End the HSPSummary
	    endElement(new QName(this, this.prefix("HSPSummary")));

	    // Start the BlastLikeAlignment
	    startElement(new QName(this, this.prefix("BlastLikeAlignment")),
			 (Attributes) attributes);

	    // Query sequence startPosition attribute
	    qName.setQName("startPosition");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    hitProperties.get("query_al_start").toString());

	    // Query sequence stopPosition attribute
	    qName.setQName("stopPosition");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    hitProperties.get("query_al_stop").toString());

	    // Start the QuerySequence
	    startElement(new QName(this, this.prefix("QuerySequence")),
			 (Attributes) attributes);
	    
	    stringOut = (String) hitProperties.get("querySeqTokens");

	    charOut = new char [stringOut.length()];
	    stringOut.getChars(0, stringOut.length(), charOut, 0);

	    // Characters of QuerySequence
	    characters(charOut, 0, charOut.length);

	    // End the QuerySequence
	    endElement(new QName(this, this.prefix("QuerySequence")));

	    attributes.clear();
	    // Whitespace attribute for MatchConsensus
	    qName.setQName("xml:space");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    "preserve");

	    // Start the MatchConsensus
	    startElement(new QName(this, this.prefix("MatchConsensus")),
			 (Attributes) attributes);

	    stringOut = (String) hitProperties.get("matchTokens");

	    charOut = new char [stringOut.length()];
	    stringOut.getChars(0, stringOut.length(), charOut, 0);

	    // Characters of MatchConsensus
	    characters(charOut, 0, charOut.length);

	    // End the MatchConsensus
	    endElement(new QName(this, this.prefix("QuerySequence")));

	    attributes.clear();
	    // Hit sequence startPosition attribute
	    qName.setQName("startPosition");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    hitProperties.get("subject_al_start").toString());

	    // Hit sequence stopPosition attribute
	    qName.setQName("stopPosition");
	    attributes.addAttribute(qName.getURI(),
				    qName.getLocalName(),
				    qName.getQName(),
				    "CDATA",
				    hitProperties.get("subject_al_stop").toString());

	    // Start the HitSequence
	    startElement(new QName(this, this.prefix("HitSequence")),
			 (Attributes) attributes);

	    stringOut = (String) hitProperties.get("subjectSeqTokens");

	    charOut = new char [stringOut.length()];
	    stringOut.getChars(0, stringOut.length(), charOut, 0);

	    // Characters of HitSequence
	    characters(charOut, 0, charOut.length);

	    // End the HitSequence
	    endElement(new QName(this, this.prefix("HitSequence")));

	    // End the BlastLikeAlignment
	    endElement(new QName(this, this.prefix("BlastLikeAlignment")));

	    // End the HSP
	    endElement(new QName(this, this.prefix("HSP")));

	    // End the HSPCollection
	    endElement(new QName(this, this.prefix("HSPCollection")));

	    // End the hit
	    endElement(new QName(this, this.prefix("Hit")));
	}
	catch (SAXException se)
	{
	    se.printStackTrace();
	}
    }

    /**
     * <code>countTokens</code> counts up the occurrences of a char in
     * a <code>String</code>.
     *
     * @param token a <code>char</code> to count.
     * @param string a <code>String</code> to count within.
     *
     * @return a <code>String</code> representation of the total count.
     */
    private String countTokens(final char token, final String string)
    {
	int count = 0;
	for (int i = 0; i < string.length(); i++)
	{
	    if (string.charAt(i) == token)
		count++;
	}
	return String.valueOf(count);
    }
}
