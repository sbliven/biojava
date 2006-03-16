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

package org.biojavax.bio.db.biosql;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.bio.seq.db.IllegalIDException;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.db.AbstractRichSequenceDB;
import org.biojavax.bio.db.HashRichSequenceDB;
import org.biojavax.bio.db.RichSequenceDB;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.SimpleRichSequence;


/**
 *
 * @author Richard Holland
 */
public class BioSQLRichSequenceDB extends AbstractRichSequenceDB {
    
    private Object session;
    private String name;
    
    private Method createCriteria;
    private Method addCriteria;
    private Method listCriteria;
    private Method createAlias;
    private Method createQuery;
    private Method setParameter;
    private Method list;
    private Method delete;
    private Method saveOrUpdate;
    
    /** Creates a new instance of BioSQLRichSequenceDB */
    public BioSQLRichSequenceDB(Object session) {
        this(null,session);
    }
    
    /** Creates a new instance of BioSQLRichSequenceDB */
    public BioSQLRichSequenceDB(String name, Object session) {
        this.name = name;
        this.session = session;
        try {
            // Lazy load the Session class from Hibernate.
            Class hibernateSession = Class.forName("org.hibernate.Session");
            // Test to see if our parameter is really a Session
            if (!hibernateSession.isInstance(session))
                throw new IllegalArgumentException("Session parameter must be a org.hibernate.Session object");
            this.session = session;
            // Lookup the createQuery method
            this.createQuery = hibernateSession.getMethod("createQuery", new Class[]{String.class});
            this.delete = hibernateSession.getMethod("delete", new Class[]{String.class,Object.class});
            this.saveOrUpdate = hibernateSession.getMethod("saveOrUpdate", new Class[]{String.class,Object.class});
            // Lazy load the Query class from Hibernate.
            Class hibernateQuery = Class.forName("org.hibernate.Query");
            // Lookup the setParameter and uniqueQuery methods
            this.setParameter = hibernateQuery.getMethod("setParameter", new Class[]{int.class,Object.class});
            this.list = hibernateQuery.getMethod("list", new Class[]{});
            // Lazy load the Criteria class.
            Class criteria = Class.forName("org.hibernate.Criteria");
            // Lookup the critera methods
            this.createCriteria = hibernateSession.getMethod("createCriteria", new Class[]{Class.class});
            this.addCriteria = criteria.getMethod("add", new Class[]{Class.forName("org.hibernate.Criteria")});
            this.listCriteria = criteria.getMethod("list", new Class[]{Class.forName("org.hibernate.Criteria")});
            this.createAlias = criteria.getMethod("createAlias", new Class[]{String.class,String.class});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public Object getHibernateSession() {
        return this.session;
    }
    
    public FeatureHolder processFeatureFilter(FeatureFilter ff) {
        BioSQLFeatureFilter bff = BioSQLFeatureFilter.Tools.convert(ff);
        SimpleFeatureHolder results = new SimpleFeatureHolder();
        // Apply the filter to the db.
        try {
            Object criteria = this.createCriteria.invoke(this.session, new Object[]{RichFeature.class});
            this.addCriteria.invoke(criteria, new Object[]{bff.asCriterion()});
            Map aliases = bff.criterionAliasMap();
            for (Iterator i = aliases.keySet().iterator(); i.hasNext(); ) {
                String property = (String)i.next();
                String alias = (String)aliases.get(property);
                this.createAlias.invoke(criteria,new Object[]{property,alias});
            }
            List cats = (List)this.listCriteria.invoke(criteria, null);
            for (Iterator i = cats.iterator(); i.hasNext(); ) results.addFeature((Feature)i.next());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (ChangeVetoException cve) {
            throw new BioError("Assertion failed: couldn't modify newly created SimpleFeatureHolder",cve);
        }
        return results;
    }
    
    public FeatureHolder filter(FeatureFilter ff) {
        FeatureHolder fh = this.processFeatureFilter(ff);
        // Post-process only if original filter was not a BioSQLFeatureFilter.
        if (!(ff instanceof BioSQLFeatureFilter)) {
            // Iterate through returned features and remove any that are not accepted.
            SimpleFeatureHolder sfh = new SimpleFeatureHolder();
            for (Iterator i = fh.features(); i.hasNext(); ) {
                Feature f = (Feature)i.next();
                try {
                    if (ff.accept(f)) sfh.addFeature(f);
                } catch (ChangeVetoException cve) {
                    throw new BioError("Assertion failed: couldn't modify newly created SimpleFeatureHolder",cve);
                }
            }
            fh = sfh;
        }
        return fh;
    }
    
    public Set ids() {
        try {
            // Build the query object
            String queryText = "select distinct name from Sequence";
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            // Get the results
            List result = (List)this.list.invoke(query, null);
            // Return the found object, if found - null if not.
            return new HashSet(result);
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to load all names",e);
        }
    }
    
    public RichSequence fullyLoadRichSequence(RichSequence id) throws IllegalIDException, BioException {
        if (id instanceof SimpleRichSequence) return id;
        try {
            // Build the query object
            String queryText = "from Sequence as s where s = ?";
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            // Set the parameters
            query = this.setParameter.invoke(query, new Object[]{new Integer(0), id});
            // Get the results
            List result = (List)this.list.invoke(query, null);
            // If the result doesn't just have a single entry, throw an exception
            if (result.size()==0) throw new IllegalIDException("Id not found: "+id);
            else if (result.size()>1) throw new IllegalIDException("Multiple records found with that id - use getRichSequences: "+id);
            // Return the found object, if found - null if not.
            return (RichSequence)result.get(0);
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to load by id: "+id,e);
        }
    }
    
    public RichSequence getRichSequence(String id) throws IllegalIDException, BioException {
        try {
            // Build the query object
            String queryText = "from Sequence where name = ?";
            Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
            // Set the parameters
            query = this.setParameter.invoke(query, new Object[]{new Integer(0), id});
            // Get the results
            List result = (List)this.list.invoke(query, null);
            // If the result doesn't just have a single entry, throw an exception
            if (result.size()==0) throw new IllegalIDException("Id not found: "+id);
            else if (result.size()>1) throw new IllegalIDException("Multiple records found with that id - use getRichSequences: "+id);
            // Return the found object, if found - null if not.
            return (RichSequence)result.get(0);
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to load by id: "+id,e);
        }
    }
    
    public RichSequenceDB getRichSequences(Set ids) throws BioException, IllegalIDException {
        return this.getRichSequences(ids,null);
    }
    
    public RichSequenceDB getRichSequences(Set ids, RichSequenceDB db) throws BioException, IllegalIDException {
        if (db==null) db = new HashRichSequenceDB();
        try {
            for (Iterator i = ids.iterator(); i.hasNext(); ) {
                String id = (String)i.next();
                // Build the query object
                String queryText = "from Sequence where name = ?";
                Object query = this.createQuery.invoke(this.session, new Object[]{queryText});
                // Set the parameters
                query = this.setParameter.invoke(query, new Object[]{new Integer(0), id});
                // Get the results
                List result = (List)this.list.invoke(query, null);
                // If the result doesn't just have a single entry, throw an exception
                if (result.size()==0) throw new IllegalIDException("Id not found: "+id);
                // Add the results to the results db.
                for (Iterator j = result.iterator(); j.hasNext(); ) db.addRichSequence((RichSequence)j.next());
            }
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to load by ids: "+ids,e);
        }
        return db;
    }
    
    public void removeRichSequence(String id) throws IllegalIDException, BioException, ChangeVetoException {
        if(!hasListeners()) {
            this._removeRichSequence(id);
        } else {
            ChangeSupport changeSupport = getChangeSupport(RichSequenceDB.SEQUENCES);
            synchronized(changeSupport) {
                ChangeEvent ce = new ChangeEvent(
                        this,
                        RichSequenceDB.SEQUENCES,
                        null,
                        id
                        );
                changeSupport.firePreChangeEvent(ce);
                this._removeRichSequence(id);
                changeSupport.firePostChangeEvent(ce);
            }
        }
    }
    
    private void _removeRichSequence(String id) throws IllegalIDException, BioException, ChangeVetoException {
        try {
            // Find the object
            RichSequence be = this.getRichSequence(id);
            // Get the results
            this.delete.invoke(this.session, new Object[]{"Sequence",be});
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to delete by id: "+id,e);
        }
    }
    
    public void addRichSequence(RichSequence seq) throws IllegalIDException, BioException, ChangeVetoException {
        if(!hasListeners()) {
            this._addRichSequence(seq);
        } else {
            ChangeSupport changeSupport = getChangeSupport(RichSequenceDB.SEQUENCES);
            synchronized(changeSupport) {
                ChangeEvent ce = new ChangeEvent(
                        this,
                        RichSequenceDB.SEQUENCES,
                        null,
                        seq
                        );
                changeSupport.firePreChangeEvent(ce);
                this._addRichSequence(seq);
                changeSupport.firePostChangeEvent(ce);
            }
        }
    }
    
    private void _addRichSequence(RichSequence seq) throws IllegalIDException, BioException, ChangeVetoException {
        try {
            // Get the results
            this.saveOrUpdate.invoke(this.session, new Object[]{"Sequence",seq});
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to save RichSequence with id: "+seq.getName(),e);
        }
    }
    
}
