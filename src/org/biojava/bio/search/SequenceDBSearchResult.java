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

package org.biojava.bio.search;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.*;
import org.biojava.utils.contract.*;

/**
 * <code>SequenceDBSearchResult</code> objects represent a result of a
 * search of a SymbolList against the sequences within a SequenceDB
 * object. The core data (query sequence, database, search parameters,
 * hits) have accessors, while supplementary data are stored in the
 * Annotation object. Supplementary data are typically the more
 * loosely formatted details which vary from one search program to
 * another (and between versions of those programs).
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.0
 * @since 1.1
 * @see AbstractChangeable
 * @see SeqSimilaritySearchResult
 * @see Annotatable
 */
public class SequenceDBSearchResult extends AbstractChangeable
    implements SeqSimilaritySearchResult, Annotatable
{
    private SequenceDB sequenceDB;
    private Map        searchParameters;
    private SymbolList querySeq;
    private List       hits;
    private Annotation annotation;

    /**
     * <code>annFor</code> is the annotation forwarder for the
     * Annotation object. It listens for changes to the annotation
     * object and passes them on to us.
     */
    protected ChangeListener annFor;

    /**
     * Creates a new <code>SequenceDBSearchResult</code> object.
     *
     * @param sequenceDB a <code>SequenceDB</code> object.
     * @param searchParameters a <code>Map</code> object.
     * @param querySeq a <code>SymbolList</code> object.
     * @param annotation an <code>Annotation</code> object.
     * @param hits a <code>List</code> object.
     */
    public SequenceDBSearchResult(SequenceDB sequenceDB,
				  Map        searchParameters,
				  SymbolList querySeq,
				  Annotation annotation,
				  List       hits)
    {
	Contract.pre(querySeq   != null, "querySeq was null");
	Contract.pre(sequenceDB != null, "sequenceDB was null");
	// searchParameters may be null
	Contract.pre(annotation != null, "annotation was null");
	Contract.pre(hits       != null, "hits was null");

	this.sequenceDB       = sequenceDB;
	this.searchParameters = searchParameters;
	this.querySeq         = querySeq;
	this.hits             = hits;
	this.annotation       = (annotation == null ? new SimpleAnnotation() : annotation);
    }

    {
	// Forward annotation changes to ourself, although the
	// annotation should not change
	annFor = new Annotatable.AnnotationForwarder(this, getChangeSupport(Annotatable.ANNOTATION));    
	annotation.addChangeListener(annFor,
				     Annotatable.ANNOTATION);

	// Lock the annotation by vetoing all changes to properties
	annotation.addChangeListener(ChangeListener.ALWAYS_VETO,
				     Annotation.PROPERTY);
    }

    public SymbolList getQuerySequence()
    {
	return querySeq;
    }

    public SequenceDB getSequenceDB()
    {
	return sequenceDB;
    }

    public Map getSearchParameters()
    {
	return (searchParameters == null ? null : Collections.unmodifiableMap(searchParameters));
    }

    public List getHits()
    {
	return Collections.unmodifiableList(hits);
    }

    /**
     * <code>getAnnotation</code> returns the Annotation associated
     * with this hit.
     *
     * @return an <code>Annotation</code> value.
     */
    public Annotation getAnnotation()
    {
	return annotation;
    }

    public boolean equals(Object other)
    {
	if (other == this) return true;
	if (other == null) return false;

	// Eliminate other if its class is not the same
	if (! other.getClass().equals(this.getClass())) return false;

	// Downcast and compare fields
	SequenceDBSearchResult that = (SequenceDBSearchResult) other;

	if (! ObjectUtil.equals(this.querySeq, that.querySeq))
	    return false;
	if (! ObjectUtil.equals(this.sequenceDB, that.sequenceDB))
	    return false;
	if (! ObjectUtil.equals(this.searchParameters, that.searchParameters))
	    return false;
	if (! ObjectUtil.equals(this.hits, that.hits))
	    return false;
	if (! ObjectUtil.equals(this.annotation, that.annotation))
	    return false;

	return true;
    }

    public int hashCode()
    {
	int hCode = 0;

	hCode = ObjectUtil.hashCode(hCode, querySeq);
	hCode = ObjectUtil.hashCode(hCode, sequenceDB);
	hCode = ObjectUtil.hashCode(hCode, searchParameters);
	hCode = ObjectUtil.hashCode(hCode, hits);
	hCode = ObjectUtil.hashCode(hCode, annotation);

	return hCode;
    }

    public String toString()
    {
	return "SequenceDBSearchResult of " + getQuerySequence()
	    + " against " + getSequenceDB();
    }

    // As the object is immutable we can alias ourselves
    public Object clone()
    {
	return this;
    }
}
