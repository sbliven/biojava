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
 * <code>AlignmentHandler</code>s collate subHit alignment data (query
 * and subject sequence aligmnent start and stop positions and tokens)
 * and inform the <code>SearchContentHandler</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public final class AlignmentHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory ALIGNMENT_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new AlignmentHandler(context);
            }
        };

    private SeqSimilarityAdapter context;
    private StringBuffer         data;
    private String               seqType;    
    private String               startPos;
    private String               endPos;

    private int                  level = 0;

    /**
     * Creates a new <code>AlignmentHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    AlignmentHandler(SeqSimilarityAdapter context)
    {
        super();
        this.context = context;

        data = new StringBuffer();
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
        level++;
        if (level > 1)
	    throw new SAXException("Found child element when expecting character data");

        seqType  = localName;
        startPos = attr.getValue("startPosition");
        endPos   = attr.getValue("stopPosition");
    }

    /**
     * <code>endElement</code> notifies of the end of an element.
     *
     * @param uri a <code>String</code>.
     * @param localName a <code>String</code>.
     * @param qName a <code>String</code>.
     *
     * @exception SAXException if an error occurs.
     */
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
     * <code>characters</code> notifies of character data.
     *
     * @param ch a <code>char []</code> array.
     * @param start an <code>int</code>.
     * @param length an <code>int</code>.
     *
     * @exception SAXException if an error occurs.
     */
    public void characters(char[] ch, int start, int length) 
        throws SAXException
    {
	data.append(ch, start, length);
    }

    /**
     * <code>setStringValue</code> informs the
     * <code>SearchContentHandler</code> of the data.
     *
     * @param s a <code>String</code>.
     *
     * @exception SAXException if an error occurs.
     */
    private void setStringValue(String s) throws SAXException
    {
        if (seqType.equals("QuerySequence"))
        {
            context.scHandler.addSubHitProperty("QuerySequence", s);
            context.scHandler.addSubHitProperty("QuerySequenceStart", startPos);
            context.scHandler.addSubHitProperty("QuerySequenceEnd", endPos);
        }
        else if (seqType.equals("HitSequence"))
        {
            context.scHandler.addSubHitProperty("HitSequence", s);
            context.scHandler.addSubHitProperty("HitSequenceStart", startPos);
            context.scHandler.addSubHitProperty("HitSequenceEnd", endPos);   
        }
    }
}
