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
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.taxa.SimpleNCBITaxon;
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
    private Method persist;
    
    /** Creates a new instance of SimpleRichObjectBuilder */
    public HibernateRichObjectBuilder(Object session) {
        super();
        try {
            Class hibernateSession = Class.forName("org.hibernate.Session");
            if (!hibernateSession.isInstance(session))
                throw new IllegalArgumentException("Parameter must be a Hibernate session object");
            this.session = session;
            this.createQuery = hibernateSession.getMethod("createQuery", new Class[]{String.class});
            this.persist = hibernateSession.getMethod("persist", new Class[]{Object.class});
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
            queryText = "from Namespace as ns where ns.name = ?";
        } else if (o instanceof SimpleComparableOntology) {
            queryText = "from Ontology as o where o.name = ?";
        } else if (o instanceof SimpleNCBITaxon) {
            queryText = "from Taxon as o where o.NCBITaxID = ?";
        } else if (o instanceof SimpleCrossRef) {
            queryText = "from CrossRef as cr where cr.dbname = ? and cr.accession = ?";
        } else if (o instanceof SimpleDocRef) {
            queryText = "from DocRef as cr where cr.authors = ? and cr.location = ?";
        } else throw new IllegalArgumentException("Don't know how to handle objects of type "+clazz);
        try {
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            for (int i = 0; i < params.length; i++) {
                query = this.setParameter.invoke(query, new Object[]{new Integer(i), params[i]});
            }
            List results = (List)this.list.invoke(query, null);
            if (results.size()>0) return results.get(0);
            else {
                this.persist.invoke(this.session, new Object[]{o});
                return o;
            }
        } catch (Exception e) {
            StringBuffer paramsstuff = new StringBuffer();
            paramsstuff.append(clazz);
            paramsstuff.append("(");
            for (int i = 0; i < params.length; i++) {
                paramsstuff.append(params[i].toString());
                if (i<(params.length-1)) paramsstuff.append(",");
            }
            paramsstuff.append(")");
            throw new RuntimeException("Error while trying to call new "+paramsstuff,e);
        }
    }
    
}
