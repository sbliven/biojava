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

package org.biojava.bio.program.das;

import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.biojava.utils.stax.*;

/**
 * A segment of DNA fetched for a DAS reference server.
 *
 * @author Thomas Down
 * @author Greg Cox
 * @since 1.2
 */

class DASRawSymbolList
  extends
    Unchangeable
  implements
    SymbolList
{
    private DASSequence sequence;
    private Segment segment;
    private SymbolList rawSymbols;

    DASRawSymbolList(DASSequence seq, Segment seg) {
	this.sequence = seq;
	this.segment = seg;
    }

    public Alphabet getAlphabet() {
	return DNATools.getDNA();
    }

    public int length() {
	if (segment.isBounded()) {
	    return segment.getStop() - segment.getStart() + 1;
	} else {
	    return getRawSymbols().length();
	}
    }

    public Iterator iterator() {
	return getRawSymbols().iterator();
    }

    public Symbol symbolAt(int i) {
	return getRawSymbols().symbolAt(i);
    }

    public SymbolList subList(int start, int end) {
	return getRawSymbols().subList(start, end);
    }

    public List toList() {
	return getRawSymbols().toList();
    }

    public String seqString() {
	return getRawSymbols().seqString();
    }

    public String subStr(int start, int end) {
	return getRawSymbols().subStr(start, end);
    }

    public void edit(Edit edit)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't edit sequence");
    }

    protected SymbolList getRawSymbols() {
	if (rawSymbols == null) {
	    try {
		DAS.startedActivity(this);

		StringBuffer qb = new StringBuffer();
		qb.append("dna?segment=");
		qb.append(segment.getID());
		if (segment.isBounded()) {
		    qb.append(':');
		    qb.append(segment.getStart());
		    qb.append(',');
		    qb.append(segment.getStop());
		}
		URL dnaURL = new URL(sequence.getDataSourceURL(), qb.substring(0));
		HttpURLConnection huc = (HttpURLConnection) dnaURL.openConnection();
		huc.setRequestProperty("Accept-Encoding", "gzip");

		huc.connect();
		// int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
		int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");

		if (status == 0)
		    throw new BioRuntimeException("Not a DAS server");
		else if (status != 200)
		    throw new BioRuntimeException("DAS error (status code = " + status + ")");

		SequenceBuilder sb = new SimpleSequenceBuilder();
		sb.setURI(dnaURL.toString());
		sb.setName(sequence.getName());
		SymbolTokenization sparser = DNATools.getDNA().getTokenization("token");
		StreamParser ssparser = sparser.parseStream(sb);

		StAXContentHandler dnaHandler = new DNAHandler(ssparser);

		// determine if I'm getting a gzipped reply
		String contentEncoding = huc.getContentEncoding();
		InputStream inStream = huc.getInputStream();

		if (contentEncoding != null) {
		    if (contentEncoding.indexOf("gzip") != -1) {
			// we have gzip encoding
			inStream = new GZIPInputStream(inStream);
			// System.out.println("gzip encoded dna!");
		    }
		}

		InputSource is = new InputSource(inStream);
		is.setSystemId(dnaURL.toString());
		XMLReader parser = DASSequence.nonvalidatingSAXParser();
		parser.setContentHandler(new SAX2StAXAdaptor(dnaHandler));
		parser.parse(is);
		rawSymbols = sb.makeSequence();
	    } catch (SAXException ex) {
		throw new BioRuntimeException(ex, "Exception parsing DAS XML");
	    } catch (IOException ex) {
		throw new BioRuntimeException(ex, "Error connecting to DAS server");
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    } finally {
		DAS.completedActivity(this);
	    }
	}

	return rawSymbols;
    }

    private class DNAHandler extends StAXContentHandlerBase {
	private StreamParser ssparser;

	DNAHandler(StreamParser ssparser) {
	    this.ssparser = ssparser;
	}

	public void startElement(String nsURI,
				 String localName,
				 String qName,
				 Attributes attrs,
				 DelegationManager dm)
	    throws SAXException
	{
	    if (localName.equals("DNA")) {
		dm.delegate(new SymbolsHandler(ssparser));
	    }
	}
    }

    private class SymbolsHandler extends StAXContentHandlerBase {
	private StreamParser ssparser;

	SymbolsHandler(StreamParser ssparser) {
	    this.ssparser = ssparser;
	}

	public void endElement(String nsURI,
			   String localName,
			   String qName,
			   StAXContentHandler handler)
	    throws SAXException
	{
	    try {
		ssparser.close();
	    } catch (IllegalSymbolException ex) {
		throw new SAXException(ex);
	    }
	}

	public void characters(char[] ch, int start, int length)
	    throws SAXException
	{
	    try {
		int parseStart = start;
		int parseEnd   = start;
		int blockEnd = start + length;

		while (parseStart < blockEnd) {
		    while (parseStart < blockEnd && Character.isWhitespace(ch[parseStart])) {
			++parseStart;
		    }
		    if (parseStart >= blockEnd) {
			return;
		    }

		    parseEnd = parseStart + 1;
		    while (parseEnd < blockEnd && !Character.isWhitespace(ch[parseEnd])) {
			++parseEnd;
		    }

		    ssparser.characters(ch, parseStart, parseEnd - parseStart);

		    parseStart = parseEnd;
		}
	    } catch (IllegalSymbolException ex) {
		throw new SAXException(ex);
	    }
	}
    }
}
