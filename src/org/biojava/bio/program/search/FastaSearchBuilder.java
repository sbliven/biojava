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

package org.biojava.bio.program.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.search.SeqSimilaritySearchSubHit;
import org.biojava.bio.search.SequenceDBSearchHit;
import org.biojava.bio.search.SequenceDBSearchResult;
import org.biojava.bio.search.SequenceDBSearchSubHit;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.db.SequenceDBInstallation;
import org.biojava.bio.seq.io.TokenParser;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleAlignment;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;

/**
 * A <code>FastaSearchBuilder</code> object organises search data
 * parsed from a search stream by a separate parser object. The parser
 * calls methods in the FastaSearchBuilder to coordinate creation of
 * FastaSearchResult objects.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 * @see SearchBuilder
 */
public class FastaSearchBuilder implements SearchBuilder
{
    private SearchParser            parser;
    private boolean                 moreSearchesAvailable = false;

    private SequenceDBInstallation  subjectDBs;
    private SequenceDB              subjectDB;

    private SequenceDB              querySeqHolder;
    private SymbolList              querySeq;

    private Annotation              hitAnnotation;
    private Annotation              resultAnnotation;

    private Map                     resultPreAnnotation;
    private Map                     searchParameters;
    private Map                     hitData;

    private StringBuffer            querySeqBuf;
    private StringBuffer            subjectSeqBuf;
    private StringBuffer            querySeqPrep;
    private StringBuffer            subjectSeqPrep;

    private TokenParser             tokenParser;

    // Hits are appended to this ArrayList as they are parsed from a
    // stream. The Fasta program pre-sorts the hits in order of
    // significance. This implementation currently relies of this to
    // sort its hits i.e. no sorting is performed.
    private ArrayList searchHits = new ArrayList();

    /**
     * Creates a new <code>FastaSearchBuilder</code> object.
     *
     * @param subjectDBs a <code>SequenceDBInstallation</code>
     * instance which must contain a SequenceDB with an identifier
     * which is the same as the filename of the database specified in
     * the search output. The filename of the search database used to
     * resolve a corresponding SequenceDB, provided that the filename
     * is recorded as one of its identifiers.
     * @param querySeqHolder a <code>SequenceDB</code> which should
     * contain one instance of each of the query sequences used in the
     * searches to be parsed. The sequence ID used in the search
     * output should correspond to the ID of a Sequence within this
     * database.
     */
    public FastaSearchBuilder(final SequenceDBInstallation subjectDBs,
			      final SequenceDB             querySeqHolder)
    {
	this.subjectDBs     = subjectDBs;
	this.querySeqHolder = querySeqHolder;

	querySeqBuf      = new StringBuffer();
	subjectSeqBuf    = new StringBuffer();
	querySeqPrep     = new StringBuffer();
	subjectSeqPrep   = new StringBuffer();
        searchParameters = new HashMap();
    }

    /**
     * The <code>makeSearchResult</code> method creates a new object
     * representing a the result of a single search.
     *
     * @return a <code>SeqSimilaritySearchResult</code>.
     *
     * @exception BioException if parsing or instantiation fails
     * internally.
     */
    public SeqSimilaritySearchResult makeSearchResult()
    	throws BioException
    {
	return new SequenceDBSearchResult(subjectDB,
					  searchParameters,
					  querySeq,
					  resultAnnotation,
					  searchHits);
    }

    /**
     * <code>setQuerySeq</code> resolves the query sequence ID parsed
     * from the search result to a SymbolList obtained from the
     * querySeqHolder SequenceDB.
     *
     * @param querySeqId a <code>String</code> which should be the
     * sequence ID in the querySeqHolder.
     *
     * @exception BioException if the sequence cannot be obtained.
     */
    public void setQuerySeq(final String querySeqId)
	throws BioException
    {
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

    /**
     * <code>setSubjectDB</code> resolves the target database filename
     * parsed from the search result to a SequenceDB instance using a
     * SequenceDBInstallation to perform the mapping.
     *
     * @param subjectDBName a <code>String</code> which should be
     * an identifier of the corresponding database in the subjectDBs
     * installation.
     *
     * @exception BioException if the database cannot be obtained.
     */
    public void setSubjectDB(final String subjectDBName)
	throws BioException
    {
	subjectDB = subjectDBs.getSequenceDB(subjectDBName);

	if (subjectDB == null)
	    throw new BioException("Failed to retrieve database from installation using ID: "
				   + subjectDBName);
    }

    public boolean getMoreSearches()
    {
        return moreSearchesAvailable;
    }

    public void setMoreSearches(final boolean value)
    {
        moreSearchesAvailable = value;
    }

    public void startSearch() { }

    public void endSearch() { }

    public void startHeader()
    {
	resultPreAnnotation = new HashMap();
	searchParameters    = new HashMap();
    }

    public void addSearchProperty(final Object key, final Object value)
    {
	resultPreAnnotation.put(key, value);
    }

    public void endHeader()
    {
	resultAnnotation = makeAnnotation(resultPreAnnotation);
    }

    public void startHit()
    {
	hitData = new HashMap();
    }

    public void addHitProperty(final Object key, final Object value)
    {
	hitData.put(key, value);
    }

    public void endHit() { }

    public void startSubHit() { }

    public void addSubHitProperty(final Object key, final Object value)
    {
	hitData.put(key, value);
    }

    public void endSubHit()
    {
	hitAnnotation = makeAnnotation(hitData);

	try
	{
	    SeqSimilaritySearchHit hit = makeHit(hitData, hitAnnotation);
	    searchHits.add(hit);
	}
	catch (BioException be)
	{
	    be.printStackTrace();
	}
    }

    /**
     * The <code>makeHit</code> method makes a new Hit object from
     * the hit data and an annotation.
     *
     * @param dataMap a <code>Map</code> object.
     * @param hitAnnotation an <code>Annotation</code>.
     *
     * @return a <code>SeqSimilaritySearchHit</code>.
     */
    private SeqSimilaritySearchHit makeHit(final Map        hitData,
                                           final Annotation hitAnnotation)
	throws BioException
    {
	String      seqType = (String) hitData.get("query_sq_type");
	String subjectSeqID = (String) hitData.get("id");
	double        score = Double.parseDouble((String) hitData.get("fa_z-score"));
	double       eValue = Double.parseDouble((String) hitData.get("fa_expect"));
	double       pValue = Double.parseDouble((String) hitData.get("fa_expect"));

	int        queryStart = Integer.parseInt((String) hitData.get("query_al_start"));
	int          queryEnd = Integer.parseInt((String) hitData.get("query_al_stop"));
	int    queryDispStart = Integer.parseInt((String) hitData.get("query_al_display_start"));
	String querySeqTokens = (String) hitData.get("querySeqTokens");
	Strand querySeqStrand = StrandedFeature.POSITIVE;

	int        subjectStart = Integer.parseInt((String) hitData.get("subject_al_start"));
	int          subjectEnd = Integer.parseInt((String) hitData.get("subject_al_stop"));
	int    subjectDispStart = Integer.parseInt((String) hitData.get("subject_al_display_start"));
	String subjectSeqTokens = (String) hitData.get("subjectSeqTokens");

	Strand subjectSeqStrand;
	if (((String) hitData.get("fa_frame")).equals("f"))
	    subjectSeqStrand = StrandedFeature.POSITIVE;
	else
	    subjectSeqStrand = StrandedFeature.NEGATIVE;

	// What happens if Fasta is given an RNA sequence?
	FiniteAlphabet alpha;

	if (seqType.equals("DNA"))
	    alpha = DNATools.getDNA();
	else
	    alpha = ProteinTools.getAlphabet();

        if (tokenParser == null)
            tokenParser = new TokenParser(alpha);

	// There is only ever one subhit in a Fasta hit
	List subHits = new ArrayList();

	querySeqBuf.setLength(0);
	querySeqPrep.setLength(0);
	querySeqBuf.append(querySeqTokens);

	subjectSeqBuf.setLength(0);
	subjectSeqPrep.setLength(0);
	subjectSeqBuf.append(subjectSeqTokens);

	try
	{
	    // System.out.println("Making alignment with: " + hitData);
	    querySeqPrep.append(prepSeqTokens(querySeqBuf,
					      queryStart,
					      queryEnd,
					      queryDispStart));

	    subjectSeqPrep.append(prepSeqTokens(subjectSeqBuf,
						subjectStart,
						subjectEnd,
						subjectDispStart));

	    Alignment alignment = makeAlignment(subjectSeqID,
                                                querySeqPrep,
                                                subjectSeqPrep,
                                                tokenParser);

	    SeqSimilaritySearchSubHit subHit =
		new SequenceDBSearchSubHit(score,
					   eValue,
					   pValue,
                                           queryStart,
					   queryEnd,
					   querySeqStrand,
					   subjectStart,
					   subjectEnd,
					   subjectSeqStrand,
					   alignment);

	    subHits.add(subHit);
	}
	catch (IllegalSymbolException ise)
	{
	    throw new BioException("Failed to create alignment for hit to "
				   + subjectSeqID);
	}

        // For Fasta the query/subject start/end/strand are easy to
        // calculate; they are the same as for the single 'sub-hit'
	return new SequenceDBSearchHit(score,
				       eValue,
				       pValue,
                                       queryStart,
                                       queryEnd,
                                       querySeqStrand,
                                       subjectStart,
                                       subjectEnd,
                                       subjectSeqStrand,
                                       subjectSeqID,
				       hitAnnotation,
                                       subHits);
    }

    /**
     * The <code>makeAnnotation</code> method makes a new annotation
     * instance from the preannotation Map data.
     *
     * @param preAnnotation a <code>Map</code> containing raw data.
     *
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
		cve.printStackTrace();
	    }
	}
	return annotation;
    }

    /**
     * The <code>makeAlignment</code> method creates an Alignment
     * object from the sequence Strings parsed from the Fasta output.
     *
     * @param subjectSeqID a <code>String</code> which will be used to
     * set the label of the subject sequence. The query label is a
     * static field in the SeqSimilaritySearchSubHit interface.
     * @param querySeqBuf a <code>StringBuffer</code> containing the
     * sequence tokens for the query sequence.
     * @param subjectSeqBuf a <code>StringBuffer</code> containing the
     * sequence tokens for the subject sequence.
     * @param tokenParser a <code>TokenParser</code>.
     *
     * @return an <code>Alignment</code>.
     *
     * @exception IllegalSymbolException if tokens from the sequence
     * are not recognised in the chosen alphabet.
     */
    private Alignment makeAlignment(final String       subjectSeqID,
                                    final StringBuffer querySeqBuf,
                                    final StringBuffer subjectSeqBuf,
                                    final TokenParser  tokenParser)
	throws IllegalSymbolException
    {
	Map labelMap = new HashMap();

	labelMap.put(SeqSimilaritySearchSubHit.QUERY_LABEL, 
                     tokenParser.parse(querySeqBuf.toString()));

	labelMap.put(subjectSeqID,
                     tokenParser.parse(querySeqBuf.toString()));

	return new SimpleAlignment(labelMap);
    }

    /**
     * The <code>prepSeqTokens</code> method prepares the sequence
     * data extracted from the Fasta output. Two things need to be
     * done; firstly, the leading gaps are removed from the sequence
     * (these are just format padding and not really part of the
     * alignment) and secondly, as Fasta supplies some flanking
     * sequence context for its alignments, this must be removed
     * too. See the Fasta documentation for an explanation of the
     * format.
     *
     * @param name a <code>StringBuffer</code> containing the
     * unprepared sequence tokens.
     * @param alStart an <code>Integer</code> indicating the start
     * position of the alignment in the original sequence.
     * @param alStop an <code>Integer</code> indicating the stop
     * position of the alignment in the original sequence.
     * @param alDispStart an <code>Integer</code> indicating the start
     * of a flanking context in the original sequence.
     *
     * @return a <code>String</code> value consisting of a subsequence
     * containing only the interesting alignment.
     */
    private String prepSeqTokens(final StringBuffer seqTokens,
				 final int          alStart,
				 final int          alStop,
				 final int          alDispStart)
    {
	// Strip leading gap characters
	while (seqTokens.charAt(0) == '-')
	    seqTokens.deleteCharAt(0);

	int gapCount = 0;
	// Count gaps to add to number of chars returned
	for (int i = 0; i < seqTokens.length(); i++)
	{
	    if (seqTokens.charAt(i) == '-')
		gapCount++;
	}

	// Calculate the position at which the real alignment
	// starts/stops, allowing for the gaps, which are not counted
	// in the numbering system
	return seqTokens.substring(alStart - alDispStart,
				   alStop  - alDispStart + gapCount + 1);
    }
}
