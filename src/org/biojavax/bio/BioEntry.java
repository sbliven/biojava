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

import org.biojava.bio.Annotatable;

import org.biojava.utils.ChangeType;

import org.biojava.utils.ChangeVetoException;

import org.biojava.utils.Changeable;

import org.biojavax.bio.taxa.NCBITaxon;

import org.biojavax.Namespace;




/**
 *
 * This class refers to the bioentry table in BioSQL. Note that although it extends
 *
 * Annotatable, it extends it in a very specific way. You should ONLY annotate this
 *
 * with Term objects as the keys, and Strings (or the toString() output of objects)
 *
 * as the values. Anything else is likely to cause you grief if you try and persist
 *
 * it to a database. This also encompasses the bioentry_dbxref table.
 *
 * @author Mark Schreiber
 *
 * @author Richard Holland
 *
 */

public interface BioEntry extends Annotatable,Comparable,Changeable {
    
    /**
     *
     * A change type.
     *
     */
    
    public static final ChangeType IDENTIFIER = new ChangeType(
            
            "This bioentry's identifier has changed",
            
            "org.biojavax.bio.BioEntry",
            
            "identifier"
            
            );
    
    /**
     *
     * A change type.
     *
     */
    
    public static final ChangeType DESCRIPTION = new ChangeType(
            
            "This bioentry's description has changed",
            
            "org.biojavax.bio.BioEntry",
            
            "description"
            
            );
    
    /**
     *
     * A change type.
     *
     */
    
    public static final ChangeType DIVISION = new ChangeType(
            
            "This bioentry's division has changed",
            
            "org.biojavax.bio.BioEntry",
            
            "division"
            
            );
    
    /**
     *
     * A change type.
     *
     */
    
    public static final ChangeType TAXON = new ChangeType(
            
            "This bioentry's taxon has changed",
            
            "org.biojavax.bio.BioEntry",
            
            "taxon"
            
            );
    
    
    /**
     *
     * A change type.
     *
     */
    
    public static final ChangeType SEQVERSION = new ChangeType(
            
            "This bioentry's sequence version has changed",
            
            "org.biojavax.bio.BioEntry",
            
            "seqversion"
            
            );
    
    
    
    /**
     *
     * Getter for property namespace.
     *
     * @return Value of property namespace.
     *
     */
    
    public Namespace getNamespace();
    
    
    
    /**
     *
     * Getter for property name.
     *
     * @return Value of property name.
     *
     */
    
    public String getName();
    
    
    
    /**
     *
     * Getter for property accession.
     *
     * @return Value of property accession.
     *
     */
    
    public String getAccession();
    
    
    
    /**
     *
     * Getter for property identifier.
     *
     * @return Value of property identifier.
     *
     */
    
    public String getIdentifier();
    
    
    
    /**
     *
     * Setter for property identifier.
     *
     * @param identifier New value of property identifier.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setIdentifier(String identifier) throws ChangeVetoException;
    
    
    
    /**
     *
     * Getter for property division.
     *
     * @return Value of property division.
     *
     */
    
    public String getDivision();
    
    
    
    /**
     *
     * Setter for property division.
     *
     * @param division New value of property division.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setDivision(String division) throws ChangeVetoException;
    
    
    
    /**
     *
     * Getter for property description.
     *
     * @return Value of property description.
     *
     */
    
    public String getDescription();
    
    
    
    /**
     *
     * Setter for property description.
     *
     * @param description New value of property description.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setDescription(String description) throws ChangeVetoException;
    
    
    
    /**
     *
     * Getter for property version.
     *
     * @return Value of property version.
     *
     */
    
    public int getVersion();
    
    
    
    /**
     *
     * Getter for property taxon.
     *
     * @return Value of property taxon.
     *
     */
    
    public NCBITaxon getTaxon();
    
    
    
    /**
     *
     * Setter for property taxon.
     *
     * @param taxon New value of property taxon.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setTaxon(NCBITaxon taxon) throws ChangeVetoException;
    
    
    
    /**
     *
     * Returns a list of all crossrefs associated with this bioentry. This
     *
     * list is not mutable. If no crossrefs are associated, you will get back an
     *
     * empty list. If the crossrefs have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant crossrefs. eg. If there are only two crossrefs A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property crossrefs.
     *
     */
    
    public Set getCrossRefs();
    
    
    /**
     *
     * Returns a list of all bioentrydocrefs associated with this bioentry. This
     *
     * list is not mutable. If no docrefs are associated, you will get back an
     *
     * empty list. If the docrefs have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant docrefs. eg. If there are only two docrefs A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property docrefs.
     *
     */
    
    public Set getDocRefs();
    
    
    
    /**
     *
     * Returns a list of all relationships associated with this bioentry. This
     *
     * list is not mutable. If no relationships are associated, you will get back an
     *
     * empty list. If the relationships have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant relationships. eg. If there are only two relationships A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property relationships.
     *
     */
    
    public Set getBioEntryRelationships();
    
    
    /**
     *
     * Returns a list of all comments associated with this bioentry. This
     *
     * list is not mutable. If no comments are associated, you will get back an
     *
     * empty list. If the comments have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant comments. eg. If there are only two comments A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property comments.
     *
     */
    
    public Set getComments();
    
}



