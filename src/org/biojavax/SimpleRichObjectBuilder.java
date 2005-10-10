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

package org.biojavax;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;


/**
 * Creates objects and returns them, and stores them in an internal
 * map of singletons for reference. Takes up a lot of memory!
 * @author Richard Holland
 */
public class SimpleRichObjectBuilder implements RichObjectBuilder {
    
    private static Map objects = new HashMap();    
    
    /**
     * {@inheritDoc}
     * Instantiates and returns objects, that's all there is to it.
     */
    public Object buildObject(Class clazz, Object[] params) {
        // put the class into the hashmap if not there already
        if (!objects.containsKey(clazz)) objects.put(clazz,new HashMap());
        Map contents = (Map)objects.get(clazz);
        // put the constructed object into the hashmap if not there already
        if (contents.containsKey(params)) return contents.get(params); 
        // otherwise build it.
        try {
            // Load the class
            Class[] types = new Class[params.length];
            // Find its constructor with given params
            for (int i = 0; i < params.length; i++) types[i] = params[i].getClass();
            Constructor c = clazz.getConstructor(types);
            // Instantiate it with the parameters
            Object o = c.newInstance(params);
            // store it for later in the singleton map
            contents.put(params, o);
            // return it
            return o;
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
