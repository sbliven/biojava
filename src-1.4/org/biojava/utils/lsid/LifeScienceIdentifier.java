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

package org.biojava.utils.lsid;

import java.io.Serializable;

/**
 * Life Science Identifier (LSID) interface.
 *
 * LSID syntax:
 * <p>
 * &lt;authority&gt;:&lt;namespace&gt;:&lt;value&gt;:&lt;version&gt;:&lt;security&gt;
 * <p>
 * The elements of a LSID are as follows:
 * <ul>
 * <li>authority = &lt;authority&gt; identifies the organization
 * <li>namespace = &lt;namespace&gt; namespace to scope the identifier value
 * <li>object_id = &lt;object_id&gt; identifier of the object within namespace
 * <li>version = &lt;version&gt; optional version information
 * <li>security = &lt;security&gt; optional security information
 * </ul>
 */
public  interface LifeScienceIdentifier
    extends Immutable, Serializable {

    // extends Immutable
    
    // public int hashCode();
    // public String toString();
    // public boolean equals(Object object);

    /**
     * Return the authority for this identifier.
     */
    public String getAuthority();

    /**
     * Return the namespace for this identifier
     * within the authority.
     */
    public String getNamespace();

    /**
     * Return the object id of this identifier.
     */
    public Object getObjectId();

    /**
     * Return the version of the object id of
     * this identifier.  May return null.
     */
    public Object getVersion();

    /**
     * Return the security information of this identifier.
     * May return null.
     */
    //public Object getSecurity();
}
