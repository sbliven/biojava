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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.SmallAnnotation;
import org.biojava.bio.program.search.SearchBuilder;
import org.biojava.bio.program.search.SearchContentHandler;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.search.SeqSimilaritySearchSubHit;
import org.biojava.bio.search.SequenceDBSearchHit;
import org.biojava.bio.search.SequenceDBSearchResult;
import org.biojava.bio.search.SequenceDBSearchSubHit;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.db.SequenceDBInstallation;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SimpleAlignment;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.utils.ChangeVetoException;

/**
 * <p>
 * <code>BlastLikeSearchBuilder</code> will create
 * <code>SeqSimilaritySearchResult</code>s from SAX events via a
 * <code>SeqSimilarityAdapter</code>. The SAX events should describe
 * elements conforming to the BioJava BlastLikeDataSetCollection
 * DTD. Suitable sources are <code>BlastLikeSAXParser</code> or
 * <code>FastaSAXParser</code>. The result objects are placed in the
 * <code>List</code> supplied to the constructor.
 * </p>
 *
 * <p>
 * The start/end/strand of <code>SeqSimilaritySearchHit</code>s are
 * calculated from their constituent
 * <code>SeqSimilaritySearchSubHit</code>s as follows:
 * </p>
 *
 * <ul>
 * <li>The query start is the lowest query start coordinate of its
 *     sub-hits, regardless of strand</li>
 * <li>The query end is the highest query end coordinate of its sub-hits,
 *     regardless of strand</li>
 * <li>The hit start is the lowest hit start coordinate of its sub-hits,
 *     regardless of strand</li>
 * <li>The hit end is the highest hit end coordinate of its sub-hits,
 *     regardless of strand</li>
 * <li>The query strand is null for protein sequences. Otherwise it is
 *     equal to the query strand of its sub-hits if they are all on the
 *     same strand, or <code>StrandedFeature.UNKNOWN</code> if the sub-hits
 *     have mixed query strands</li>
 * <li>The hit strand is null for protein sequences. Otherwise it is
 *     equal to the hit strand of its sub-hits if they are all on the same
 *     strand, or <code>StrandedFeature.UNKNOWN</code> if the sub-hits have
 *     mixed hit strands</li>
 * </ul>
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class BlastLikeSearchBuilder implements SearchBuilder
{
    // Supplier of instances of searched databases
    private SequenceDBInstallation subjectDBs;
    // The specific database searched
    private SequenceDB             subjectDB;
    // Holder for all query sequences
    private SequenceDB             querySeqHolder;
    // The specific query sequence instance
    private SymbolList             querySeq;

    // Hit and Result annotation
    private Annotation             hitAnnotation;
    private Annotation             resultAnnotation;

    // Data holders for search result properties
    private Map                    resultPreAnnotation;
    private Map                    searchParameters;
    private Map                    hitData;
    private Map                    subHitData;

    private SymbolTokenization     tokenParser;
    private StringBuffer           tokenBuffer;

    private List                   hits;
    private List                   subHits;

    private SeqSimilaritySearchSubHit [] subs;

    // Flag indicating whether there are more results in the stream
    private boolean moreSearchesAvailable = false;

    // List to accept all results in the stream
    private List target;

    /**
     * Creates a new <code>BlastLikeSearchBuilder</code> which will
     * instantiate results into the <code>List</code> target.
     *
     * @param target a <code>List</code>.
     */
    public BlastLikeSearchBuilder(List target)
    {
        this.target = target;

        resultPreAnnotation = new HashMap();
        searchParameters    = new HashMap();
        hitData             = new HashMap();
        subHitData          = new HashMap();
        tokenBuffer         = new StringBuffer(1024);
    }

    public SeqSimilaritySearchResult makeSearchResult()
        throws BioException
    {
        return new SequenceDBSearchResult(subjectDB,
					  searchParameters,
					  querySeq,
					  resultAnnotation,
					  hits);
    }

    /**
     * <code>getQuerySeqHolder</code> returns the database of query
     * sequences used to retrieve sequences for creation of the
     * various result objects.
     *
     * @return a <code>SequenceDB</code> value.
     */
    public SequenceDB getQuerySeqHolder()
    {
        return querySeqHolder;
    }

    /**
     * <code>setQuerySeqHolder</code> sets the query sequence holder
     * to a specific database.
     *
     * @param querySeqHolder a <code>SequenceDB</code> containing the
     * query sequence(s).
     */
    public void setQuerySeqHolder(final SequenceDB querySeqHolder)
    {
        this.querySeqHolder = querySeqHolder;
    }

    /**
     * <code>getSubjectDBInstallation</code> returns the installation
     * in which all the databases searched may be
     * found. <code>SequenceDB</code>s are retrieved for creation of
     * the various result objects.
     *
     * @return a <code>SequenceDBInstallation</code> containing the
     * subject database(s).
     */
    public SequenceDBInstallation getSubjectDBInstallation()
    {
        return subjectDBs;
    }

    /**
     * <code>setSubjectDBInstallation</code> sets the subject database
     * holder to a specific installation.
     *
     * @param subjectDBs a <code>SequenceDBInstallation</code>
     * containing the subject database(s)
     */
    public void setSubjectDBInstallation(final SequenceDBInstallation subjectDBs)
    {
        this.subjectDBs = subjectDBs;
    }

    public void setQuerySeq(final String querySeqId)
        throws BioException
    {
        if (querySeqHolder == null)
            throw new BioException("Running BlastLikeSearchBuilder with null query SequenceDB");

        SymbolList temp = (SymbolList) querySeqHolder.getSequence(querySeqId);

        // It shouldn't happen, but it can with some implementations
        // of SequenceDB
        if (temp == null)
	    throw new BioException("Failed to retrieve query sequence from holder using ID '"
				   + querySeqId
                                   + "' (sequence was null)");

        querySeq = temp;
    }

    public void setSubjectDB(final String subjectDBName)
        throws BioException
    {
        if (subjectDBs == null)
            throw new BioException("Running BlastLikeSearchBuilder with null subject SequenceDBInstallation");

        subjectDB = subjectDBs.getSequenceDB(subjectDBName);

        // It shouldn't happen, but it can with some implementations
        // of SequenceDBInstallation
	if (subjectDB == null)
	    throw new BioException("Failed to retrieve database from installation using ID '"
				   + subjectDBName
                                   + "' (sequence was null)");
    }

    public boolean getMoreSearches()
    {
        return moreSearchesAvailable;
    }

    public void setMoreSearches(final boolean value)
    {
        moreSearchesAvailable = value;
    }

    public void startSearch()
    {
        hits = new ArrayList();
    }

    public void endSearch()
    {
        try
        {
            resultAnnotation = AnnotationFactory.makeAnnotation(resultPreAnnotation);
            target.add(makeSearchResult());
        }
        catch (BioException be)
        {
            System.err.println("Failed to build SeqSimilaritySearchResult:");
            be.printStackTrace();
        }
    }

    public void startHeader()
    {
        resultPreAnnotation.clear();
        searchParameters.clear();
    }

    public void endHeader() { }

    public void startHit()
    {
        hitData.clear();
        subHits = new ArrayList();
    }

    public void endHit()
    {
        hits.add(makeHit());
    }

    public void startSubHit()
    {
        subHitData.clear();
    }

    public void endSubHit()
    {
        try
        {
            subHits.add(makeSubHit());
        }
        catch (BioException be)
        {
            System.err.println("Failed to build SubHit:");
            be.printStackTrace();
        }
    }

    public void addSearchProperty(final Object key, final Object value)
    {
        resultPreAnnotation.put(key, value);
    }

    public void addHitProperty(final Object key, final Object value)
    {
        hitData.put(key, value);
    }

    public void addSubHitProperty(final Object key, final Object value)
    {
        subHitData.put(key, value);
    }

    /**
     * <code>makeHit</code> creates a new hit. The hit's strand data
     * is the same as that of the highest-scoring sub-hit. The hit's
     * start/end data are the same as the extent of the sub-hits on
     * that strand.
     *
     * @return a <code>SeqSimilaritySearchHit</code>.
     */
    private SeqSimilaritySearchHit makeHit()
    {
        double sc = Double.NaN;
        double ev = Double.NaN;
        double pv = Double.NaN;

        subs = (SeqSimilaritySearchSubHit []) subHits
            .toArray(new SeqSimilaritySearchSubHit [subHits.size() - 1]);

        // Sort to get highest score
        Arrays.sort(subs, SeqSimilaritySearchSubHit.byScore);
        sc = subs[subs.length - 1].getScore();
        ev = subs[subs.length - 1].getEValue();
        pv = subs[subs.length - 1].getPValue();

        // Check for any mixed or null strands
        boolean    mixQueryStrand = false;
        boolean  mixSubjectStrand = false;
        boolean   nullQueryStrand = false;
        boolean nullSubjectStrand = false;

        // Start with index 0 value (arbitrarily)
        Strand qStrand = subs[0].getQueryStrand();
        Strand sStrand = subs[0].getSubjectStrand();

        int qStart = subs[0].getQueryStart();
        int qEnd   = subs[0].getQueryEnd();
        int sStart = subs[0].getSubjectStart();
        int sEnd   = subs[0].getSubjectEnd();

        if (qStrand == null)
            nullQueryStrand = true;
        if (sStrand == null)
            nullSubjectStrand = true;

        // Compare all other values. Note --i > 0 as we are comparing
        // all the the index 0 value
        for (int i = subs.length; --i > 0;)
        {
            Strand qS = subs[i].getQueryStrand();
            Strand sS = subs[i].getSubjectStrand();

            if (qS == null)
                nullQueryStrand = true;
            if (sS == null)
                nullSubjectStrand = true;

            if (qS != qStrand)
                mixQueryStrand = true;
            if (sS != sStrand)
                mixSubjectStrand = true;

            qStart = Math.min(qStart, subs[i].getQueryStart());
            qEnd   = Math.max(qEnd,   subs[i].getQueryEnd());
            
            sStart = Math.min(sStart, subs[i].getSubjectStart());
            sEnd   = Math.max(sEnd,   subs[i].getSubjectEnd());
        }

        // Note any mixed strand hits as unknown strand
        if (mixQueryStrand)
            qStrand = StrandedFeature.UNKNOWN;
        if (mixSubjectStrand)
            sStrand = StrandedFeature.UNKNOWN;

        // Any null strands from protein sequences
        if (nullQueryStrand)
            qStrand = null;
        if (nullSubjectStrand)
            sStrand = null;

        String hitId = (String) hitData.get("HitId");

        return new SequenceDBSearchHit(sc, ev, pv,
                                       qStart,
                                       qEnd,
                                       qStrand,
                                       sStart,
                                       sEnd,
                                       sStrand,
                                       hitId,
                                       AnnotationFactory.makeAnnotation(hitData),
                                       subHits);
    }

    /**
     * <code>makeSubHit</code> creates a new sub-hit.
     *
     * @return a <code>SeqSimilaritySearchSubHit</code>.
     *
     * @exception BioException if an error occurs.
     */
    private SeqSimilaritySearchSubHit makeSubHit() throws BioException
    {
        // Try to get a valid TokenParser
        if (tokenParser == null)
        {
            String identifier;

            // Try explicit sequence type first
            if (subHitData.containsKey("hitSequenceType"))
                identifier = (String) subHitData.get("hitSequenceType");
            // Otherwise try to resolve from the program name (only
            // works for Blast)
            else if (resultPreAnnotation.containsKey("program"))
                identifier = (String) resultPreAnnotation.get("program");
            else
                throw new BioException("Failed to determine sequence type");

            FiniteAlphabet alpha = AlphabetResolver.resolveAlphabet(identifier);
            tokenParser = alpha.getTokenization("token");
        }

        // BLASTP output has the strands set null (protein sequences)
        Strand qStrand = null;
        Strand sStrand = null;

        // Override where an explicit strand is given (FASTA DNA,
        // BLASTN)
        if (subHitData.containsKey("queryStrand"))
            if (subHitData.get("queryStrand").equals("plus"))
                qStrand = StrandedFeature.POSITIVE;
            else
                qStrand = StrandedFeature.NEGATIVE;

        if (subHitData.containsKey("subjectStrand"))
            if (subHitData.get("subjectStrand").equals("plus"))
                sStrand = StrandedFeature.POSITIVE;
            else
                sStrand = StrandedFeature.NEGATIVE;

        // Override where a frame is given as this contains strand
        // information (BLASTX for query, TBLASTN for hit, TBLASTX for
        // both)
        if (subHitData.containsKey("queryFrame"))
            if (((String) subHitData.get("queryFrame")).startsWith("plus"))
                qStrand = StrandedFeature.POSITIVE;
            else
                qStrand = StrandedFeature.NEGATIVE;

        if (subHitData.containsKey("subjectFrame"))
            if (((String) subHitData.get("subjectFrame")).startsWith("plus"))
                sStrand = StrandedFeature.POSITIVE;
            else
                sStrand = StrandedFeature.NEGATIVE;

        // Get start/end
        int qStart = Integer.parseInt((String) subHitData.get("querySequenceStart"));
        int   qEnd = Integer.parseInt((String) subHitData.get("querySequenceEnd"));
        int sStart = Integer.parseInt((String) subHitData.get("subjectSequenceStart"));
        int   sEnd = Integer.parseInt((String) subHitData.get("subjectSequenceEnd"));

        // The start/end coordinates from BioJava XML don't follow the
        // BioJava paradigm of start < end, with orientation given by
        // the strand property. Rather, they present start/end as
        // displayed in BLAST output, with the coordinates being
        // inverted on the reverse strand. We account for this here.
        if (qStrand == StrandedFeature.NEGATIVE)
        {
            int swap = qStart;
            qStart = qEnd;
            qEnd   = swap;
        }

        if (sStrand == StrandedFeature.NEGATIVE)
        {
            int swap = sStart;
            sStart = sEnd;
            sEnd   = swap;
        }

        // Get scores
        double sc = Double.NaN;
        double ev = Double.NaN;
        double pv = Double.NaN;

        if (subHitData.containsKey("score"))
            sc = Double.parseDouble((String) subHitData.get("score"));

        if (subHitData.containsKey("expectValue"))
        {
            String val = (String) subHitData.get("expectValue");
            // Blast sometimes uses invalid formatting such as 'e-156'
            // rather than '1e-156'
            if (val.startsWith("e"))
                ev = Double.parseDouble("1" + val);
            else
                ev = Double.parseDouble(val);
        }

        if (subHitData.containsKey("pValue"))
            pv = Double.parseDouble((String) subHitData.get("pValue"));

        Map labelMap = new HashMap();

        tokenBuffer.setLength(0);
        tokenBuffer.append((String) subHitData.get("querySequence"));
        labelMap.put(SeqSimilaritySearchSubHit.QUERY_LABEL, 
                     new SimpleSymbolList(tokenParser, tokenBuffer.toString()));

        tokenBuffer.setLength(0);
        tokenBuffer.append((String) subHitData.get("subjectSequence"));
        labelMap.put(hitData.get("HitId"), 
                     new SimpleSymbolList(tokenParser, tokenBuffer.toString()));

        return new SequenceDBSearchSubHit(sc, ev, pv,
                                          qStart, qEnd, qStrand,
                                          sStart, sEnd, sStrand,
                                          new SimpleAlignment(labelMap));
    }
}
