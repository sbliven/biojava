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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.SmallAnnotation;
import org.biojava.bio.program.search.SearchContentHandler;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.ViewSequence;
import org.biojava.bio.seq.db.IllegalIDException;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.db.SequenceDBInstallation;
import org.biojava.bio.seq.homol.SimilarityPairFeature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.SimpleAlignment;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.utils.ChangeVetoException;

/**
 * <p>
 * <code>SimilarityPairBuilder</code> annotates query and subject
 * <code>Sequence</code> with <code>SimilarityPairFeature</code>s
 * created from SAX events supplied via a
 * <code>SeqSimilarityAdapter</code>. The objective is to describe a
 * simple pairwise relationship between the two sequences. This
 * differs slightly from using <code>HomologyFeature</code>s which are
 * slightly heavier, have to contain a full alignment and don't have
 * an explicit distinction between query and subject sequences in the
 * alignment. The SAX events should describe elements conforming to
 * the BioJava BlastLikeDataSetCollection DTD. Suitable sources are
 * <code>BlastLikeSAXParser</code> or <code>FastaSAXParser</code>.
 * </p>
 *
 * <p>
 * Annotated <code>ViewSequence</code>s wrapping both query and
 * subject sequences are created.
 * </p>
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SimilarityPairBuilder implements SearchContentHandler
{
    public static final String SIMILARITY_PAIR_FEATURE_TYPE = "similarity";

    // Supplier of instances of searched databases
    private SequenceDBInstallation subjectDBs;
    // The specific database searched
    private SequenceDB             subjectDB;
    // Holder for all query sequences
    private SequenceDB             querySeqHolder;
    // View of query sequence instance
    private Sequence               queryView;

    // Cache which holds view(s) of query sequence(s) which have
    // been instantiated for annotation
    private Map                    queryViewCache;

    // Cache which holds view(s) of subject sequence(s) which have
    // been instantiated for annotation
    private Map                    subjectViewCache;

    // Data holders for search result properties
    private Map                    resultData;
    private Map                    hitData;
    private Map                    subHitData;

    private AlphabetResolver       alphaResolver;
    private SymbolTokenization     tokenParser;
    private StringBuffer           tokenBuffer;

    // Flag indicating whether there are more results in the stream
    private boolean moreSearchesAvailable = false;

    public SimilarityPairBuilder()
    {
        resultData       = new HashMap();
        hitData          = new HashMap();
        subHitData       = new HashMap();
        queryViewCache   = new HashMap();
        subjectViewCache = new HashMap();
        alphaResolver    = new AlphabetResolver();
        tokenBuffer      = new StringBuffer(1024);
    }

    public Sequence getAnnotatedQuerySeq(String queryId)
        throws IllegalIDException
    {
        if (! queryViewCache.containsKey(queryId))
            throw new IllegalIDException("Failed to retrieve annotated query sequence from cache using ID '"
                                         + queryId
                                         + "' (unknown ID");

        return (Sequence) queryViewCache.get(queryId);
    }

    public Sequence getAnnotatedSubjectSeq(String subjectId)
        throws IllegalIDException
    {
        if (! subjectViewCache.containsKey(subjectId))
            throw new IllegalIDException("Failed to retrieve annotated subject sequence from cache using ID '"
                                         + subjectId
                                         + "' (unknown ID");

        return (Sequence) subjectViewCache.get(subjectId);
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
            throw new BioException("Running SimilarityPairBuilder with null query SequenceDB");

        Sequence temp = querySeqHolder.getSequence(querySeqId);

        // It shouldn't happen, but it can with some implementations
        // of SequenceDB
        if (temp == null)
	    throw new BioException("Failed to retrieve query sequence from holder using ID '"
				   + querySeqId
                                   + " (sequence was null)");

        queryView = new ViewSequence(temp);
        queryViewCache.put(querySeqId, queryView);
    }

    public void setSubjectDB(final String subjectDBName)
        throws BioException
    {
        if (subjectDBs == null)
            throw new BioException("Running SimilarityPairBuilder with null subject SequenceDBInstallation");

        subjectDB = subjectDBs.getSequenceDB(subjectDBName);

	if (subjectDB == null)
	    throw new BioException("Failed to retrieve database from installation using ID '"
				   + subjectDBName
                                   + "'");
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
        subjectViewCache.clear();
    }

    public void endSearch() { }

    public void startHeader()
    {
        resultData.clear();
    }

    public void endHeader() { }

    public void startHit()
    {
        hitData.clear();
        subHitData.clear();
    }

    public void endHit() { }

    public void startSubHit() { }

    public void endSubHit()
    {
        try
        {
            makeSimilarity();
        }
        catch (BioException be)
        {
            System.err.println("Failed to build Similarity:");
            be.printStackTrace();
        }
    }

    public void addSearchProperty(final Object key, final Object value)
    {
        resultData.put(key, value);
    }

    public void addHitProperty(final Object key, final Object value)
    {
        hitData.put(key, value);
    }

    public void addSubHitProperty(final Object key, final Object value)
    {
        subHitData.put(key, value);
    }

    private void makeSimilarity() throws BioException
    {
        subHitData.putAll(resultData);
        subHitData.putAll(hitData);

        // Try to get a valid TokenParser
        if (tokenParser == null)
        {
            String identifier;
            // Try explicit sequence type first
            if (subHitData.containsKey("hitSequenceType"))
                identifier = (String) subHitData.get("hitSequenceType");
            // Otherwise try to resolve from the program name (only
            // works for Blast)
            else if (subHitData.containsKey("program"))
                identifier = (String) subHitData.get("program");
            else
                throw new BioException("Failed to determine sequence type");

            FiniteAlphabet alpha = alphaResolver.resolveAlphabet(identifier);
            tokenParser = alpha.getTokenization("token");
        }

        // Set strands of hit on query and subject
        Strand qStrand = StrandedFeature.POSITIVE;
        Strand sStrand = StrandedFeature.POSITIVE;

        // In cases where an explicit strand is given (FASTA DNA, BLASTN)
        if (subHitData.containsKey("queryStrand") &&
            subHitData.get("queryStrand").equals("minus"))
            qStrand = StrandedFeature.NEGATIVE;

        if (subHitData.containsKey("subjectStrand") &&
            subHitData.get("subjectStrand").equals("minus"))
            sStrand = StrandedFeature.NEGATIVE;

        // In cases where a frame is given as this contains strand
        // information (TBLASTN for hit, TBLASTX for both query and
        // hit)
        if (subHitData.containsKey("queryFrame") &&
            ((String) subHitData.get("queryFrame")).startsWith("minus"))
            qStrand = StrandedFeature.NEGATIVE;

        if (subHitData.containsKey("subjectFrame") &&
            ((String) subHitData.get("subjectFrame")).startsWith("minus"))
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

        String subjectSeqId = (String) subHitData.get("HitId");

        Sequence subjectView;
        // If we have already instantiated a subjectView for this sequence
        if (subjectViewCache.containsKey(subjectSeqId))
        {
            subjectView = (Sequence) subjectViewCache.get(subjectSeqId);
        }
        else
        {
            try
            {
                Sequence subjectSeq = subjectDB.getSequence(subjectSeqId);
                if (subjectSeq == null)
                    throw new BioException("Failed to retrieve subject sequence from subjectDB using ID '"
                                           + subjectSeqId
                                           + "' (sequence was null)");

                subjectView = new ViewSequence(subjectSeq);
            }
            catch (IllegalIDException iie)
            {
                throw new BioException(iie, "Failed to retrieve subject sequence from subjectDB using ID '"
                                       + subjectSeqId
                                       +  "'");
            }
            subjectViewCache.put(subjectSeqId, subjectView);
        }

        // Map of Alignment sequences
        Map labelMap = new HashMap();

        try
        {
            // Set source to the program name
            String source = "unknown";
            if (subHitData.containsKey("program"))
                source = (String) subHitData.get("program");

            tokenBuffer.setLength(0);
            tokenBuffer.append((String) subHitData.get("querySequence"));
            labelMap.put(SimilarityPairFeature.QUERY_LABEL,
                         new SimpleSymbolList(tokenParser, tokenBuffer.toString()));

            tokenBuffer.setLength(0);
            tokenBuffer.append((String) subHitData.get("subjectSequence"));
            labelMap.put(SimilarityPairFeature.SUBJECT_LABEL,
                         new SimpleSymbolList(tokenParser, tokenBuffer.toString()));

            double score = 0.0;
            if (subHitData.containsKey("score"))
                score = Double.parseDouble((String) subHitData.get("score"));

            // Query sequence feature
            SimilarityPairFeature.Template qt = new SimilarityPairFeature.Template();
            qt.type       = SIMILARITY_PAIR_FEATURE_TYPE;
            qt.source     = source;
            qt.location   = new RangeLocation(qStart, qEnd);
            qt.strand     = qStrand;
            qt.score      = score;
            qt.annotation = AnnotationFactory.makeAnnotation(subHitData);

            // Subject sequence feature
            SimilarityPairFeature.Template st = new SimilarityPairFeature.Template();
            st.type       = SIMILARITY_PAIR_FEATURE_TYPE;
            st.source     = source;
            st.location   = new RangeLocation(sStart, sEnd);
            st.strand     = sStrand;
            st.score      = score;
            st.annotation = AnnotationFactory.makeAnnotation(subHitData);

            Alignment a = new SimpleAlignment(labelMap);
            qt.alignment = a;
            st.alignment = a;

            qt.sibling = (SimilarityPairFeature) subjectView.createFeature(st);
            st.sibling = (SimilarityPairFeature) queryView.createFeature(qt);
        }
        catch (ChangeVetoException cve)
        {
            throw new BioError(cve, "Assertion failure creating SimilarityPairFeature. Template modification vetoed");
        }
    }
}
