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
 * <code>HitDescHandler</code>s collate the hit description lines and
 * inform the <code>SearchContentHandler</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class HitDescHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory HIT_DESC_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new HitDescHandler(context);
            }
        };

    private SeqSimilarityAdapter context;
    private StringBuffer         data;

    private int                  level = 0;

    /**
     * Creates a new <code>HitDescHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    HitDescHandler(SeqSimilarityAdapter context)
    {
        super();
        this.context = context;

        data = new StringBuffer();
    }

    public void startElement(String     uri,
                             String     localName,
                             String     qName,
                             Attributes attr)
        throws SAXException
    {
        level++;
        if (level > 1)
	    throw new SAXException("Found child element when expecting character data");
    }

    public void characters(char[] ch, int start, int length) 
        throws SAXException
    {
	data.append(ch, start, length);
    }

    public void endElement(String nsURI,
			   String localName,
			   String qName)
	throws SAXException
    {
	level--;

	if (level == 0)
	    setStringValue(data.toString());
    }

    /**
     * <code>setStringValue</code> informs the
     * <code>SearchContentHandler</code> of the data.
     *
     * @param s a <code>String</code>.
     *
     * @exception SAXException if an error occurs.
     */
    protected void setStringValue(String s) throws SAXException
    {
        context.scHandler.addHitProperty("desc", s);
    }
}
