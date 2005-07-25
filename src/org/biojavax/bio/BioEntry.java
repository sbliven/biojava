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

import java.util.List;
import java.util.Set;

import org.biojava.bio.Annotatable;

import org.biojava.ontology.AlreadyExistsException;

import org.biojava.utils.ChangeType;

import org.biojava.utils.ChangeVetoException;

import org.biojava.utils.Changeable;

import org.biojavax.CrossRef;

import org.biojavax.bio.taxa.NCBITaxon;

import org.biojavax.Namespace;

import org.biojavax.LocatedDocumentReference;



/**

 * This class refers to the bioentry table in BioSQL. Note that although it extends

 * Annotatable, it extends it in a very specific way. You should ONLY annotate this

 * with Term objects as the keys, and Strings (or the toString() output of objects)

 * as the values. Anything else is likely to cause you grief if you try and persist

 * it to a database. This also encompasses the bioentry_dbxref table.

 * @author Mark Schreiber

 * @author Richard Holland

 */

public interface BioEntry extends Annotatable,Comparable,Changeable {

    /**

     * A change type.

     */

    public static final ChangeType IDENTIFIER = new ChangeType(

            "This bioentry's identifier has changed",

            "org.biojavax.bio.BioEntry",

            "identifier"

            );

    /**

     * A change type.

     */

    public static final ChangeType DESCRIPTION = new ChangeType(

            "This bioentry's description has changed",

            "org.biojavax.bio.BioEntry",

            "description"

            );

    /**

     * A change type.

     */

    public static final ChangeType DIVISION = new ChangeType(

            "This bioentry's division has changed",

            "org.biojavax.bio.BioEntry",

            "division"

            );

    /**

     * A change type.

     */

    public static final ChangeType TAXON = new ChangeType(

            "This bioentry's taxon has changed",

            "org.biojavax.bio.BioEntry",

            "taxon"

            );

    /**

     * A change type.

     */

    public static final ChangeType RELATIONSHIP = new ChangeType(

            "This bioentry's relationships have changed",

            "org.biojavax.bio.BioEntry",

            "relationships"

            );

    /**

     * A change type.

     */

    public static final ChangeType COMMENTS = new ChangeType(

            "This bioentry's comments have changed",

            "org.biojavax.bio.BioEntry",

            "comments"

            );

    /**

     * A change type.

     */

    public static final ChangeType CROSSREF = new ChangeType(

            "This bioentry's crossrefs have changed",

            "org.biojavax.bio.BioEntry",

            "crossrefs"

            );

    /**

     * A change type.

     */

    public static final ChangeType SEQVERSION = new ChangeType(

            "This bioentry's sequence version has changed",

            "org.biojavax.bio.BioEntry",

            "seqversion"

            );

    /**

     * A change type.

     */

    public static final ChangeType DOCREF = new ChangeType(

            "This bioentry's document references have changed",

            "org.biojavax.bio.BioEntry",

            "docrefs"

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

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

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

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

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

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

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

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     */

    public void setTaxon(NCBITaxon taxon) throws ChangeVetoException;

    

    /**

     * Returns a list of all crossrefs associated with this bioentry. This

     * list is not mutable. If no crossrefs are associated, you will get back an

     * empty list. If the crossrefs have indexes that are not consecutive, then the

     * list will contain nulls at the indexes corresponding to the gaps between

     * the extant crossrefs. eg. If there are only two crossrefs A and B at positions 10

     * and 20 respectively, then the List returned will be of size 20, with nulls

     * at index positions 0-9 and 11-19.

     * @return Value of property crossrefs.

     */

    public List getCrossRefs();

    

    /**

     * Returns the crossref at a given index. If the index is valid but no crossref is

     * found at that position, it will return null. If the index is invalid,

     * an exception will be thrown.

     * @param index the index of the crossref to retrieve.

     * @return The crossref at that index position.

     * @throws IndexOutOfBoundsException if an invalid index is specified.

     */

    public CrossRef getCrossRef(int index) throws IndexOutOfBoundsException;

    

    /**

     * Overwrites the list of crossrefs at the given index position with the crossref

     * supplied. It will overwrite anything already at that position.

     * @param crossref New crossref to write at that position.

     * @param index Position to write crossref at.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the crossref already exists at another index.

     */

    public void setCrossRef(CrossRef crossref, int index) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Adds the crossref to the end of the list of crossrefs, giving it the index of

     * max(all other crossref index positions)+1.

     * @return The position the crossref was added at.

     * @param crossref New crossref to add.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the crossref already exists at another index.

     */

    public int addCrossRef(CrossRef crossref) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Searches for a crossref in the list of all crossrefs, and removes it if it was

     * found.

     * @return True if the crossref was found, false if the crossref was not found.

     * @param crossref the crossref to search for and remove.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     */

    public boolean removeCrossRef(CrossRef crossref) throws ChangeVetoException; 

    

    /**

     * Removes the crossref at a given index. If the index position already had no

     * crossref associated, it returns false. Else, it returns true.

     * @return True if a crossref was found at that position and removed.

     * @param index the index position to remove the crossref from.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws IndexOutOfBoundsException if the index position was invalid.

     */

    public boolean removeCrossRef(int index) throws IndexOutOfBoundsException,ChangeVetoException;

    

    /**

     * Tests for the existence of a crossref in the list.

     * @param crossref the crossref to look for.

     * @return True if the crossref is in the list, false if not.

     */

    public boolean containsCrossRef(CrossRef crossref);

    

    /**

     * Counts cross refs, not including nulls.

     * @return the number of cross refs.

     */

    public int countCrossRefs();

    

    /**

     * Returns a list of all bioentrydocrefs associated with this bioentry. This

     * list is not mutable. If no docrefs are associated, you will get back an

     * empty list. If the docrefs have indexes that are not consecutive, then the

     * list will contain nulls at the indexes corresponding to the gaps between

     * the extant docrefs. eg. If there are only two docrefs A and B at positions 10

     * and 20 respectively, then the List returned will be of size 20, with nulls

     * at index positions 0-9 and 11-19.

     * @return Value of property docrefs.

     */

    public List getDocRefs();

    

    /**

     * Returns the docref at a given index. If the index is valid but no docref is

     * found at that position, it will return null. If the index is invalid,

     * an exception will be thrown.

     * @param index the index of the docref to retrieve.

     * @return The docref at that index position.

     * @throws IndexOutOfBoundsException if an invalid index is specified.

     */

    public LocatedDocumentReference getDocRef(int index) throws IndexOutOfBoundsException;

    

    /**

     * Overwrites the list of docrefs at the given index position with the docref

     * supplied. It will overwrite anything already at that position.

     * @param docref New docref to write at that position.

     * @param index Position to write docref at.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the docref already exists at another index.

     */

    public void setDocRef(LocatedDocumentReference docref, int index) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Adds the docref to the end of the list of docrefs, giving it the index of

     * max(all other docref index positions)+1.

     * @return The position the docref was added at.

     * @param docref New docref to add.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the docref already exists at another index.

     */

    public int addDocRef(LocatedDocumentReference docref) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Searches for a docref in the list of all docrefs, and removes it if it was

     * found.

     * @return True if the docref was found, false if the docref was not found.

     * @param docref the docref to search for and remove.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     */

    public boolean removeDocRef(LocatedDocumentReference docref) throws ChangeVetoException; 

    

    /**

     * Removes the docref at a given index. If the index position already had no

     * docref associated, it returns false. Else, it returns true.

     * @return True if a docref was found at that position and removed.

     * @param index the index position to remove the docref from.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws IndexOutOfBoundsException if the index position was invalid.

     */

    public boolean removeDocRef(int index) throws IndexOutOfBoundsException,ChangeVetoException;

    

    /**

     * Tests for the existence of a docref in the list.

     * @param docref the docref to look for.

     * @return True if the docref is in the list, false if not.

     */

    public boolean containsDocRef(LocatedDocumentReference docref);

    

    /**

     * Counts doc refs, not including nulls.

     * @return the number of doc refs.

     */

    public int countDocRefs();

    

    /**

     * Returns a list of all relationships associated with this bioentry. This

     * list is not mutable. If no relationships are associated, you will get back an

     * empty list. If the relationships have indexes that are not consecutive, then the

     * list will contain nulls at the indexes corresponding to the gaps between

     * the extant relationships. eg. If there are only two relationships A and B at positions 10

     * and 20 respectively, then the List returned will be of size 20, with nulls

     * at index positions 0-9 and 11-19.

     * @return Value of property relationships.

     */

    public Set getBioEntryRelationships();

    /**

     * Adds the relationship to the end of the list of relationships, giving it the index of

     * max(all other relationship index positions)+1.

     * @return The position the relationship was added at.

     * @param relationship New relationship to add.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the relationship already exists at another index.

     */

    public void addBioEntryRelationship(BioEntryRelationship relationship) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Searches for a relationship in the list of all relationships, and removes it if it was

     * found.

     * @return True if the relationship was found, false if the relationship was not found.

     * @param relationship the relationship to search for and remove.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     */

    public boolean removeBioEntryRelationship(BioEntryRelationship relationship) throws ChangeVetoException;
    

    /**

     * Tests for the existence of a relationship in the list.

     * @param relationship the relationship to look for.

     * @return True if the relationship is in the list, false if not.

     */

    public boolean containsBioEntryRelationship(BioEntryRelationship relationship);

    

    /**

     * Counts relationships.

     * @return the number of relationships.

     */

    public int countBioEntryRelationships();

    

    /**

     * Returns a list of all comments associated with this bioentry. This

     * list is not mutable. If no comments are associated, you will get back an

     * empty list. If the comments have indexes that are not consecutive, then the

     * list will contain nulls at the indexes corresponding to the gaps between

     * the extant comments. eg. If there are only two comments A and B at positions 10

     * and 20 respectively, then the List returned will be of size 20, with nulls

     * at index positions 0-9 and 11-19.

     * @return Value of property comments.

     */

    public List getComments();

    

    /**

     * Returns the comment at a given index. If the index is valid but no comment is

     * found at that position, it will return null. If the index is invalid,

     * an exception will be thrown.

     * @param index the index of the comment to retrieve.

     * @return The comment at that index position.

     * @throws IndexOutOfBoundsException if an invalid index is specified.

     */

    public BioEntryComment getComment(int index) throws IndexOutOfBoundsException;

    

    /**

     * Overwrites the list of comments at the given index position with the comment

     * supplied. It will overwrite anything already at that position.

     * @param comment New comment to write at that position.

     * @param index Position to write comment at.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the comment already exists at another index.

     */

    public void setComment(BioEntryComment comment, int index) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Adds the comment to the end of the list of comments, giving it the index of

     * max(all other comment index positions)+1.

     * @return The position the comment was added at.

     * @param comment New comment to add.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws AlreadyExistsException if the comment already exists at another index.

     */

    public int addComment(BioEntryComment comment) throws AlreadyExistsException,ChangeVetoException;

    

    /**

     * Searches for a comment in the list of all comments, and removes it if it was

     * found.

     * @return True if the comment was found, false if the comment was not found.

     * @param comment the comment to search for and remove.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     */

    public boolean removeComment(BioEntryComment comment) throws ChangeVetoException;

    

    /**

     * Removes the comment at a given index. If the index position already had no

     * comment associated, it returns false. Else, it returns true.

     * @return True if a comment was found at that position and removed.

     * @param index the index position to remove the comment from.

     * @throws org.biojava.utils.ChangeVetoException in case of objections.

     * @throws IndexOutOfBoundsException if the index position was invalid.

     */

    public boolean removeComment(int index) throws IndexOutOfBoundsException,ChangeVetoException;

    

    /**

     * Tests for the existence of a comment in the list.

     * @param comment the comment to look for.

     * @return True if the comment is in the list, false if not.

     */

    public boolean containsComment(BioEntryComment comment);

    

    /**

     * Counts comments, not including nulls.

     * @return the number of comments.

     */

    public int countComments();


}



