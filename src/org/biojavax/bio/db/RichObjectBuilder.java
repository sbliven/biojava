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
 * RichObjectBuilder.java
 *
 * Created on August 8, 2005, 9:22 AM
 */

package org.biojavax.bio.db;

/**
 *
 * @author Richard Holland
 */
public interface RichObjectBuilder {
    
    public Object buildObject(Class clazz, Object[] params);
    
}
