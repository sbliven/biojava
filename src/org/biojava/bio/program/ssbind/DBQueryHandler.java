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

import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.biojava.bio.BioException;

/**
 * <code>DBQueryHandler</code>s parse the query sequence name and
 * database name from the raw header output contained within the
 * RawOutput element of the Header element and inform the
 * <code>SearchContentHandler</code>. There should really be dedicated
 * Elements in the DTD for these data. The Fasta search parser fakes a
 * RawOutput element containing these values as a kludge until this is
 * resolved.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class DBQueryHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory DBQUERY_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new DBQueryHandler(context);
            }
        };

    private SeqSimilarityAdapter context;
    private StringBuffer         data;
    
    private int                  level = 0;

    /**
     * Creates a new <code>DBQueryHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    DBQueryHandler(SeqSimilarityAdapter context)
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

    public void endElement(String nsURI,
			   String localName,
			   String qName)
	throws SAXException
    {
	level--;

	if (level == 0)
	    setStringValue(data.toString());
    }

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
     *.
     * @exception SAXException if an error occurs.
     */
    private void setStringValue(String s) throws SAXException
    {
        StringTokenizer st = new StringTokenizer(s);

        String    query = null;
        String database = null;

        while (st.hasMoreTokens())
        {
            String t = st.nextToken();

            if (t.equals("Query="))
            {
                query = st.nextToken();
                continue;
            }
            else if (t.equals("Database:"))
            {
                database = st.nextToken();
                break;
            }
        }

        if (query == null)
            throw new SAXException("Unable to parse query sequence Id");
        else if (database == null)
            throw new SAXException("Unable to parse database name");

        try
        {
            context.scHandler.setQuerySeq(query);
            context.scHandler.setSubjectDB(database);
        }
        catch (BioException be)
        {
            be.printStackTrace();
        }
    }
}
