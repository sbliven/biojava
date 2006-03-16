/*
 * BioSQLRichSequenceHandler.java
 *
 * Created on March 7, 2006, 3:12 PM
 */

package org.biojavax.bio.db.biosql;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceHandler;
import org.biojavax.bio.seq.RichLocation.Tools;
import org.biojava.bio.seq.io.ChunkedSymbolListFactory;
import org.biojava.bio.symbol.PackedSymbolListFactory;

/**
 * A handler which loads sequence data from a BioSQL database, caching it where possible.
 * Note that this data is read-only. If you want to modify it permanently, you must use
 * BioSQLRichSequenceDB.getRichSequence() to convert the original sequence into a proper
 * SimpleRichSequence.
 * @author Richard Holland
 */
public class BioSQLRichSequenceHandler implements RichSequenceHandler {
    
    // the Hibernate session.
    private Object session;
    private Class query;
    private Method createQuery;
    private Method setParameter;
    private Method uniqueResult;
    
    /**
     * Requires a Hibernate session to work correctly. The session parameter
     * is a Hibernate Session object and must not be null. It is this session
     * that database objects will be retrieved from/persisted to.
     * @see <a href="http://www.hibernate.org/hib_docs/v3/api/org/hibernate/Session.html"> org.hibernate.Session</a>
     */
    public BioSQLRichSequenceHandler(Object session) {
        try {
            // Lazy load the Session class from Hibernate.
            Class hibernateSession = Class.forName("org.hibernate.Session");
            // Test to see if our parameter is really a Session
            if (!hibernateSession.isInstance(session))
                throw new IllegalArgumentException("Parameter must be a org.hibernate.Session object");
            this.session = session;
            // Lookup the createQuery method
            this.createQuery = hibernateSession.getMethod("createQuery", new Class[]{String.class});
            // Lazy load the Query class from Hibernate.
            Class hibernateQuery = Class.forName("org.hibernate.Query");
            // Lookup the setParameter and uniqueQuery methods
            this.setParameter = hibernateQuery.getMethod("setParameter", new Class[]{int.class,Object.class});
            this.uniqueResult = hibernateQuery.getMethod("uniqueResult", new Class[]{});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void edit(RichSequence seq, Edit edit) throws IndexOutOfBoundsException, IllegalAlphabetException, ChangeVetoException {
        throw new ChangeVetoException("Cannot modify this sequence. Convert to a SimpleRichSequence first.");
    }
    
    /**
     * {@inheritDoc}
     */
    public Symbol symbolAt(RichSequence seq, int index) throws IndexOutOfBoundsException {
        return this.subList(seq, index, index).symbolAt(1);
    }
    
    /**
     * {@inheritDoc}
     */
    public List toList(RichSequence seq) {
        if (seq.length()==0) return new ArrayList(); // empty list for empty seq
        else return this.subList(seq,1,seq.length()).toList();
    }
    
    /**
     * {@inheritDoc}
     */
    public String subStr(RichSequence seq, int start, int end) throws IndexOutOfBoundsException {
        if (seq.length()==0) return ""; // empty seq
        else if (seq.getCircular()) {
            StringBuffer result = new StringBuffer(); // place to store the resulting substring
            int[] modLocation = Tools.modulateCircularLocation(start,end,seq.length());
            int modStart = modLocation[0];
            int modEnd = modLocation[1];
            int modLength = (modEnd - modStart)+1;
            int seqLength = seq.length();
            if (modStart==0) modStart = seqLength;
            if (modEnd==0) modEnd = seqLength;
            // Use the packed symbol factory
            ChunkedSymbolListFactory symsf = new ChunkedSymbolListFactory(new PackedSymbolListFactory());
            if (modEnd>seqLength) {
                // add it in chunks
                int remaining = modLength;
                int chunkSize = (seqLength-modStart)+1;
                //   add modStart -> seqLength
                result.append(this.seqSubString(seq, modStart, seqLength));
                remaining -= chunkSize;
                //   repeat add seqLength
                while (remaining > seqLength) {
                    chunkSize = seqLength;
                    //   add 0 -> seqLength
                    result.append(this.seqSubString(seq, 1, seqLength));
                    remaining -= chunkSize;
                }
                //   add 0 -> remaining
                chunkSize = remaining;
                result.append(this.seqSubString(seq, 1, chunkSize));
            } else {
                //   add modStart->modEnd
                result.append(this.seqSubString(seq, modStart, modEnd));
            }
            return result.toString();
        } else {
            return this.seqSubString(seq, start, end);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList subList(RichSequence seq, int start, int end) throws IndexOutOfBoundsException {
        return this.convertToSymbolList(this.subStr(seq,start,end),seq.getAlphabet());
    }
    
    /**
     * {@inheritDoc}
     */
    public String seqString(RichSequence seq) {
        // load whole stringSequence property from Sequence
        try {
            // Build the query object
            String queryText = "select s.stringSequence from Sequence as s where s = ?";
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            // Set the parameters
            query = this.setParameter.invoke(query, new Object[]{new Integer(0), seq});
            // Get the results
            Object result = this.uniqueResult.invoke(query, null);
            // Return the found object, if found - null if not.
            return (String)result;
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to locate full sequence "+seq,e);
        }
    }
    
    private String seqSubString(RichSequence seq, int start, int end) {
        // load whole stringSequence property from Sequence
        try {
            // Build the query object
            String queryText = "select substring(s.stringSequence,?,?) from Sequence as s where s = ?";
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            // Set the parameters
            query = this.setParameter.invoke(query, new Object[]{new Integer(0), new Integer(start-1)});
            query = this.setParameter.invoke(query, new Object[]{new Integer(1), new Integer((end-start)+1)});
            query = this.setParameter.invoke(query, new Object[]{new Integer(2), seq});
            // Get the results
            Object result = this.uniqueResult.invoke(query, null);
            // Return the found object, if found - null if not.
            return (String)result;
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to locate full sequence "+seq,e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator iterator(RichSequence seq) {
        return this.toList(seq).iterator();
    }
    
    private SymbolList convertToSymbolList(String seq, Alphabet alpha) {
        try {
            return new SimpleSymbolList(alpha.getTokenization("token"), seq);
        } catch (IllegalSymbolException e) {
            throw new BioError("Found bad symbols in sequence string!",e);
        } catch (BioException e) {
            throw new BioError("Found general exception in sequence string!",e);
        }
    }
}
