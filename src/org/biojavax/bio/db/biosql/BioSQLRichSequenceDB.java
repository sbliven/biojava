/*
 * BioSQLRichSequenceDB.java
 *
 * Created on January 24, 2006, 9:41 AM
 */

package org.biojavax.bio.db.biosql;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.db.IllegalIDException;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.db.AbstractRichSequenceDB;
import org.biojavax.bio.db.HashRichSequenceDB;
import org.biojavax.bio.db.RichSequenceDB;
import org.biojavax.bio.db.RichSequenceDBLite;
import org.biojavax.bio.seq.RichSequence;

/**
 *
 * @author Richard Holland
 */
public class BioSQLRichSequenceDB extends AbstractRichSequenceDB {
    
    private Object session;
    private String name;
    
    private Class query;
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
    
    public void _addRichSequence(RichSequence seq) throws IllegalIDException, BioException, ChangeVetoException {
        try {
            // Get the results
            this.saveOrUpdate.invoke(this.session, new Object[]{"Sequence",seq});
        } catch (Exception e) {
            // Throw the exception with our nice message
            throw new RuntimeException("Error while trying to save RichSequence with id: "+seq.getName(),e);
        }
    }
    
}