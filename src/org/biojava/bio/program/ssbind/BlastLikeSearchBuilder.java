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
import org.biojava.bio.seq.io.TokenParser;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SimpleAlignment;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;

/**
 * <p><code>BlastLikeSearchBuilder</code> will create
 * <code>SeqSimilaritySearchResult</code>s from SAX events via a
 * <code>SeqSimilarityAdapter</code>. The SAX events should describe
 * elements conforming to the BioJava BlastLikeDataSetCollection
 * DTD. The result objects are placed in the <code>List</code>
 * supplied to the constructor.</p>
 *
 * <p>The start/end/strand of <code>SeqSimilaritySearchHit</code>s are
 * calculated by taking the strand of the top scoring
 * <code>SeqSimilaritySearchSubHit</code> and setting the start equal
 * to the start of the very first sub-hit and the end equal to the end
 * of the very last sub-hit on that strand. The calculation is
 * performed on the subject sequence sub-hits and the corresponding
 * query positions recorded. If you believe that this should be
 * calculated differently or have an improved method, please e-mail
 * me.</p>
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class BlastLikeSearchBuilder implements SearchBuilder
{
    private SequenceDBInstallation subjectDBs;
    private SequenceDB             subjectDB;
    private SequenceDB             querySeqHolder;
    private SymbolList             querySeq;

    private Annotation             hitAnnotation;
    private Annotation             resultAnnotation;
    private Map                    resultPreAnnotation;
    private Map                    searchParameters;
    private Map                    hitData;
    private Map                    subHitData;

    private AlphabetResolver       alphaResolver;
    private TokenParser            tokenParser;
    private StringBuffer           tokenBuffer;

    private List                   hits;
    private List                   subHits;

    private SeqSimilaritySearchSubHit [] subs;

    private List    target;
    private boolean moreSearchesAvailable = false;

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
        alphaResolver       = new AlphabetResolver();
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
            resultAnnotation = makeAnnotation(resultPreAnnotation);
            target.add(makeSearchResult());
        }
        catch (BioException be)
        {
            System.err.println("Failed to build SeqSimilaritySearchResult: ");
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
            System.err.println("Failed to build SubHit: ");
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

    public void setQuerySeq(final String querySeqId)
        throws BioException
    {
        if (querySeqHolder == null)
            throw new BioException("Running BlastLikeSearchBuilder with null query SequenceDB");

        try
	{
	    querySeq = (SymbolList) querySeqHolder.getSequence(querySeqId);
	}
	catch (BioException be)
	{
	    throw new BioException(be, "Failed to retrieve query sequence from holder using ID: "
				   + querySeqId);
	}
    }

    public void setSubjectDB(final String subjectDBName)
        throws BioException
    {
        if (subjectDBs == null)
            throw new BioException("Running BlastLikeSearchBuilder with null subject SequenceDBInstallation");

        subjectDB = subjectDBs.getSequenceDB(subjectDBName);

	if (subjectDB == null)
	    throw new BioException("Failed to retrieve database from installation using ID: "
				   + subjectDBName);
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

        // Retain the strand of the best sub-hit for calculation of
        // hit strand
        Strand   bestQueryStrand = subs[subs.length - 1].getQueryStrand();
        Strand bestSubjectStrand = subs[subs.length - 1].getSubjectStrand();

        int qStart = 0;
        int qEnd   = 0;
        int sStart = 0;
        int sEnd   = 0;

        // Get extent of sub-hits on this strand by subject position
        for (int i = subs.length; --i >= 0;)
        {
            if (subs[i].getSubjectStrand().equals(bestSubjectStrand))
            {
                if (sStart == 0 || sStart > subs[i].getSubjectStart())
                {
                    sStart = subs[i].getSubjectStart();
                    qStart = subs[i].getQueryStart();
                }

                if (sEnd < subs[i].getSubjectEnd())
                {
                    sEnd = subs[i].getSubjectEnd();
                    qEnd = subs[i].getQueryEnd();
                }
            }
        }

        String hitId = (String) hitData.get("HitId");

        return new SequenceDBSearchHit(sc, ev, pv,
                                       qStart,
                                       qEnd,
                                       bestQueryStrand,
                                       sStart,
                                       sEnd,
                                       bestSubjectStrand,
                                       hitId,
                                       makeAnnotation(hitData),
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
        // BLASTP output has the strands implied to be POSITIVE
        Strand qStrand = StrandedFeature.POSITIVE;
        Strand sStrand = StrandedFeature.POSITIVE;

        // Override where an explicit strand is given (FASTA DNA,
        // FASTA protein, BLASTN)
        if (subHitData.containsKey("queryStrand") &&
            subHitData.get("queryStrand").equals("minus"))
            qStrand = StrandedFeature.NEGATIVE;

        if (subHitData.containsKey("subjectStrand") &&
            subHitData.get("subjectStrand").equals("minus"))
            sStrand = StrandedFeature.NEGATIVE;

        // Override where a frame is given as this contains strand
        // information (BLASTX for query, TBLASTN for hit, TBLASTX for
        // both)
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
            int swap = qStart;
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
            ev = Double.parseDouble((String) subHitData.get("expectValue"));
        if (subHitData.containsKey("pValue"))
            pv = Double.parseDouble((String) subHitData.get("pValue"));

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

            FiniteAlphabet alpha = alphaResolver.resolveAlphabet(identifier);
            tokenParser = new TokenParser(alpha);
        }

        Map labelMap = new HashMap();

        tokenBuffer.setLength(0);
        tokenBuffer.append((String) subHitData.get("querySequence"));
        labelMap.put(SeqSimilaritySearchSubHit.QUERY_LABEL, 
                     tokenParser.parse(tokenBuffer.toString()));

        tokenBuffer.setLength(0);
        tokenBuffer.append((String) subHitData.get("subjectSequence"));
        labelMap.put(hitData.get("HitId"), 
                     tokenParser.parse(tokenBuffer.toString()));

        return new SequenceDBSearchSubHit(sc, ev, pv,
                                          qStart, qEnd, qStrand,
                                          sStart, sEnd, sStrand,
                                          new SimpleAlignment(labelMap));
    }

    /**
     * <code>makeAnnotation</code> creates the annotation.
     *
     * @param preAnnotation a <code>Map</code> of raw data.
     * @return an <code>Annotation</code>.
     */
    private Annotation makeAnnotation(final Map preAnnotation)
    {
	Annotation annotation = new SmallAnnotation();
	Set  annotationKeySet = preAnnotation.keySet();

	for (Iterator ksi = annotationKeySet.iterator(); ksi.hasNext();)
	{
	    Object   annotationKey = ksi.next();
	    Object annotationValue = preAnnotation.get(annotationKey);

	    try
	    {
		annotation.setProperty(annotationKey, annotationValue);
	    }
	    catch (ChangeVetoException cve)
	    {
		System.err.println("Failed to add mapping to Annotation:");
                cve.printStackTrace();
	    }
	}
	return annotation;
    }
}
