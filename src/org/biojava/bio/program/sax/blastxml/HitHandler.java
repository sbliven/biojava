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

import java.util.*;

/**
 * @author Matthew Pocock
 */
class HitHandler
    extends StAXFeatureHandler
{
    // create static factory class that makes an instance
    // of this class.
    public final static StAXHandlerFactory HIT_HANDLER_FACTORY
             =
        new StAXHandlerFactory() {
            public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                return new HitHandler(staxenv);
            }
        };

    // class variables
    boolean startHitElementFired = false;

    // constructor
    public HitHandler(StAXFeatureHandler staxenv)
    {
        super(staxenv);

//        // acquire value of <Hit_num> with inner class.
//        super.addHandler(new ElementRecognizer.ByLocalName("Hit_num"),
//            HitPropertyHandler.HIT_PROPERTY_HANDLER_FACTORY);

        // acquire value of <Hit_id> with inner class.
        super.addHandler(new ElementRecognizer.ByLocalName("Hit_id"),
            new StAXHandlerFactory() {
                public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                    return new StringElementHandlerBase() {

                        AttributesImpl hitIdAttrs = null;

                        public void startElement(
                            String nsURI,
                            String localName,
                            String qName,
                            Attributes attrs,
                            DelegationManager dm)
                            throws SAXException
                        {
                            // generate start of containing element if required
                            if (!startHitElementFired) {
                                listener.startElement(biojavaUri, "Hit", biojavaUri + ":Hit", new AttributesImpl());
                                startHitElementFired = true;
                            }

                            // now generate my own start element
                            super.startElement(nsURI, localName, qName, attrs, dm);

                            // generate start of <biojava:HitDescription>
                            hitIdAttrs = new AttributesImpl();
                        }

                        public void setStringValue(String s) {
                            hitIdAttrs.addAttribute(biojavaUri, "id", biojavaUri + ":id", CDATA, s.trim());
                        }

                        public void endElement(
                            String nsURI,
                            String localName,
                            String qName,
                            StAXContentHandler handler)
                            throws SAXException
                        {
                            super.endElement(nsURI, localName, qName, handler);

                            // create <biojava:HitId>
                            hitIdAttrs.addAttribute(biojavaUri, "metaData", biojavaUri + ":metaData", CDATA, "none");
                            listener.startElement(biojavaUri, "HitId", biojavaUri + ":HitId", hitIdAttrs);
                            listener.endElement(biojavaUri, "HitId", biojavaUri + ":HitId");
                        }
                    };
                }
            }
        );

        // acquire value of <Hit_def> with inner class.
        super.addHandler(new ElementRecognizer.ByLocalName("Hit_def"),
            new StAXHandlerFactory() {
                public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                    return new StringElementHandlerBase() {
                        public void startElement(
                            String nsURI,
                            String localName,
                            String qName,
                            Attributes attrs,
                            DelegationManager dm)
                            throws SAXException
                        {
                            // generate start of containing element if required
                            if (!startHitElementFired) {
                                listener.startElement(biojavaUri, "Hit", biojavaUri + ":Hit", new AttributesImpl());
                                startHitElementFired = true;
                            }

                            // now generate my own start element
                            super.startElement(nsURI, localName, qName, attrs, dm);

                            // generate start of <biojava:HitDescription>
                            listener.startElement(biojavaUri, "HitDescription", biojavaUri + ":HitDescription", new AttributesImpl());
                        }

                        public void setStringValue(String s)  throws SAXException {
                            listener.characters(s.toCharArray(), 0, s.trim().length());
                        }

                        public void endElement(
                            String nsURI,
                            String localName,
                            String qName,
                            StAXContentHandler handler)
                            throws SAXException
                        {
                            super.endElement(nsURI, localName, qName, handler);

                            listener.endElement(biojavaUri, "HitDescription", biojavaUri + ":HitDescription");
                        }
                    };
                }
            }
        );

        // handle <Hit_hsps> with its own handler.
        // the handling here is a tad perverse in that the sequence length has 
        // to be saved as an attribute of <Hit> although it is present as
        // an element.  This would mean that it cannot be created in the
        // startElementHandler.  Creating it in the endElementHandler would
        // cause it to fail to contain the child elements correctly.
        super.addHandler(new ElementRecognizer.ByLocalName("Hit_hsps"),
            new StAXHandlerFactory() {
                public StAXContentHandler getHandler(StAXFeatureHandler staxenv) {
                    return new HitHspsHandler(staxenv) {
                        public void startElementHandler(
                            String nsURI,
                            String localName,
                            String qName,
                            Attributes attrs)
                            throws SAXException
                        {
                            // generate start of containing element if required
                            if (!startHitElementFired) {
                                listener.startElement(biojavaUri, "Hit", biojavaUri + ":Hit", new AttributesImpl());
                                startHitElementFired = true;
                            }

                            // now I generate my own start element
                            super.startElementHandler(nsURI, localName, qName, attrs);
                        }
                    };
                }            
            }
        );
    }

    public void endElementHandler(
            String nsURI,
            String localName,
            String qName,
            StAXContentHandler handler)
             throws SAXException
    {
        listener.endElement(biojavaUri, "Hit", biojavaUri + ":Hit");
    }    
}
