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
import org.xml.sax.SAXException;

import org.biojava.bio.BioException;
import org.biojava.bio.program.xff.ElementRecognizer;
import org.biojava.bio.search.SearchContentHandler;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.biojava.utils.stax.StringElementHandlerBase;

/**
 * <code>HeaderStAXHandler</code> handles the Header element of
 * BioJava BlastLike XML.
 *
 * @author Keith James
 * @author Matthew Pocock
 * @since 1.3
 */
public class HeaderStAXHandler extends SeqSimilarityStAXHandler
{
    public static final StAXHandlerFactory HEADER_HANDLER_FACTORY =
        new StAXHandlerFactory()
        {
            public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
            {
                return new HeaderStAXHandler(ssContext);
            }
        };

    /**
     * Creates a new instance which sends callbacks to the specified
     * <code>SeqSimilarityStAXAdapter</code>.
     *
     * @param ssContext a <code>SeqSimilarityStAXAdapter</code>.
     */
    HeaderStAXHandler(SeqSimilarityStAXAdapter ssContext)
    {
        super(ssContext);
        addHandler(new ElementRecognizer.ByNSName(SeqSimilarityStAXAdapter.NAMESPACE,
                                                  "RawOutput"),
                   new StAXHandlerFactory()
                   {
                       public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
                       {
                           return new BlastDBQueryStAXHandler();
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
                                                  "DatabaseId"),
                   new StAXHandlerFactory()
                   {
                       public StAXContentHandler getHandler(SeqSimilarityStAXAdapter ssContext)
                       {
                           return new DatabaseIdStAXHandler();
                       }
                   });
    }

    /**
     * <code>BlastDBQueryStAXHandler</code> workaround class which
     * parses DB and Query info from the RawOutput element. The DTD
     * has specific elements for these data, but the current parser
     * does not populate them. When those elements are present, the
     * other two handlers in the enclosing class will be used.
     */
    private class BlastDBQueryStAXHandler extends StringElementHandlerBase
    {
        private SearchContentHandler sch;

        protected void setStringValue(String s) throws SAXException
        {
            // Check that we are dealing with Blast output
            String program = ssContext.getProgram().toUpperCase();

            if (program.indexOf("BLAST") == -1 )
                return;

            sch = ssContext.getSearchContentHandler();

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
                    continue;
                }
            }

            if (query == null)
                throw new SAXException("Failed to parse query sequence ID");
            else if (database == null)
                throw new SAXException("Failed to parse database ID");

            try
            {
                sch.setQuerySeq(query);
            }
            catch (BioException be)
            {
                throw new SAXException("Received a query sequence ID which fails: "
                                       + be.getMessage());
            }

            try
            {
                sch.setSubjectDB(database);
            }
            catch (BioException be)
            {
                throw new SAXException("Received a database ID which fails: "
                                       + be.getMessage());
            }
        }
    }

    /**
     * <code>QueryIdStAXHandler</code> handles the query sequence ID.
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
     * <code>DatabaseIdStAXHandler</code> handles the database ID.
     */
    private class DatabaseIdStAXHandler extends StAXContentHandlerBase
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
                 ssContext.getSearchContentHandler().setSubjectDB(attr.getValue("id"));
             }
             catch (BioException be)
             {
                 throw new SAXException("Received a database ID which fails: "
                                        + be.getMessage());
             }
        }
    }
}
