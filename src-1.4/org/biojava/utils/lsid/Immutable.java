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

/**
 * Defines an interface implemented by classes that are immutable.
 * To comply:
 * <ul>
 * <li>The class must be final
 * <li>All fields must be private
 * <li>The class must have no set methods, or other methods that modify
 *  the object
 * </ul>
 *
 * <p><b>note</b>:<br>
 * This interface cut n' pasted from 
 * <code>org.apache.commons.pattern.immutable.Immutable</code>,
 * see <a href="http://jakarta.apache.org/commons">http://jakarta.apache.org/commons</a>.
 * The original would have been used instead of copied here, but the
 * pattern package has not been released by the commons project yet.  It still
 * remains in the jakarta-commons-sandbox repository.
 * </p>
 *
 * @author <a href="mailto:scolebourne@joda.org">Stephen Colebourne</a>
 */
public interface Immutable {

    /**
     * Compare this object to another. Immutable objects are often used
     * in Maps, so the equals() method should be designed with care.
     *
     * @see java.lang.Object#equals()
     * @return true if this object equals the passed in object
     */
    public boolean equals(Object object);
    
    /**
     * Get a hash code that complies with the normal rules laid out in
     * <code>java.lang.Object</code>. Immutable objects are often used
     * in Maps, so the hashCode() method should be designed with care.
     *
     * @see java.lang.Object#hashCode()
     * @return an integer hashcode for the object
     */
    public int hashCode();
    
    /**
     * Return the entire information about the identifier as a String.
     * <p>
     * The method may not return null. The format of the String should
     * be such that it can be parsed back to recreate the object. Normally,
     * the class will have a static <code>parse(String)</code> method for
     * this purpose.
     * <p>
     * This method is not a debugging method, and thus StringBuffer
     * should be used to append Strings, not the + operator for
     * performance reasons.
     *
     * @return a string representing the entire state of the immutable object
     */
    public String toString();
}
