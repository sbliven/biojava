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
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * <code>SequenceDBSearchResult</code> objects represent a result of a
 * search of a SymbolList against the sequences within a SequenceDB
 * object. The core data (query sequence, database, search parameters,
 * hits) have accessors, while supplementary data are stored in the
 * Annotation object. Supplementary data are typically the more
 * loosely formatted details which vary from one search program to
 * another (and between versions of those programs).
 *
 * @author Keith James
 * @since 1.1
 * @see AbstractChangeable
 * @see SeqSimilaritySearchResult
 * @see Annotatable
 */
public class SequenceDBSearchResult extends AbstractChangeable
    implements SeqSimilaritySearchResult, Annotatable
{
    private String     queryID;
    private String     databaseID;
    private Map        searchParameters;
    private List       hits;
    private Annotation annotation;
    protected transient AnnotationForwarder annotationForwarder;

    // Hashcode is cached after first calculation because the data on
    // which is is based do not change
    private int hc;
    private boolean hcCalc;

    /**
     * Creates a new <code>SequenceDBSearchResult</code>.
     *
     * @param queryID a <code>String</code>.
     * @param databaseID a <code>String</code>.
     * @param searchParameters a <code>Map</code>.
     * @param hits a <code>List</code>.
     * @param annotation an <code>Annotation</code>.
     */
    public SequenceDBSearchResult(String     queryID,
                                  String     databaseID,
                                  Map        searchParameters,
                                  List       hits,
                                  Annotation annotation)
    {
        if (queryID  == null)
        {
            throw new IllegalArgumentException("queryID was null");
        }

        if (databaseID == null)
        {
            throw new IllegalArgumentException("databaseID was null");
        }

        // searchParameters may be null
        if (annotation == null)
        {
            throw new IllegalArgumentException("annotation was null");
        }

        if (hits == null)
        {
            throw new IllegalArgumentException("hits was null");
        }

        this.queryID          = queryID;
        this.databaseID       = databaseID;
        this.searchParameters = searchParameters;
        this.hits             = Collections.unmodifiableList(hits);
        this.annotation       = annotation;

        // Lock the annotation by vetoing all changes to properties
        this.annotation.addChangeListener(ChangeListener.ALWAYS_VETO);
    }

    public String getQueryID()
    {
        return queryID;
    }

    public String getDatabaseID()
    {
        return databaseID;
    }

    /**
     * Return the query sequence which was used to perform the search.
     *
     * @return the <code>SymbolList</code> object used to search the
     * <code>SequenceDB</code>. Never returns null.
     *
     * @deprecated use <code>getQueryID</code> to obtain a database
     * identifier which may then be used to locate the query
     * <code>SymbolList</code> in the appropriate
     * <code>SequenceDB</code>.
     */
    public SymbolList getQuerySequence()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Return the sequence database against which the search that
     * produced this search result was performed.
     *
     * @return the <code>SequenceDB</code> object against which the
     * search was carried out. Never returns null.
     *
     * @deprecated use <code>getDatabaseID</code> to obtain a database
     * identifier which may then be used to locate a
     * <code>SequenceDB</code> in the appropriate
     * <code>SequenceDBInstallation</code>.
     */
    public SequenceDB getSequenceDB()
    {
        throw new UnsupportedOperationException();
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

        if (! other.getClass().equals(this.getClass())) return false;

        SequenceDBSearchResult that = (SequenceDBSearchResult) other;

        if (! ObjectUtil.equals(this.queryID, that.queryID))
            return false;
        if (! ObjectUtil.equals(this.databaseID, that.databaseID))
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
        if (! hcCalc)
        {
            hc = ObjectUtil.hashCode(hc, queryID);
            hc = ObjectUtil.hashCode(hc, databaseID);
            hc = ObjectUtil.hashCode(hc, searchParameters);
            hc = ObjectUtil.hashCode(hc, hits);
            hc = ObjectUtil.hashCode(hc, annotation);
            hcCalc = true;
        }

        return hc;
    }

    public String toString()
    {
        return "SequenceDBSearchResult of " + queryID
            + " against " + databaseID;
    }

    protected ChangeSupport getChangeSupport(ChangeType ct){
      ChangeSupport cs = super.getChangeSupport(ct);

      if(annotationForwarder == null &&
        (ct == null || ct == Annotatable.ANNOTATION)){
        annotationForwarder = new Annotatable.AnnotationForwarder(
            this,
            cs);
        getAnnotation().addChangeListener(
            annotationForwarder,
            Annotatable.ANNOTATION);
      }
      return cs;
    }

}
