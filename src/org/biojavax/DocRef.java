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
 *      http://www.biojava.orDocRef
 */

package org.biojavax;

import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;


/**
 * Represents a documentary reference. Relates to the reference table 
 * in BioSQL.
 * @author Mark Schreiber
 * @author Richard Holland
 * @see RankedDocRef
 */
public interface DocRef extends Comparable,Changeable {
    
    public static final ChangeType CROSSREF = new ChangeType(
            "This reference's crossref has changed",
            "org.biojavax.DocRef",
            "CROSSREF"
            );
    public static final ChangeType AUTHORS = new ChangeType(
            "This reference's authors have changed",
            "org.biojavax.DocRef",
            "AUTHORS"
            );
    public static final ChangeType LOCATION = new ChangeType(
            "This reference's location has changed",
            "org.biojavax.DocRef",
            "LOCATION"
            );
    public static final ChangeType TITLE = new ChangeType(
            "This reference's title has changed",
            "org.biojavax.DocRef",
            "TITLE"
            );
    public static final ChangeType REMARK = new ChangeType(
            "This reference's remark has changed",
            "org.biojavax.DocRef",
            "REMARK"
            );
    
    /**
     * The document reference may refer to an object in another database. If so,
     * this method will return that reference.
     * @return Value of property crossref.
     */
    public CrossRef getCrossref();
    
    /**
     * The document reference may refer to an object in another database. Use this
     * method to set that reference. Null will unset it.
     * @param crossref New value of property crossref.
     * @throws ChangeVetoException in case of objections.
     */
    public void setCrossref(CrossRef crossref) throws ChangeVetoException;
    
    /**
     * Returns a textual description of the document reference. This field is 
     * immutable so should be set using the constructor of the implementing class.
     * @return Value of property location.
     */
    public String getLocation();
    
    /**
     * Returns the title of the document reference.
     * @return Value of property title.
     */
    public String getTitle();
    
    /**
     * Sets the title of the document reference. Null will unset it.
     * @param title New value of property title.
     * @throws ChangeVetoException in case of objections.
     */
    public void setTitle(String title) throws ChangeVetoException;
    
    /**
     * Returns the authors of the document reference. This field is 
     * immutable so should be set using the constructor of the implementing class.
     * It will usually be in the form "Jones H., Bloggs J et al" or similar -
     * a human-readable text value.
     * @return Value of property authors.
     */
    public String getAuthors();
    
    /**
     * Returns a CRC64 checksum of this document reference, allowing for easy
     * comparisons with other document references.
     * @return Value of property CRC.
     */
    public String getCRC();
    
    /**
     * If remarks have been made about this document reference, this method
     * will return them.
     * @return Value of property Remark.
     */
    public String getRemark();
    
    /**
     * Set the remarks for this document reference using this method. Remarks
     * can be anything, it is derived from the equivalent field in the GenBank
     * format.
     * @param Remark New value of property Remark.
     * @throws ChangeVetoException in case of objections.
     */
    public void setRemark(String Remark) throws ChangeVetoException;
    
}
