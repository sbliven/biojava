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
 * RichFeatureRelationship.java
 *
 * Created on June 14, 2005, 5:33 PM
 */

package org.biojavax.bio.seq;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;
import org.biojavax.bio.db.BioDBUtils;
import org.biojavax.ontology.ComparableTerm;

/**
 * Represents the relation between two bioentries. The bioentry_relationship in
 * BioSQL is what this represents.
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface RichFeatureRelationship extends Comparable,Changeable {
    
    /**
     * The default term used for defining the relationship between features.
     */
    public static final ComparableTerm DEFAULT_FEATURE_RELATIONSHIP_TERM = BioDBUtils.getOntologyTerm(BioDBUtils.DEFAULT_ONTOLOGY, "contains");
    
    public static final ChangeType RANK = new ChangeType(
            "This feature relationship's rank has changed",
            "org.biojavax.bio.seq.RichSeqFeatRelationship",
            "RANK"
            );
    
    /**
     * Setter for property rank.
     * @param rank Value of property rank.
     * @throws ChangeVetoException if the rank is untasty.
     */
    public void setRank(int rank) throws ChangeVetoException;
    
    /**
     * Getter for property rank.
     * @return Value of property rank.
     */
    public int getRank();
    
    /**
     * Getter for property object.
     * @return Value of property object.
     */
    public RichFeature getObject();
    
    /**
     * Getter for property subject.
     * @return Value of property subject.
     */
    public RichFeature getSubject();
    
    /**
     * Getter for property term.
     * @return Value of property term.
     */
    public ComparableTerm getTerm();
    
}
