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
 * <code>HitIdHandler</code>s collate hit database IDs and inform the
 * <code>SearchContentHandler</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.2
 */
public class HitIdHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory HIT_ID_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new HitIdHandler(context);
            }
        };

    private SeqSimilarityAdapter context;

    /**
     * Creates a new <code>HitIdHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    HitIdHandler(SeqSimilarityAdapter context)
    {
        super();
        this.context = context;
    }

    public void startElement(String     uri,
                             String     localName,
                             String     qName,
                             Attributes attr)
        throws SAXException
    {
        context.scHandler.addHitProperty("HitId", attr.getValue("id"));
    }
}
