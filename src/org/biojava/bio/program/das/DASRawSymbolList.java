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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.biojava.bio.BioException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.seq.io.SimpleSequenceBuilder;
import org.biojava.bio.seq.io.StreamParser;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Unchangeable;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.SAX2StAXAdaptor;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
        return getRawSymbols().getAlphabet();
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

        String seqRequest = "dna";
        if (DASCapabilities.checkCapable(new URL(sequence.getDataSourceURL(), ".."),
                                         DASCapabilities.CAPABILITY_SEQUENCE))
        {
            seqRequest = "sequence";
        }
        StringBuffer qb = new StringBuffer();
        qb.append(seqRequest);
        qb.append("?segment=");
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
                StAXContentHandler seqHandler = new SequenceHandler(sb, seqRequest);

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
                parser.setContentHandler(new SAX2StAXAdaptor(seqHandler));
                parser.parse(is);
                rawSymbols = sb.makeSequence();
            } catch (SAXException ex) {
                throw new BioRuntimeException("Exception parsing DAS XML", ex);
            } catch (IOException ex) {
                throw new BioRuntimeException("Error connecting to DAS server", ex);
            } catch (BioException ex) {
                throw new BioRuntimeException(ex);
            } finally {
                DAS.completedActivity(this);
            }
        }

        return rawSymbols;
    }

    private class SequenceHandler extends StAXContentHandlerBase {
        private SequenceBuilder sbuilder;
        private String requestType;

        SequenceHandler(SequenceBuilder sbuilder, String requestType) {
            this.sbuilder = sbuilder;
            this.requestType = requestType;
        }

        public void startElement(String nsURI,
                                                 String localName,
                                 String qName,
                                 Attributes attrs,
                                 DelegationManager dm)
                throws SAXException
            {
            if (localName.equals("DNA")) {
                SymbolTokenization toke;
                try {
                    toke = DNATools.getDNA().getTokenization("token");
                } catch (Exception ex) {
                    throw new SAXException("Couldn't get DNA tokenization");
                }
                StreamParser sparser = toke.parseStream(sbuilder);
                dm.delegate(new SymbolsHandler(sparser));
            } else if ("sequence".equals(requestType) && "SEQUENCE".equals(localName)) {
                String moltype = attrs.getValue("moltype");
                Alphabet alpha = null;
                if ("DNA".equalsIgnoreCase(moltype)) {
                    alpha = DNATools.getDNA();
                } else if ("ssRNA".equalsIgnoreCase(moltype) || "dsRNA".equalsIgnoreCase(moltype)) {
                    alpha = RNATools.getRNA();
                } else if ("Protein".equalsIgnoreCase(moltype)) {
                    alpha = ProteinTools.getTAlphabet();
                } else {
                    throw new SAXException("Unknown moltype " + moltype);
                }
                SymbolTokenization toke;
                try {
                    toke = alpha.getTokenization("token");
                } catch (Exception ex) {
                    throw new SAXException("Couldn't get tokenization for " + moltype);
                }
                StreamParser sparser = toke.parseStream(sbuilder);
                dm.delegate(new SymbolsHandler(sparser));
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
