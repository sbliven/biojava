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

package org.biojava.bio.program.ssbind;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.biojava.bio.BioException;
import org.biojava.bio.search.SearchContentHandler;
import org.biojava.bio.program.xff.ElementRecognizer;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.biojava.utils.stax.StringElementHandlerBase;

/**
 * <code>AlignmentStAXHandler</code> handles the Hit element of
 * BioJava BlastLike XML.
 *
 * @author Keith James
 */
public class HitStAXHandler extends SeqSimilarityStAXHandler
{
    public static final StAXHandlerFactory HIT_HANDLER_FACTORY =
        new StAXHandlerFactory()
        {
            public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
            {
                return new HitStAXHandler(ssContext);
            }
        };

    /**
     * Creates a new instance which sends callbacks to the specified
     * <code>SeqSimilarityStAXAdapter</code>.
     *
     * @param ssContext a <code>SeqSimilarityStAXAdapter</code>.
     */
    HitStAXHandler(SeqSimilarityStAXAdapter ssContext)
    {
        super(ssContext);
        addHandler(new ElementRecognizer.ByNSName(SeqSimilarityStAXAdapter.NAMESPACE,
                                                  "HitId"),
                   new StAXHandlerFactory()
                   {
                       public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
                       {
                           return new HitIdStAXHandler();
                       }
                   });

        addHandler(new ElementRecognizer.ByNSName(SeqSimilarityStAXAdapter.NAMESPACE,
                                                  "QueryId"),
                   new StAXHandlerFactory()
                   {
                       public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
                       {
                           return new QueryIdStAXHandler();
                       }
                   });

        addHandler(new ElementRecognizer.ByNSName(SeqSimilarityStAXAdapter.NAMESPACE,
                                                  "HitDescription"),
                   new StAXHandlerFactory()
                   {
                       public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
                       {
                           return new HitDescriptionStAXHandler();
                       }
                   });

        addHandler(new ElementRecognizer.ByNSName(SeqSimilarityStAXAdapter.NAMESPACE,
                                                  "HSPCollection"),
                   new StAXHandlerFactory()
                   {
                       public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
                       {
                           return new HSPCollectionStAXHandler();
                       }
                   });
    }

    protected void handleStartElement(String     nsURI,
                                      String     localName,
                                      String     qName,
                                      Attributes attrs)
        throws SAXException
    {
        SearchContentHandler sch = ssContext.getSearchContentHandler();

        sch.startHit();
        if (attrs.getValue("sequenceLength") != null)
        {
            sch.addHitProperty("subjectSequenceLength",
                               attrs.getValue("sequenceLength"));
        }
    }

    protected void handleEndElement(String     nsURI,
                                    String     localName,
                                    String     qName)
        throws SAXException
    {
        ssContext.getSearchContentHandler().endHit();
    }

    /**
     * <code>HitIdStAXHandler</code> handles the hit ID.
     *
     */
    private class HitIdStAXHandler extends StAXContentHandlerBase
    {
        public void startElement(String            uri,
                                 String            localName,
                                 String            qName,
                                 Attributes        attr,
                                 DelegationManager dm)
        throws SAXException
        {
            ssContext.getSearchContentHandler().addHitProperty("HitId", attr.getValue("id"));
        }
    }

    /**
     * <code>QueryIdStAXHandler</code> handles the query ID.
     *
     */
    private class QueryIdStAXHandler extends StAXContentHandlerBase
    {
        public void startElement(String            uri,
                                 String            localName,
                                 String            qName,
                                 Attributes        attr,
                                 DelegationManager dm)
        throws SAXException
        {
             try
             {
                 ssContext.getSearchContentHandler().setQuerySeq(attr.getValue("id"));
             }
             catch (BioException be)
             {
                 throw new SAXException("Received a query sequence ID which fails: "
                                        + be.getMessage());
             }
        }
    }

    /**
     * <code>HitDescriptionStAXHandler</code> handles the hit
     * description.
     */
    private class HitDescriptionStAXHandler extends StringElementHandlerBase
    {
        protected void setStringValue(String s)
        {
            ssContext.getSearchContentHandler().addHitProperty("desc", s);
        }
    }

    /**
     * <code>HSPCollectionStAXHandler</code> handles the HSPCollection
     * element.
     */
    private class HSPCollectionStAXHandler extends StAXContentHandlerBase
    {
        public void startElement(String            uri,
                                 String            localName,
                                 String            qName,
                                 Attributes        attr,
                                 DelegationManager dm)
        throws SAXException
        {
            if (localName.equals("HSP"))
                dm.delegate(HSPStAXHandler.HSP_HANDLER_FACTORY.getHandler(ssContext));
        }
    }
}
