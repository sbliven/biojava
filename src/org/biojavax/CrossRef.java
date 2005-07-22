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

 * CrossRef.java

 *

 * Created on June 14, 2005, 4:53 PM

 */



package org.biojavax;

import java.util.List;

import org.biojava.ontology.AlreadyExistsException;

import org.biojava.utils.ChangeType;

import org.biojava.utils.ChangeVetoException;

import org.biojava.utils.Changeable;

import org.biojavax.ontology.ComparableTerm;



/**

 * Represents a cross reference to another database, the dbxref table in BioSQL.

 * @author Mark Schreiber

 * @author Richard Holland

 */

public interface CrossRef extends Comparable,Changeable {

    

    /**

     * The TERM change type when terms are added or removed.

     */

    public static final ChangeType TERM = new ChangeType(

      "This crossref's terms have changed",

      "org.biojavax.CrossRef",

      "terms"

            );

    

    /**

     * Returns a list of all terms associated with this cross reference. This 

     * list is not mutable. If no terms are associated, you will get back an 

     * empty list. If the terms have indexes that are not consecutive, then the

     * list will contain nulls at the indexes corresponding to the gaps between

     * the extant terms. eg. If there are only two terms A and B at positions 10

     * and 20 respectively, then the List returned will be of size 20, with nulls

     * at index positions 0-9 and 11-19.

     * @return Value of property terms.

     */

    public List getTerms();

    

    /**

     * Returns the term at a given index. If the index is valid but no term is

     * found at that position, it will return null. If the index is invalid,

     * an exception will be thrown.

     * @param index the index of the term to retrieve.

     * @return The term at that index position.

     * @throws IndexOutOfBoundsException if an invalid index is specified.

     */

    public ComparableTerm getTerm(int index) throws IndexOutOfBoundsException;
    public String getTermValue(int index) throws IndexOutOfBoundsException;



    /**

     * Overwrites the list of terms at the given index position with the term

     * supplied. It will overwrite anything already at that position.

     * @param term New term to write at that position.

     * @param index Position to write term at.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the term already exists at another index.

     * @throws IllegalArgumentException if the term is null.

     */

    public void setTerm(ComparableTerm term, String value, int index) throws AlreadyExistsException,IllegalArgumentException,ChangeVetoException;



    /**

     * Adds the term to the end of the list of terms, giving it the index of

     * max(all other term index positions)+1.

     * @return The position the term was added at.

     * @param term New term to add.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the term already exists at another index.

     * @throws IllegalArgumentException if the term is null.

     */

    public int addTerm(ComparableTerm term, String value) throws AlreadyExistsException,IllegalArgumentException,ChangeVetoException;



    /**

     * Searches for a term in the list of all terms, and removes it if it was

     * found.

     * @return True if the term was found, false if the term was not found.

     * @param term the term to search for and remove.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws IllegalArgumentException if the term is null.

     */

    public boolean removeTerm(ComparableTerm term) throws IllegalArgumentException,ChangeVetoException;

    

    /**

     * Removes the term at a given index. If the index position already had no

     * term associated, it returns false. Else, it returns true.

     * @return True if a term was found at that position and removed.

     * @param index the index position to remove the term from.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws IndexOutOfBoundsException if the index position was invalid.

     */

    public boolean removeTerm(int index) throws IndexOutOfBoundsException,ChangeVetoException;

    

    /** 

     * Tests for the existence of a term in the list.

     * @param term the term to look for.

     * @return True if the term is in the list, false if not.

     * @throws IllegalArgumentException if the term is null.

     */

    public boolean containsTerm(ComparableTerm term) throws IllegalArgumentException;



    /**

     * Getter for property dbname.

     * @return Value of property dbname.

     */

    public String getDbname();



    /**

     * Getter for property accession.

     * @return Value of property accession.

     */

    public String getAccession();



    /**

     * Getter for property version.

     * @return Value of property version.

     */

    public int getVersion();

    

}

