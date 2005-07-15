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
 * SimpleNamespace.java
 *
 * Created on June 15, 2005, 6:04 PM
 */

package org.biojavax;
import java.net.URI;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;


/**
 * A basic Namespace implemenation.
 *
 * Equality is based on the name of the namespace.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleNamespace extends AbstractChangeable implements Namespace {
    
    /**
     * The name for this namespace.
     */
    private String name;
    /**
     * The acronym for this namespace.
     */
    private String acronym;
    /**
     * The authority for this namespace.
     */
    private String authority;
    /**
     * The description for this namespace.
     */
    private String description;
    /**
     * The URI for this namespace.
     */
    private URI URI;
    
    /**
     * Creates a new instance of SimpleNamespace
     * @param name the name of the namespace.
     */
    public SimpleNamespace(String name) {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        this.name = name;
        this.acronym = null;
        this.authority = null;
        this.description = null;
        this.URI = null;
    }
    
    /**
     * Setter for property acronym.
     * @param acronym the acronym for the namespace.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setAcronym(String acronym) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.ACRONYM)) {
            this.acronym = acronym;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.ACRONYM,
                    acronym,
                    this.acronym
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.ACRONYM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.acronym = acronym;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Setter for property authority.
     * @param authority the name of the namespace authority.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setAuthority(String authority) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.AUTHORITY)) {
            this.authority = authority;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.AUTHORITY,
                    authority,
                    this.authority
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.AUTHORITY);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.authority = authority;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Setter for property description.
     * @param description the description of the namespace.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setDescription(String description) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.DESCRIPTION)) {
            this.description = description;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.DESCRIPTION,
                    description,
                    this.description
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.DESCRIPTION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.description = description;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Setter for property URI.
     * @param URI the URI of the authority.
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     */
    public void setURI(URI URI) throws ChangeVetoException {
        if(!this.hasListeners(Namespace.URI)) {
            this.URI = URI;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    Namespace.URI,
                    URI,
                    this.URI
                    );
            ChangeSupport cs = this.getChangeSupport(Namespace.URI);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.URI = URI;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * Getter for property acronym.
     * @return the acronym for the namespace.
     */
    public String getAcronym() {
        return this.acronym;
    }
    
    /**
     * Getter for property authority.
     * @return the name of the namespace authority.
     */
    public String getAuthority() {
        return this.authority;
    }
    
    /**
     * Getter for property description.
     * @return the description of the namespace.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Getter for property name.
     * @return The name of the namespace.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Getter for property URI.
     * @return the URI of the authority.
     */
    public URI getURI() {
        return this.URI;
    }
    
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @return a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     * @param o the Object to be compared.
     */
    public int compareTo(Object o) {
        Namespace them = (Namespace)o;
        return this.getName().compareTo(them.getName());
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * Equality is based on the name of the namespace.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (obj==null || !(obj instanceof Namespace)) return false;
        else {
            Namespace them = (Namespace)obj;
            return this.getName().equals(them.getName());
        }
    }
    
    
    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public int hashCode() {
        int hash = 17;
        return 31*hash + this.getName().hashCode();
    }
    
    /**
     * Returns the name of the name space.
     * @see #getName()
     * @return  a string representation of the object.
     */
    public String toString() {
        return this.getName();
    }
    
}
