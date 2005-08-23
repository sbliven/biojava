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
 * RichObjectFactory.java
 *
 * Created on August 1, 2005, 9:33 AM
 */

package org.biojavax.bio.db;

import java.util.HashMap;
import java.util.Map;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.PositionResolver;
import org.biojavax.bio.seq.PositionResolver.AverageResolver;
import org.biojavax.ontology.ComparableOntology;
import org.biojavax.ontology.SimpleComparableOntology;

/**
 *
 * @author Richard Holland
 */
public class RichObjectFactory {
    
    private static Map objects = new HashMap();
    
    private static RichObjectBuilder builder = new SimpleRichObjectBuilder();
    
    public static synchronized void setRichObjectBuilder(RichObjectBuilder b) {
        if (b!=builder) objects.clear(); // because they're from a different factory now
        builder = b;
    }
    
    public static synchronized Object getObject(Class clazz, Object[] params) {
        if (!objects.containsKey(clazz)) objects.put(clazz,new HashMap());
        Map contents = (Map)objects.get(clazz);
        if (!contents.containsKey(params)) contents.put(params, builder.buildObject(clazz, params));
        return contents.get(params);
    }
    
    private static String defaultOntologyName = "biojavax";
    private static String defaultNamespaceName = "lcl";
    private static PositionResolver defaultPositionResolver = new AverageResolver();
    
    public static void setDefaultNamespaceName(String name) { defaultNamespaceName = name; }
    public static void setDefaultNamespace(Namespace ns) { defaultNamespaceName = ns.getName(); }
    public static void setDefaultOntologyName(String name) { defaultOntologyName = name; }
    public static void setDefaultOntology(ComparableOntology onto) { defaultOntologyName = onto.getName(); }
    public static void setDefaultPositionResolver(PositionResolver pr) { defaultPositionResolver = pr; }
    
    public static Namespace getDefaultNamespace() {
        return (Namespace)getObject(SimpleNamespace.class, new Object[]{defaultNamespaceName});
    }
    public static ComparableOntology getDefaultOntology() {
        return (ComparableOntology)getObject(SimpleComparableOntology.class, new Object[]{defaultOntologyName});
    }
    public static PositionResolver getDefaultPositionResolver() { return defaultPositionResolver; }
    
    private static String genbankNamespaceName = "gb";
    private static String emblNamespaceName = "embl";
    public static Namespace getGenbankNamespace(){
        return (Namespace)getObject(SimpleNamespace.class, new Object[]{genbankNamespaceName});
    }
    public static Namespace getEMBLNamespace(){
        return (Namespace)getObject(SimpleNamespace.class, new Object[]{emblNamespaceName});
    }
    
        
}
