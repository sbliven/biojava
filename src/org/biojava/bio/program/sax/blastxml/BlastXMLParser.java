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

package org.biojava.bio.program.sax.blastxml;

import org.biojava.bio.seq.io.game.ElementRecognizer;
import org.biojava.utils.stax.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * This class parses NCBI Blast XML output.
 *
 * @author David Huen
 */
public class BlastXMLParser
    extends StAXFeatureHandlerMod
{
    boolean firstTime = true;

    // constructor
    public BlastXMLParser()
    {
        // this is the base element class
        this.staxenv = this;
//        System.out.println("staxenv " + staxenv);
        // just set a DefaultHandler: does nothing worthwhile.
        this.listener = new DefaultHandler();
    }

    /**
     * sets the ContentHandler for this object
     */
    public void setContentHandler(org.xml.sax.ContentHandler listener)
    {
        this.listener = listener;
    }

    /**
     * we override the superclass startElement method so we can determine the
     * the start tag type and use it to set up delegation for the superclass.
     */
    public void startElement(
            String nsURI,
            String localName,
            String qName,
            Attributes attrs,
            DelegationManager dm)
        throws SAXException
    {
//        System.out.println("localName is " + localName);
        if (firstTime) {
            // what kind of tag do we have?
            if (localName.equals("BlastOutput")) {
                // this is a well-formed XML document from NCBI BLAST
                // pertaining to one search result
                super.addHandler(
                    new ElementRecognizer.ByLocalName("BlastOutput"),
                    new StAXHandlerFactory() {
                        public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                            return new BlastOutputHandler(staxenv);
                        }
                    }
                );
            }
            else if (localName.equals("blast_aggregate")) {
                // this is my phony aggregate document that exists to
                // legitimise otherwise ill-formed output from NCBI Blast
                super.addHandler(new ElementRecognizer.ByLocalName("blast_aggregate"),
                    new StAXHandlerFactory() {
                        public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                            return new BlastAggregator(staxenv);
                        }
                    }
                );
            }
            else {
                throw new SAXException("illegal element " + localName);
            }

            firstTime = false;

            // setup the root element of the output
            AttributesImpl bldscAttrs = new AttributesImpl();
            bldscAttrs.addAttribute("", "xmlns", "xmlns", CDATA, "");
            bldscAttrs.addAttribute(biojavaUri, "biojava", "xmlns:biojava", CDATA, "http://www.biojava.org");
            listener.startElement(biojavaUri, "BlastLikeDataSetCollection", biojavaUri + ":BlastLikeDataSetCollection", bldscAttrs);
        }

        // now invoke delegation
        super.startElement(nsURI, localName, qName, attrs, dm);
    }

    public void endElementHandler(
            String nsURI,
            String localName,
            String qName,
            StAXContentHandler handler)
             throws SAXException
    {
        listener.endElement(biojavaUri, "BlastLikeDataSetCollection", biojavaUri + ":BlastLikeDataSetCollection");
    }
}
