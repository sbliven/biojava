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
 * DocumentReference.java
 *
 * Created on June 14, 2005, 5:10 PM
 */

package org.biojavax;

import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * Represents a documentary reference, the reference table in BioSQL.
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface DocumentReference extends Comparable,Changeable {
    
    /**
     * A change type.
     */
    public static final ChangeType CROSSREF = new ChangeType(
      "This reference's crossref has changed",
      "org.biojavax.DocumentReference",
      "crossref"
            );
    /**
     * A change type.
     */
    public static final ChangeType AUTHORS = new ChangeType(
      "This reference's authors have changed",
      "org.biojavax.DocumentReference",
      "authors"
            );
    /**
     * A change type.
     */
    public static final ChangeType LOCATION = new ChangeType(
      "This reference's location has changed",
      "org.biojavax.DocumentReference",
      "location"
            );
    /**
     * A change type.
     */
    public static final ChangeType TITLE = new ChangeType(
      "This reference's title has changed",
      "org.biojavax.DocumentReference",
      "title"
            );
    /**
     * A change type.
     */
    public static final ChangeType CRC = new ChangeType(
      "This reference's CRC has changed",
      "org.biojavax.DocumentReference",
      "crc"
            );
    
    /**
     * Getter for property crossref.
     * @return Value of property crossref.
     */
    public CrossRef getCrossref();

    /**
     * Setter for property crossref.
     * @param crossref New value of property crossref.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setCrossref(CrossRef crossref) throws ChangeVetoException;

    /**
     * Getter for property location.
     * @return Value of property location.
     */
    public String getLocation();

    /**
     * Getter for property title.
     * @return Value of property title.
     */
    public String getTitle();

    /**
     * Setter for property title.
     * @param title New value of property title.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setTitle(String title) throws ChangeVetoException;

    /**
     * Getter for property authors.
     * @return Value of property authors.
     */
    public String getAuthors();

    /**
     * Getter for property CRC.
     * @return Value of property CRC.
     */
    public String getCRC();

    /**
     * Setter for property CRC.
     * @param CRC New value of property CRC.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setCRC(String CRC) throws ChangeVetoException;
    
}
