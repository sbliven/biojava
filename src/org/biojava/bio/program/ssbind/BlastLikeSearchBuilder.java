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
import org.biojava.bio.SimpleAnnotation;
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
 * <code>BlastLikeSearchBuilder</code> will create
 * <code>SeqSimilaritySearchResult</code>s from SAX events via a
 * <code>SeqSimilarityAdapter</code>. The SAX events should describe
 * elements conforming to the BioJava BlastLikeDataSetCollection
 * DTD. The result objects are placed in the <code>List</code>
 * supplied to the constructor.
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
     * <code>makeHit</code> creates a new hit.
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

        // Sort to put sub-hits in order with respect to subject
        Arrays.sort(subs, SeqSimilaritySearchSubHit.bySubjectStart);

        String hitId = (String) hitData.get("HitId");

        return new SequenceDBSearchHit(sc, ev, pv,
                                       hitQueryStart(), hitQueryEnd(),
                                       hitQueryStrand(),
                                       hitSubjectStart(), hitSubjectEnd(),
                                       hitSubjectStrand(),
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
        double sc = Double.NaN;
        double ev = Double.NaN;
        double pv = Double.NaN;

        if (subHitData.containsKey("score"))
            sc = Double.parseDouble((String) subHitData.get("score"));
        if (subHitData.containsKey("expectValue"))
            ev = Double.parseDouble((String) subHitData.get("expectValue"));
        if (subHitData.containsKey("pValue"))
            pv = Double.parseDouble((String) subHitData.get("pValue"));

        int qStart = Integer.parseInt((String) subHitData.get("QuerySequenceStart"));
        int   qEnd = Integer.parseInt((String) subHitData.get("QuerySequenceEnd"));
        int sStart = Integer.parseInt((String) subHitData.get("HitSequenceStart"));
        int   sEnd = Integer.parseInt((String) subHitData.get("HitSequenceEnd"));

        Strand qStrand = subHitData.get("queryStrand").equals("plus") ?
            StrandedFeature.POSITIVE : StrandedFeature.NEGATIVE;
        Strand sStrand = subHitData.get("hitStrand").equals("plus") ?
            StrandedFeature.POSITIVE : StrandedFeature.NEGATIVE;

        if (tokenParser == null)
        {
            String identifier;

            // Try explicit sequence type first
            if (subHitData.containsKey("hitSequenceType"))
            {
                identifier = (String) subHitData.get("hitSequenceType");
            }
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
        tokenBuffer.append((String) subHitData.get("QuerySequence"));
        labelMap.put(SeqSimilaritySearchSubHit.QUERY_LABEL, 
                     tokenParser.parse(tokenBuffer.toString()));

        tokenBuffer.setLength(0);
        tokenBuffer.append((String) subHitData.get("HitSequence"));
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
	Annotation annotation = new SimpleAnnotation();
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

    /**
     * <code>hitQueryStart</code> resolves the start of the hit on the
     * query sequence from the underlying sub-hits.
     *
     * @return an <code>int</code>.
     */
    private int hitQueryStart()
    {
        return subs[0].getQueryStart();
    }

    /**
     * <code>hitQueryEnd</code> resolves the end of the hit on the
     * query sequence from the underlying sub-hits.
     *
     * @return an <code>int</code>.
     */
    private int hitQueryEnd()
    {
        return subs[subs.length - 1].getQueryEnd();
    }

    /**
     * <code>hitQueryStrand</code> resolves the strand of the hit on
     * the query sequence from the underlying sub-hits.
     *
     * @return a <code>Strand</code>.
     */
    private Strand hitQueryStrand()
    {
        int posCount = 0;
        int negCount = 0;

        // The Strands are public static final, so we can check
        // equality
        for (int i = subs.length; --i >= 0;)
        {
            Strand s = subs[i].getQueryStrand();

            if (s == StrandedFeature.POSITIVE)
                posCount++;
            else if (s == StrandedFeature.NEGATIVE)
                negCount++;
        }

        // If not all one or the other, report unknown
        if (posCount == subs.length)
            return StrandedFeature.POSITIVE;
        else if (negCount == subs.length)
            return StrandedFeature.NEGATIVE;
        else
            return StrandedFeature.UNKNOWN;
    }

    /**
     * <code>hitSubjectStart</code> resolves the start of the hit on
     * the subject sequence from the underlying sub-hits.
     *
     * @return an <code>int</code>.
     */
    private int hitSubjectStart()
    {
        return subs[0].getSubjectStart();
    }

    /**
     * <code>hitSubjectEnd</code> resolves the end of the hit on the
     * subject sequence from the underlying sub-hits.
     *
     * @return an <code>int</code>.
     */
    private int hitSubjectEnd()
    {
        return subs[subs.length - 1].getSubjectEnd();
    }

    /**
     * <code>hitSubjectStrand</code> resolves the strand of the hit on
     * the subject sequence from the underlying sub-hits.
     *
     * @return a <code>Strand</code>.
     */
    private Strand hitSubjectStrand()
    {
        int posCount = 0;
        int negCount = 0;

        // The Strands are public static final, so we can check
        // equality
        for (int i = subs.length; --i >= 0;)
        {
            Strand s = subs[i].getSubjectStrand();

            if (s == StrandedFeature.POSITIVE)
                posCount++;
            else if (s == StrandedFeature.NEGATIVE)
                negCount++;
        }

        // If not all one or the other, report unknown
        if (posCount == subs.length)
            return StrandedFeature.POSITIVE;
        else if (negCount == subs.length)
            return StrandedFeature.NEGATIVE;
        else
            return StrandedFeature.UNKNOWN;
    }
}
