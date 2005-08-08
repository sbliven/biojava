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
import java.lang.reflect.Method;
import java.util.List;
import org.biojavax.SimpleNamespace;
import org.biojavax.ontology.SimpleComparableOntology;

/**
 *
 * @author Richard Holland
 */
public class HibernateRichObjectBuilder extends SimpleRichObjectBuilder {
    
    private Object session;
    private Class query;
    private Method createQuery;
    private Method setParameter;
    private Method list;
    
    /** Creates a new instance of SimpleRichObjectBuilder */
    public HibernateRichObjectBuilder(Object session) {
        super();
        try {
            Class hibernateSession = Class.forName("org.hibernate.Session");
            if (!hibernateSession.isInstance(session))
                throw new IllegalArgumentException("Parameter must be a Hibernate session object");
            this.session = session;
            this.createQuery = hibernateSession.getMethod("createQuery", new Class[]{String.class});
            Class hibernateQuery = Class.forName("org.hibernate.Query");
            this.setParameter = hibernateQuery.getMethod("setParameter", new Class[]{int.class,Object.class});
            this.list = hibernateQuery.getMethod("list", new Class[]{});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object buildObject(Class clazz, Object[] params) {
        Object o = super.buildObject(clazz, params);
        String queryText;
        if (o instanceof SimpleNamespace) {
            queryText = "from SimpleNamespace as ns where ns.name = ?";
        } else if (o instanceof SimpleComparableOntology) {
            queryText = "from SimpleComparableOntology as o where o.name = ?";
        } else throw new IllegalArgumentException("Don't know how to handle objects of type "+clazz);
        try {
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            for (int i = 0; i < params.length; i++) {
                query = this.setParameter.invoke(query, new Object[]{new Integer(i), params[i]});
            }
            List results = (List)this.list.invoke(query, new Object[]{});
            if (results.size()>0) return results.get(0);
            else return o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
