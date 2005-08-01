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
 * NCBITaxon.java
 *
 * Created on June 14, 2005, 5:18 PM
 */

package org.biojavax.bio.taxa;
import java.util.Set;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;


/**
 * Represents an NCBI Taxon entry, a combination of the taxon and taxon_name
 * tables in BioSQL.
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface NCBITaxon extends Comparable,Changeable {
    
    public static final ChangeType NAMES = new ChangeType(
            "This taxon's names have changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "names"
            );
    public static final ChangeType PARENT = new ChangeType(
            "This taxon's parent has changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "parent"
            );
    public static final ChangeType NODERANK = new ChangeType(
            "This taxon's node rank has changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "noderank"
            );
    public static final ChangeType GENETICCODE = new ChangeType(
            "This taxon's genetic code has changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "geneticcode"
            );
    public static final ChangeType MITOGENETICCODE = new ChangeType(
            "This taxon's mito genetic code has changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "mitogeneticcode"
            );
    public static final ChangeType LEFTVALUE = new ChangeType(
            "This taxon's left value has changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "leftvalue"
            );
    public static final ChangeType RIGHTVALUE = new ChangeType(
            "This taxon's right value has changed",
            "org.biojavax.bio.taxa.NCBITaxon",
            "rightvalue"
            );
    
    /**
     * Use this to define scientific names for things. There should
     * usually only be one scientific name for an organism.
     */
    public static final String SCIENTIFIC = "SCIENTIFIC";
    
    /**
     * Use this to define common names for things. There can be as many
     * common names as you like.
     */
    public static final String COMMON = "COMMON";
    
    /**
     * Returns all the name classes available for a taxon.
     * @return a set of name classes, or the empty set if there are none.
     */
    public Set getNameClasses();
    
    /**
     * Returns all the names available for a taxon in a given class.
     * @param nameClass the name class to retrieve names from.
     * @return a set of names, or the empty set if there are none.
     * @throws IllegalArgumentException if the name is null.
     */
    public Set getNames(String nameClass) throws IllegalArgumentException;
    
    /**
     * Adds the name to this taxon in the given name class. Neither can be null.
     * @param nameClass the class to add the name in.
     * @param name the name to add.
     * @throws ChangeVetoException in case of objections.
     * @throws IllegalArgumentException if the name is null.
     */
    public void addName(String nameClass, String name) throws IllegalArgumentException,ChangeVetoException;
    
    /**
     * Removes the name from the given name class. Neither can be null.
     * @return True if the name was found and removed, false otherwise.
     * @param nameClass the class to remove the name from.
     * @param name the name to remove.
     * @throws ChangeVetoException in case of objections.
     * @throws IllegalArgumentException if the name is null.
     */
    public boolean removeName(String nameClass, String name) throws IllegalArgumentException,ChangeVetoException;
    
    /**
     * Tests for the presence of a name in a given class. Neither can be null.
     * @param nameClass the class to look the name up in.
     * @param name the name to text for existence of.
     * @return True if the name exists in that class, false otherwise.
     * @throws IllegalArgumentException if the name is null.
     */
    public boolean containsName(String nameClass, String name) throws IllegalArgumentException;
    
    /**
     * Getter for property parent.
     * @return Value of property parent.
     */
    public int getParentNCBITaxID();
    
    /**
     * Setter for property parent.
     * @param parent New value of property parent.
     * @throws ChangeVetoException in case of objections.
     */
    public void setParentNCBITaxID(int parent) throws ChangeVetoException;
    
    /**
     * Getter for property NCBITaxID. Returns Persistent.NULL_INTEGER if null.
     * @return Value of property NCBITaxID.
     */
    public int getNCBITaxID();
    
    /**
     * Getter for property nodeRank.
     * @return Value of property nodeRank.
     */
    public String getNodeRank();
    
    /**
     * Setter for property nodeRank.
     * @param nodeRank New value of property nodeRank.
     * @throws ChangeVetoException in case of objections.
     */
    public void setNodeRank(String nodeRank) throws ChangeVetoException;
    
    /**
     * Getter for property geneticCode.
     * @return Value of property geneticCode.
     */
    public int getGeneticCode();
    
    /**
     * Setter for property geneticCode.
     * @param geneticCode New value of property geneticCode.
     * @throws ChangeVetoException in case of objections.
     */
    public void setGeneticCode(int geneticCode) throws ChangeVetoException;
    
    /**
     * Getter for property mitoGeneticCode.
     * @return Value of property mitoGeneticCode.
     */
    public int getMitoGeneticCode();
    
    /**
     * Setter for property mitoGeneticCode.
     * @param mitoGeneticCode New value of property mitoGeneticCode.
     * @throws ChangeVetoException in case of objections.
     */
    public void setMitoGeneticCode(int mitoGeneticCode) throws ChangeVetoException;
    
    /**
     * Getter for property leftValue.
     * @return Value of property leftValue.
     */
    public int getLeftValue();
    
    /**
     * Setter for property leftValue.
     * @param leftValue New value of property leftValue.
     * @throws ChangeVetoException in case of objections.
     */
    public void setLeftValue(int leftValue) throws ChangeVetoException;
    
    /**
     * Getter for property rightValue.
     * @return Value of property rightValue.
     */
    public int getRightValue();
    
    /**
     * Setter for property rightValue.
     * @param rightValue New value of property rightValue.
     * @throws ChangeVetoException in case of objections.
     */
    public void setRightValue(int rightValue) throws ChangeVetoException;
    
}

