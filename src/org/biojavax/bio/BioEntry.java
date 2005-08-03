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
 * BioEntry.java
 *
 * Created on June 14, 2005, 5:20 PM
 */

package org.biojavax.bio;

import java.util.Set;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;
import org.biojavax.RankedCrossRefable;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.Namespace;
import org.biojavax.RankedDocRef;
import org.biojavax.RichAnnotatable;
import org.biojavax.Comment;

/**
 * This class refers to the bioentry table in BioSQL.
 * This also encompasses the bioentry_dbxref table.
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface BioEntry extends RichAnnotatable,RankedCrossRefable,Comparable,Changeable {
    
    public static final ChangeType IDENTIFIER = new ChangeType(
            "This bioentry's identifier has changed",
            "org.biojavax.bio.BioEntry",
            "IDENTIFIER"
            );
    public static final ChangeType DESCRIPTION = new ChangeType(
            "This bioentry's description has changed",
            "org.biojavax.bio.BioEntry",
            "DESCRIPTION"
            );
    public static final ChangeType DIVISION = new ChangeType(
            "This bioentry's division has changed",
            "org.biojavax.bio.BioEntry",
            "DIVISION"
            );
    public static final ChangeType TAXON = new ChangeType(
            "This bioentry's taxon has changed",
            "org.biojavax.bio.BioEntry",
            "TAXON"
            );
    public static final ChangeType SEQVERSION = new ChangeType(
            "This bioentry's sequence version has changed",
            "org.biojavax.bio.BioEntry",
            "SEQVERSION"
            );
    public static final ChangeType RANKEDCROSSREF = new ChangeType(
            "This bioentry's ranked crossrefs changed",
            "org.biojavax.bio.BioEntry",
            "RANKEDCROSSREF"
            );
    public static final ChangeType RANKEDDOCREF = new ChangeType(
            "This bioentry's ranked docrefs changed",
            "org.biojavax.bio.BioEntry",
            "RANKEDDOCREF"
            );
    public static final ChangeType COMMENT = new ChangeType(
            "This bioentry's comments changed",
            "org.biojavax.bio.BioEntry",
            "COMMENT"
            );
    public static final ChangeType RELATIONS = new ChangeType(
            "This bioentry's relations have changed",
            "org.biojavax.bio.BioEntry",
            "RELATIONS"
            );
    
    /**
     * Getter for property namespace.
     * @return Value of property namespace.
     */
    public Namespace getNamespace();
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName();
    
    /**
     * Getter for property accession.
     * @return Value of property accession.
     */
    public String getAccession();
    
    /**
     * Getter for property identifier.
     * @return Value of property identifier.
     */
    public String getIdentifier();
    
    /**
     * Setter for property identifier.
     * @param identifier New value of property identifier.
     * @throws ChangeVetoException in case of objections.
     */
    public void setIdentifier(String identifier) throws ChangeVetoException;
    
    /**
     * Getter for property division.
     * @return Value of property division.
     */
    public String getDivision();
    
    /**
     * Setter for property division.
     * @param division New value of property division.
     * @throws ChangeVetoException in case of objections.
     */
    public void setDivision(String division) throws ChangeVetoException;
    
    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription();
    
    /**
     * Setter for property description.
     * @param description New value of property description.
     * @throws ChangeVetoException in case of objections.
     */
    public void setDescription(String description) throws ChangeVetoException;
    
    /**
     * Getter for property version.
     * @return Value of property version.
     */
    public int getVersion();
    
    /**
     * Getter for property taxon.
     * @return Value of property taxon.
     */
    public NCBITaxon getTaxon();
    
    /**
     * Setter for property taxon.
     * @param taxon New value of property taxon.
     * @throws ChangeVetoException in case of objections.
     */
    public void setTaxon(NCBITaxon taxon) throws ChangeVetoException;
    
    /**
     * Returns a list of all bioentrydocrefs associated with this bioentry. This
     * list is not mutable. If no docrefs are associated, you will get back an
     * empty list.
     */
    public Set getRankedDocRefs();
    
    /**
     * Returns a list of all comments associated with this bioentry. This
     * list is not mutable. If no comments are associated, you will get back an
     * empty list.
     * @return Value of property comments.
     */
    public Set getComments();
    
    /**
     * Returns a list of all relationships associated with this bioentry. This
     * list is not mutable. If no relationships are associated, you will get back an
     * empty list.
     * @return Value of property relationships.
     */
    public Set getRelationships();
    
    /**
     * Adds a ranked docref instance to this bioentry.
     * @param docref the item to add.
     * @throws ChangeVetoException if it doesn't want to add it.
     */
    public void addRankedDocRef(RankedDocRef docref) throws ChangeVetoException;
    
    /**
     * Removes a ranked docref instance from this bioentry.
     * @param docref the item to remove.
     * @throws ChangeVetoException if it doesn't want to remove it.
     */
    public void removeRankedDocRef(RankedDocRef docref) throws ChangeVetoException;
    
    /**
     * Adds a comment instance to this bioentry.
     * @param comment the item to add.
     * @throws ChangeVetoException if it doesn't want to add it.
     */
    public void addComment(Comment comment) throws ChangeVetoException;
    
    /**
     * Removes a comment instance from this bioentry.
     * @param comment the item to remove.
     * @throws ChangeVetoException if it doesn't want to remove it.
     */
    public void removeComment(Comment comment) throws ChangeVetoException;
    
    /**
     * Adds a relation instance to this bioentry.
     * @param relation the item to add.
     * @throws ChangeVetoException if it doesn't want to add it.
     */
    public void addRelationship(BioEntryRelationship relation) throws ChangeVetoException;
    
    /**
     * Removes a relation instance from this bioentry.
     * @param relation the item to remove.
     * @throws ChangeVetoException if it doesn't want to remove it.
     */
    public void removeRelationship(BioEntryRelationship relation) throws ChangeVetoException;
}



