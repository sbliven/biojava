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
 * Namespace.java
 *
 * Created on June 14, 2005, 4:31 PM
 */

package org.biojavax;

import java.net.URI;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;

/**
 * The namespace of an entry in a database schema
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface Namespace extends Comparable,Changeable {
    
    /**
     * A change type.
     */
    public static final ChangeType NAME = new ChangeType(
            "This namespace's name has changed",
            "org.biojavax.Namespace",
            "name"
            );
    /**
     * A change type.
     */
    public static final ChangeType AUTHORITY = new ChangeType(
            "This namespace's authority has changed",
            "org.biojavax.Namespace",
            "authority"
            );
    /**
     * A change type.
     */
    public static final ChangeType DESCRIPTION = new ChangeType(
            "This namespace's description has changed",
            "org.biojavax.Namespace",
            "description"
            );
    /**
     * A change type.
     */
    public static final ChangeType ACRONYM = new ChangeType(
            "This namespace's acronym has changed",
            "org.biojavax.Namespace",
            "acronym"
            );
    /**
     * A change type.
     */
    public static final ChangeType URI = new ChangeType(
            "This namespace's URI has changed",
            "org.biojavax.Namespace",
            "URI"
            );
    
    /**
     * Getter for property name.
     * @return The name of the namespace.
     */
    public String getName();
    
    /**
     * Getter for property authority.
     * @return the name of the namespace authority.
     */
    public String getAuthority();
    
    /**
     * Setter for property authority.
     * @param authority the name of the namespace authority.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setAuthority(String authority) throws ChangeVetoException;
    
    /**
     * Getter for property description.
     * @return the description of the namespace.
     */
    public String getDescription();
    
    /**
     * Setter for property description.
     * @param description the description of the namespace.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setDescription(String description) throws ChangeVetoException;
    
    /**
     * Getter for property acronym.
     * @return the acronym for the namespace.
     */
    public String getAcronym();
    
    /**
     * Setter for property acronym.
     * @param acronym the acronym for the namespace.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setAcronym(String acronym) throws ChangeVetoException;
    
    /**
     * Getter for property URI.
     * @return the URI of the authority.
     */
    public URI getURI();
    
    /**
     * Setter for property URI.
     * @param URI the URI of the authority.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setURI(URI URI) throws ChangeVetoException;
}
