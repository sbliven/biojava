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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <code>SubHitSummaryHandler</code>s collate score, E-value, P-value
 * and alignment data from sub-hits and inform the
 * <code>SearchContentHandler</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SubHitSummaryHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory SUBHIT_SUMMARY_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new SubHitSummaryHandler(context);
            }
        };

    private SeqSimilarityAdapter context;

    /**
     * Creates a new <code>SubHitSummaryHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    SubHitSummaryHandler(SeqSimilarityAdapter context)
    {
        super();
        this.context = context;
    }

    /**
     * <code>startElement</code> notifies of the start of an element.
     *
     * @param uri a <code>String</code>.
     * @param localName a <code>String</code>.
     * @param qName a <code>String</code>.
     * @param attr an <code>Attributes</code> object.
     *
     * @exception SAXException if an error occurs.
     */
    public void startElement(String     uri,
                             String     localName,
                             String     qName,
                             Attributes attr)
        throws SAXException
    {
        context.scHandler.addSubHitProperty("score", attr.getValue("score"));

        if (attr.getValue("expectValue") != null)
            context.scHandler.addSubHitProperty("expectValue",
                                                attr.getValue("expectValue"));

        if (attr.getValue("pValue") != null)
            context.scHandler.addSubHitProperty("pValue",
                                                attr.getValue("pValue"));

        // These are only really important for Fasta as the sequence
        // types can be resolved from the program name for Blast
        if (attr.getValue("querySequenceType") != null)
            context.scHandler.addSubHitProperty("querySequenceType",
                                                attr.getValue("querySequenceType"));

        if (attr.getValue("hitSequenceType") != null)
            context.scHandler.addSubHitProperty("hitSequenceType",
                                                attr.getValue("hitSequenceType"));

        context.scHandler.addSubHitProperty("queryStrand", attr.getValue("queryStrand"));
        context.scHandler.addSubHitProperty("hitStrand", attr.getValue("hitStrand"));
    }
}
