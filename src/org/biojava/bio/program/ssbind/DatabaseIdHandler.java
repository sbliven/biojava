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

import org.biojava.bio.BioException;

/**
 * <code>DatabaseIdHandler</code>s extract the database name/ID and
 * inform the <code>SearchContentHandler</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.2
 */
public class DatabaseIdHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory DATABASE_ID_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new DatabaseIdHandler(context);
            }
        };

    private SeqSimilarityAdapter context;

    /**
     * Creates a new <code>DatabaseIdHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    DatabaseIdHandler(SeqSimilarityAdapter context)
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
        try
        {
            context.scHandler.setSubjectDB(attr.getValue("id"));
        }
        catch (BioException be)
        {
            throw new SAXException("Received a database ID which fails: "
                                   + be.getMessage());
        }
    }
}
