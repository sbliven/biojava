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
 * <code>BlastDBQueryHandler</code>s parse the query sequence name and
 * database name from the raw header output contained within the
 * RawOutput element of the Header element and inform the
 * <code>SearchContentHandler</code>. This means of setting the query
 * sequence name and database name will be deprecated if and when the
 * BlastLikeSAXParser begins to use the new QueryId and DatabaseId
 * elements recently added to the DTD.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class BlastDBQueryHandler extends DefaultHandler
{
    /**
     * Static factory to which creates new
     * <code>ContentHandler</code>s of this type.
     */
    public static final SSPropHandlerFactory BLAST_DBQUERY_HANDLER_FACTORY =
        new SSPropHandlerFactory()
        {
            public ContentHandler getHandler(SeqSimilarityAdapter context)
            {
                return new BlastDBQueryHandler(context);
            }
        };

    private SeqSimilarityAdapter context;
    private StringBuffer         data;
    
    private int                  level = 0;

    /**
     * Creates a new <code>BlastDBQueryHandler</code> object with a
     * reference to a parent context.
     *
     * @param context a <code>SeqSimilarityAdapter</code>.
     */
    BlastDBQueryHandler(SeqSimilarityAdapter context)
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
     *.
     * @exception SAXException if an error occurs.
     */
    private void setStringValue(String s) throws SAXException
    {
        // Check that we are dealing with Blast output
        String program = context.getProgram().toUpperCase();

        if (program.indexOf("BLAST") == -1 )
            return;

        StringTokenizer st = new StringTokenizer(s.trim());

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
            throw new SAXException("Failed to parse query sequence ID");
        else if (database == null)
            throw new SAXException("Failed to parse database ID");

        try
        {
            context.scHandler.setQuerySeq(query);
        }
        catch (BioException be)
        {
            throw new SAXException("Received a query sequence ID which fails: "
                                   + be.getMessage());
        }

        try
        {
            context.scHandler.setSubjectDB(database);
        }
        catch (BioException be)
        {
            throw new SAXException("Received a database ID which fails: "
                                   + be.getMessage());
        }
    }
}
