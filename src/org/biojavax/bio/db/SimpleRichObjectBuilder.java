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

package org.biojavax.bio.db;

import java.lang.reflect.Constructor;

/**
 * Creates objects and returns them.
 * @author Richard Holland
 */
public class SimpleRichObjectBuilder implements RichObjectBuilder {
    
    /** Creates a new instance of SimpleRichObjectBuilder */
    public SimpleRichObjectBuilder() {}
    
    /**
     * {@inheritDoc}
     * Instantiates and returns objects, that's all there is to it.
     */
    public Object buildObject(Class clazz, Object[] params) {
        try {
            // Load the class
            Class[] types = new Class[params.length];
            // Find its constructor with given params
            for (int i = 0; i < params.length; i++) types[i] = params[i].getClass();
            Constructor c = clazz.getConstructor(types);
            // Instantiate it with the parameters
            return c.newInstance(params);
        } catch (Exception e) {
            StringBuffer paramsstuff = new StringBuffer();
            paramsstuff.append(clazz);
            paramsstuff.append("(");
            for (int i = 0; i < params.length; i++) {
                paramsstuff.append(params[i].toString());
                if (i<(params.length-1)) paramsstuff.append(",");
            }
            paramsstuff.append(")");
            throw new IllegalArgumentException("Could not find constructor for "+paramsstuff);
        }
    }
    
}
