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

import java.util.HashMap;
import java.util.Map;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.PositionResolver;
import org.biojavax.bio.seq.PositionResolver.AverageResolver;
import org.biojavax.ontology.ComparableOntology;
import org.biojavax.ontology.SimpleComparableOntology;

/**
 * Maintains a singleton map of rich objects, and provides some default values
 * for things like default ontology, default namespace, etc.
 * @author Richard Holland
 */
public class RichObjectFactory {
    
    private static Map objects = new HashMap();    
    private static RichObjectBuilder builder = new SimpleRichObjectBuilder();
    
    private static String defaultOntologyName = "biojavax";
    private static String defaultNamespaceName = "lcl";
    private static String genbankNamespaceName = "gb";
    private static String emblNamespaceName = "embl";
    private static PositionResolver defaultPositionResolver = new AverageResolver();
    
    // Constructor is private as this is all static.
    private RichObjectFactory() {}
    
    /**
     * Sets the builder to use when instantiating new Rich objects. The basic,
     * default, one is a SimpleRichObjectBuilder, which just calls the constructor.
     * Another useful one is HibernateRichObjectBuilder, which attempts to load
     * objects from the database. The latter is required if you are working with
     * Hibernate as it will not work without it.
     * @param b the builder to use.
     * @see SimpleRichObjectBuilder
     * @see HibernateRichObjectBuilder
     */
    public static synchronized void setRichObjectBuilder(RichObjectBuilder b) {
        if (b!=builder) objects.clear(); // because they're from a different factory now
        builder = b;
    }
    
    /**
     * Maintains the singleton map, and returns the requested object from that map
     * based on the class name and constructor parameters. If the object does not
     * exist, it delegates to a RichObjectBuilder to construct the object, then 
     * adds it to the map, and returns it.
     * @param clazz the class to build 
     * @param params[] the parameters to pass to the class' constructor
     * @return the instantiated object
     */
    public static synchronized Object getObject(Class clazz, Object[] params) {
        // put the class into the hashmap if not there already
        if (!objects.containsKey(clazz)) objects.put(clazz,new HashMap());
        Map contents = (Map)objects.get(clazz);
        // put the constructed object into the hashmap if not there already
        if (!contents.containsKey(params)) contents.put(params, builder.buildObject(clazz, params));
        return contents.get(params);
    }
    
    /** 
     * Sets the default namespace name to use when loading sequences. Defaults to "lcl".
     * @param name the namespace name to use.
     */
    public static void setDefaultNamespaceName(String name) { defaultNamespaceName = name; }
        
    /** 
     * Sets the default ontology name to use when loading sequences. Defaults to "biojavax".
     * @param name the ontology name to use.
     */
    public static void setDefaultOntologyName(String name) { defaultOntologyName = name; }
        
    /**
     * Sets the default position resolver to use when creating new rich feature locations.
     * Defaults to the AverageResolver
     * @param pr the position resolver to use.
     * @see PositionResolver
     * @see PositionResolver.AverageResolver
     * @see RichLocation
     */
    public static void setDefaultPositionResolver(PositionResolver pr) { defaultPositionResolver = pr; }
    
    /**
     * Returns the default namespace object. Defaults to "lcl".
     * @return the default namespace.
     */
    public static Namespace getDefaultNamespace() {
        return (Namespace)getObject(SimpleNamespace.class, new Object[]{defaultNamespaceName});
    }  
    
    /**
     * Returns the default ontology object. Defaults to "biojavax".
     * @return the default ontology.
     */
    public static ComparableOntology getDefaultOntology() {
        return (ComparableOntology)getObject(SimpleComparableOntology.class, new Object[]{defaultOntologyName});
    }  
    
    /**
     * Returns the default ontology object. Defaults to PositionResolver.AverageResolver
     * @return the default ontology.
     * @see PositionResolver.AverageResolver
     */
    public static PositionResolver getDefaultPositionResolver() { return defaultPositionResolver; }
        
    /**
     * Returns the GenBank namespace object ("gb").
     * @return the GenBank namespace.
     */
    public static Namespace getGenbankNamespace(){
        return (Namespace)getObject(SimpleNamespace.class, new Object[]{genbankNamespaceName});
    }    
    
    /**
     * Returns the EMBL namespace object ("embl").
     * @return the EMBL namespace.
     */
    public static Namespace getEMBLNamespace(){
        return (Namespace)getObject(SimpleNamespace.class, new Object[]{emblNamespaceName});
    }
            
}
