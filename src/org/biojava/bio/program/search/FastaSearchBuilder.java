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
import java.lang.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.search.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.TokenParser;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * A <code>FastaSearchBuilder</code> object organises search data
 * parsed from a search stream by a separate parser object. The parser
 * calls methods in the FastaSearchBuilder to coordinate creation of
 * FastaSearchResult objects.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.0
 * @since 1.1
 * @see SearchBuilder
 */
public class FastaSearchBuilder implements SearchBuilder
{
    private SearchParser           parser;

    private SequenceDBInstallation subjectDBs;
    private SequenceDB             subjectDB;

    private SequenceDB             querySeqHolder;
    private SymbolList             querySeq;

    private Annotation             hitAnnotation;
    private Annotation             resultAnnotation;

    private Map                    resultPreAnnotation;
    private Map                    searchParameters;
    private Map                    hitPreAnnotation;
    private Map                    hitData;

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
    public FastaSearchBuilder(SequenceDBInstallation subjectDBs,
			      SequenceDB             querySeqHolder)
    {
	this.subjectDBs     = subjectDBs;
	this.querySeqHolder = querySeqHolder;
    }

    /**
     * The <code>makeSearchResult</code> method creates a new object
     * representing a the result of a single search.
     *
     * @return a <code>SeqSimilaritySearchResult</code> object.
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
     * <code>setQuerySeq</code> resolves the query sequence id parsed
     * from the search result to a SymbolList obtained from the
     * querySeqHolder SequenceDB.
     *
     * @param querySeqID a <code>String</code> which should be the
     * sequence ID in the querySeqHolder.
     * @exception BioException if the sequence cannot be obtained.
     */
    void setQuerySeq(String querySeqID)
	throws BioException
    {
	try
	{
	    // System.out.println("Setting Query sequence with ID: " + querySeqID);
	    querySeq = (SymbolList) querySeqHolder.getSequence(querySeqID);
	}
	catch (BioException bex)
	{
	    throw new BioException(bex, "Failed to retrieve query sequence from holder using ID: "
				   + querySeqID);
	}
    }

    /**
     * <code>setSubjectDB</code> resolves the target database filename
     * parsed from the search result to a SequenceDB instance using a
     * SequenceDBInstallation to perform the mapping.
     *
     * @param subjectDBFileName a <code>String</code> which should be
     * an identifier of the corresponding database in the subjectDBs
     * installation.
     * @exception BioException if the database cannot be obtained.
     */
    void setSubjectDB(String subjectDBFileName)
	throws BioException
    {
	// System.out.println("Setting subject DB with filename/ID: " + subjectDBFileName);
	subjectDB = subjectDBs.getSequenceDB(subjectDBFileName);

	if (subjectDB == null)
	    throw new BioException("Failed to retrieve database from installation using ID: "
				   + subjectDBFileName);
    }

    public void startSearch() { }

    public void endSearch() { }

    public void startHeader() { }

    public void endHeader()
    {
	resultAnnotation = createAnnotation(resultPreAnnotation);
    }

    public void startHit() { }

    public void endHit() { }

    public void startSubHit() { }

    public void endSubHit()
    {
	hitAnnotation = createAnnotation(hitPreAnnotation);

	try
	{
	    SeqSimilaritySearchHit hit = createHit(hitData, hitAnnotation);
	    searchHits.add(hit);
	}
	catch (BioException bex)
	{
	    bex.printStackTrace();
	}
    }

    /**
     * The <code>setSearchResultData</code> method sets up the search
     * parameters. It is called by the parser once per search.
     *
     * @param resultData a <code>Map</code> object.
     */
    void setSearchResultData(Map resultData)
    {
	searchParameters = resultData;
    }

    /**
     * The <code>setSearchAnnotationData</code> method sets up the
     * search result annotation. It is called by the parser once per
     * search.
     *
     * @param resultPreAnnotation a <code>Map</code> object.
     */
    void setSearchAnnotationData(Map resultPreAnnotation)
    {
	this.resultPreAnnotation = resultPreAnnotation;
    }

    /**
     * The <code>setHitData</code> method sets up the hit data for the
     * currently parsed hit. It is called by the parser once per hit.
     *
     * @param hitData a <code>Map</code> object.
     */
    void setHitData(Map hitData)
    {
	this.hitData = hitData;
    }

    /**
     * The <code>setHitAnnotationData</code> method sets up the hit
     * annotation data for the currently parsed hit. It is called by
     * the parser once per hit.
     *
     * @param hitPreAnnotation a <code>Map</code> object.
     */
    void setHitAnnotationData(Map hitPreAnnotation)
    {
	this.hitPreAnnotation = hitPreAnnotation;
    }

    /**
     * The <code>createHit</code> method makes a new Hit object from
     * the hit data and an annotation.
     *
     * @param dataMap a <code>Map</code> object.
     * @param hitAnnotation an <code>Annotation</code> object.
     * @return a <code>SeqSimilaritySearchHit</code> object.
     */
    private SeqSimilaritySearchHit createHit(Map        dataMap,
					     Annotation hitAnnotation)
	throws BioException
    {
	String subjectID  = (String) dataMap.get("id");
	Double score      = (Double) dataMap.get("fa_z-score");
	Double eValue     = (Double) dataMap.get("fa_expect");
	Double pValue     = (Double) dataMap.get("fa_expect");

	// There is only ever one subhit in a Fasta hit
	List subHits = new ArrayList();

	try
	{
	    Alignment alignment = createAlignment(dataMap);

	    SeqSimilaritySearchSubHit subHit =
		new SequenceDBSearchSubHit(score.doubleValue(),
					   eValue.doubleValue(),
					   pValue.doubleValue(),
					   alignment);

	    subHits.add(subHit);
	}
	catch (IllegalSymbolException ise)
	{
	    throw new BioException("Failed to create alignment for hit to "
				   + subjectID);
	}

	return new SequenceDBSearchHit(subjectID,
				       score.doubleValue(),
				       eValue.doubleValue(),
				       pValue.doubleValue(),
				       subHits,
				       hitAnnotation);
    }

    /**
     * The <code>createAnnotation</code> method makes a new annotation
     * instance from the preannotation Map data.
     *
     * @param preAnnotation a <code>Map</code> object.
     * @return an <code>Annotation</code> object.
     */
    private Annotation createAnnotation(Map preAnnotation)
    {
	Annotation annotation = new SimpleAnnotation();
	Set annotationKeySet  = preAnnotation.keySet();

	for (Iterator ksi = annotationKeySet.iterator(); ksi.hasNext();)
	{
	    Object annotationKey   = ksi.next();
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
     * The <code>createAlignment</code> method creates an Alignment
     * object from the sequence Strings parsed from the Fasta output.
     *
     * @param dataMap a <code>Map</code> object.
     * @return an <code>Alignment</code> object.
     * @exception IllegalSymbolException if an error occurs.
     */
    private Alignment createAlignment(Map dataMap)
	throws IllegalSymbolException
    {
	String seqType = (String) dataMap.get("query");

	FiniteAlphabet alpha;

	// What happens if Fasta is given an RNA sequence?
	if (seqType.equals("dna"))
	    alpha = DNATools.getDNA();
	else
	    alpha = ProteinTools.getAlphabet();

	TokenParser tp = new TokenParser(alpha);

	StringBuffer querySeqTokens   =
	    new StringBuffer((String) dataMap.get("querySeqTokens"));
	StringBuffer subjectSeqTokens =
	    new StringBuffer((String) dataMap.get("querySeqTokens"));

	StringBuffer ungappedQuerySeqTokens   = new StringBuffer();
	StringBuffer ungappedSubjectSeqTokens = new StringBuffer();

	Map labelMap = new HashMap();

	// Alignment handling code to go here; currently the alignment
	// is empty. Perhaps separate class to build gapped symbol list
	// from sequence String containing 'gap' characters such as "-",
	// "." or " "

	// Query label is static field in interface
	labelMap.put("query",   tp.parse(""));
	labelMap.put("subject", tp.parse(""));

	return new SimpleAlignment(labelMap);
    }
}
