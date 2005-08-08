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
 * SimpleRichObjectBuilder.java
 *
 * Created on August 8, 2005, 9:28 AM
 */

package org.biojavax.bio.db;

import java.lang.reflect.Constructor;

/**
 *
 * @author Richard Holland
 */
public class SimpleRichObjectBuilder implements RichObjectBuilder {
    
    /** Creates a new instance of SimpleRichObjectBuilder */
    public SimpleRichObjectBuilder() {
    }
    
    public Object buildObject(Class clazz, Object[] params) {
        try {
            Class[] types = new Class[params.length];
            for (int i = 0; i < params.length; i++) types[i] = params[i].getClass();
            Constructor c = clazz.getConstructor(types);
            return c.newInstance(params);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not find constructor for "+clazz+"("+params+")",e);
        }
    }
    
}
