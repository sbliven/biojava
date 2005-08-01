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

/*
 * RichLocation.java
 *
 * Created on July 28, 2005, 5:29 PM
 */
package org.biojavax.bio.seq;

import java.util.Set;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.RichAnnotatable;
import org.biojavax.ontology.ComparableTerm;

/**
 * Holds enough info about locations to keep BioSQL happy if needs be.
 *
 * @author Richard Holland
 */
public interface RichLocation extends Location,RichAnnotatable {
    
    public static final ChangeType CROSSREF = new ChangeType(
            "This location's crossref has changed",
            "org.biojavax.bio.seq.RichLocation",
            "crossref"
            );
    public static final ChangeType NOTE = new ChangeType(
            "This location's notes have changed",
            "org.biojavax.bio.seq.RichLocation",
            "note"
            );
    public static final ChangeType TERM = new ChangeType(
            "This location's term has changed",
            "org.biojavax.bio.seq.RichLocation",
            "term"
            );
    public static final ChangeType STRAND = new ChangeType(
            "This location's strand has changed",
            "org.biojavax.bio.seq.RichLocation",
            "strand"
            );
    public static final ChangeType RANK = new ChangeType(
            "This location's rank has changed",
            "org.biojavax.bio.seq.RichLocation",
            "rank"
            );
    public static final ChangeType MIN = new ChangeType(
            "This location's min has changed",
            "org.biojavax.bio.seq.RichLocation",
            "min"
            );
    public static final ChangeType MAX = new ChangeType(
            "This location's max has changed",
            "org.biojavax.bio.seq.RichLocation",
            "max"
            );
    public static final ChangeType PARENT = new ChangeType(
            "This location's parent has changed",
            "org.biojavax.bio.seq.RichLocation",
            "parent"
            );
    
    /**
     * Retrieves the crossref associated with this location.
     * @return the crossref.
     */
    public CrossRef getCrossRef();
    
    /**
     * Sets the crossref for this location.
     * @param crossref the crossref this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setCrossRef(CrossRef crossref) throws ChangeVetoException;
    
    /**
     * Retrieves the term associated with this location.
     * @return the term.
     */
    public ComparableTerm getTerm();
    
    /**
     * Sets the term for this location.
     * @param term the term this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setTerm(ComparableTerm term) throws ChangeVetoException;
    
    /**
     * Retrieves the strand associated with this location.
     * @return the strand.
     */
    public Strand getStrand();
    
    /**
     * Sets the strand for this location.
     * @param strand the strand this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setStrand(Strand strand) throws ChangeVetoException;
    
    /**
     * Retrieves the rank associated with this location.
     * @return the rank.
     */
    public int getRank();
    
    /**
     * Sets the rank for this location.
     * @param rank the rank this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setRank(int rank) throws ChangeVetoException;
    
    /**
     * Sets the min for this location.
     * @param min the min this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setMin(int min) throws ChangeVetoException;
    
    /**
     * Sets the max for this location.
     * @param max the max this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setMax(int max) throws ChangeVetoException;
    
    /**
     * Retrieves the parent associated with this location.
     * @return the parent.
     */
    public RichFeature getParentFeature();
    
    /**
     * Sets the parent for this location.
     * @param parent the parent this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setParentFeature(RichFeature feature) throws ChangeVetoException;
    
    /**
     * Retrieves the blocks associated with this location.
     * @return the blocks.
     */
    public Set getBlocks();
    
    /**
     * Sets the blocks for this location.
     * @param blocks the blocks this location should adopt.
     * @throws ChangeVetoException in case of error.
     */
    public void setBlocks(Set blocks) throws ChangeVetoException;
}
