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
import java.lang.reflect.Method;
import java.util.List;
import org.biojavax.SimpleCrossRef;
import org.biojavax.SimpleDocRef;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.taxa.SimpleNCBITaxon;
import org.biojavax.ontology.SimpleComparableOntology;

/**
 * Takes requests for RichObjects and sees if it can load them from a Hibernate
 * database. If it can, it returns the loaded objects. Else, it creates them
 * and persists them, then returns them.
 * @author Richard Holland
 */
public class HibernateRichObjectBuilder extends SimpleRichObjectBuilder {
    
    private Object session;
    private Class query;
    private Method createQuery;
    private Method setParameter;
    private Method list;
    private Method persist;
    
    /** 
     * Creates a new instance of SimpleRichObjectBuilder. The session parameter
     * is a Hibernate Session object and must not be null. It is this session
     * that database objects will be retrieved from/persisted to.
     * @see org.hibernate.Session
     */
    public HibernateRichObjectBuilder(Object session) {
        super(); // call the normal rich object builder first
        try {
            // Lazy load the Session class from Hibernate.
            Class hibernateSession = Class.forName("org.hibernate.Session");
            // Test to see if our parameter is really a Session
            if (!hibernateSession.isInstance(session))
                throw new IllegalArgumentException("Parameter must be a org.hibernate.Session object");
            this.session = session;
            // Lookup the createQuery and persist methods
            this.createQuery = hibernateSession.getMethod("createQuery", new Class[]{String.class});
            this.persist = hibernateSession.getMethod("persist", new Class[]{Object.class});
            // Lazy load the Query class from Hibernate.
            Class hibernateQuery = Class.forName("org.hibernate.Query");
            // Lookup the setParameter and list methods
            this.setParameter = hibernateQuery.getMethod("setParameter", new Class[]{int.class,Object.class});
            this.list = hibernateQuery.getMethod("list", new Class[]{});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * Attempts to look up the details of the object in the database. If it
     * finds them it loads the object and returns it. Else, it persists the
     * wrapper object it made to do the search with and returns that.
     */
    public Object buildObject(Class clazz, Object[] params) {
        // Create a wrapper object to do the search with
        Object o = super.buildObject(clazz, params);
        // Create the Hibernate query to look it up with
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
        // Run the query.
        try {
            // Build the query object
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            // Set the parameters
            for (int i = 0; i < params.length; i++) {
                query = this.setParameter.invoke(query, new Object[]{new Integer(i), params[i]});
            }
            // Get the results
            List results = (List)this.list.invoke(query, null);
            // Return the found object, if found
            if (results.size()>0) return results.get(0);
            // Persist and return the wrapper object otherwise
            else {
                this.persist.invoke(this.session, new Object[]{o});
                return o;
            }
        } catch (Exception e) {
            // Write a useful message explaining what we were trying to do. It will
            // be in the form "class(param,param...)".
            StringBuffer paramsstuff = new StringBuffer();
            paramsstuff.append(clazz);
            paramsstuff.append("(");
            for (int i = 0; i < params.length; i++) {
                paramsstuff.append(params[i].toString());
                if (i<(params.length-1)) paramsstuff.append(",");
            }
            paramsstuff.append(")");
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to call new "+paramsstuff,e);
        }
    }
    
}
