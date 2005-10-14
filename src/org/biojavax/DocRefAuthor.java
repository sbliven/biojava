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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.utils.Changeable;


/**
 * Represents an author of a documentary reference.
 * @author Richard Holland
 * @see DocRef
 */
public interface DocRefAuthor extends Comparable,Changeable {
    
    /**
     * Returns a textual description of the authors name. This field is
     * immutable so should be set using the constructor of the implementing class.
     * @return Value of property name.
     */
    public String getName();
    
    /**
     * Returns the extended version of the authors name.
     * Form: "name (consortium) (ed.)" where sections in brackets are optional.
     * @return Value of property name with additions.
     */
    public String getExtendedName();
    
    /**
     * Is this author actually an editor?
     * @return true if they are, false if not.
     */
    public boolean isEditor();
    
    /**
     * Is this author actually a consortium?
     * @return true if they are, false if not.
     */
    public boolean isConsortium();
    
    /**
     * Useful tools for working with authors.
     */
    public static class Tools {
        
        // cannot instantiate
        private Tools() {}
        
        /**
         * Takes a list of authors and returns a set of DocRefAuthor objects.
         * @param authors a comma-separated list of authors
         * @return set of DocRefAuthor objects.
         */
        public static Set parseAuthorString(String authors) {
            if (authors==null) throw new IllegalArgumentException("Authors string cannot be null");
            String[] parts = authors.split(",");
            Set authSet = new HashSet();
            for (int i = 0; i < parts.length; i++) authSet.add(new SimpleDocRefAuthor(parts[i]));
            return authSet;
        }
        
        /**
         * Takes a set of authors and creates a comma-separated string.
         * @param a set of authors
         * @return a comma-separated string
         */
        public static String generateAuthorString(Set authors) {
            StringBuffer sb = new StringBuffer();
            for (Iterator i = authors.iterator(); i.hasNext(); ) {
                DocRefAuthor a = (DocRefAuthor)i.next();
                sb.append(a.getExtendedName());
                if (i.hasNext()) sb.append(", ");
            }
            return sb.toString();
        }
        
    }
    
}
