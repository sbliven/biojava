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


package org.biojava.bio.seq.db.biosql;

import java.sql.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.cache.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.taxa.*;
import org.biojava.bio.ontology.*;

/**
 * Behind-the-scenes adaptor to the features sub-schema of BioSQL.
 *
 * @author Thomas Down
 * @since 1.4
 */

class OntologySQL {
    private BioSQLSequenceDB seqDB;
    private Map ontologiesByID;
    private Map ontologiesByName;
    private Map termsByID;
    private Map IDsByTerm;
    private Map monitors;
    
    private Map blessedExternalAliases;
    private Map blessedExternalTerms;
    
    {
        ontologiesByID = new HashMap();
        ontologiesByName = new HashMap();
        termsByID = new HashMap();
        IDsByTerm = new HashMap();
        monitors = new HashMap();
        
        blessedExternalAliases = new HashMap();
        blessedExternalTerms = new HashMap();
    }

    public Ontology getOntology(String name)
        throws NoSuchElementException
    {
        Ontology ont = (Ontology) ontologiesByName.get(name);
        if (ont == null) {
            throw new NoSuchElementException("Can't find ontology named " + name);
        }
        return ont;
    }
    
    public Ontology createOntology(String name, String description)
        throws AlreadyExistsException, BioException
    {
        if (ontologiesByName.containsKey(name)) {
            throw new AlreadyExistsException("This BioSQL database already contains an ontology of name " + name);
        }
        Ontology ont = new Ontology.Impl(name, description);    
        
        persistOntology(ont);
        
        return ont;
    }
    
    public Ontology addOntology(Ontology ontology) {
        return ontology;
    }
    
    private void loadTerms(Ontology ont, int id)
        throws SQLException, OntologyException, ChangeVetoException
    {
        Connection conn = seqDB.getPool().takeConnection();
        PreparedStatement get_terms = conn.prepareStatement(
                "select ontology_term_id, name, definition " +
                "  from ontology_term " +
                " where ontology_id = ?"
        );
        get_terms.setInt(1, id);
        ResultSet rs = get_terms.executeQuery();
        while (rs.next()) {
            int term_id = rs.getInt(1);
            String name = rs.getString(2);
            String description = rs.getString(3);
            
            Term t = ont.createTerm(name, description);
            Integer tid = new Integer(term_id);
            termsByID.put(tid, t);
            IDsByTerm.put(t, tid);
        }
        rs.close();
        get_terms.close();
        
        seqDB.getPool().putConnection(conn);
        conn = null;
        
        if (ont.getName().equals("core_ontology")) {
            blessExternal(ont, OntoTools.getCoreOntology());
        }
    }
    
    public void blessExternal(Ontology internalOntology, Ontology externalOntology) {
        for (Iterator i = externalOntology.getTerms().iterator(); i.hasNext(); ) {
            Term extTerm = (Term) i.next();
            Term intTerm = internalOntology.getTermByName(extTerm.getName());
            blessedExternalAliases.put(extTerm, intTerm);
            blessedExternalTerms.put(intTerm, extTerm);
        }
    }
    
    private Term localize(Ontology ont, Term t) 
        throws AlreadyExistsException, ChangeVetoException
    {
        if (t.getOntology() == ont) {
            return t;
        } else {
            if (blessedExternalTerms.containsKey(t)) {
                t = (Term) blessedExternalTerms.get(t);
            }
            return ont.importTerm(t);
        }
    }
    
    OntologySQL(BioSQLSequenceDB seqDB) 
        throws SQLException, BioException
    {
        this.seqDB = seqDB;
        
        Connection conn = seqDB.getPool().takeConnection();
	
        try {
            PreparedStatement get_onts = conn.prepareStatement(
               "select ontology_id, name, definition " +
               "  from ontology "
            );
            ResultSet rs = get_onts.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String description = rs.getString(3);
                Ontology ont = new Ontology.Impl(name, description);
                loadTerms(ont, id);
                ontologiesByID.put(new Integer(id), ont);
                ontologiesByName.put(name, ont);
            }
            rs.close();
            get_onts.close();
            
            PreparedStatement get_rels = conn.prepareStatement(
                "select ontology_id, subject_id, object_id, predicate_id " +
                "  from ontology_relationship"
            );
            
            rs = get_rels.executeQuery();
            while (rs.next()) {
                int ontology_id = rs.getInt(1);
                int subject_id = rs.getInt(2);
                int object_id = rs.getInt(3);
                int predicate_id = rs.getInt(4);
                Ontology ont = (Ontology) ontologiesByID.get(new Integer(ontology_id));
                ont.createTriple(
                    localize(ont, (Term) termsByID.get(new Integer(subject_id))),
                    localize(ont, (Term) termsByID.get(new Integer(object_id))),
                    localize(ont, (Term) termsByID.get(new Integer(predicate_id)))
                );
            }
            rs.close();
            get_rels.close();
            
            for (Iterator i = ontologiesByID.values().iterator(); i.hasNext(); ) {
                Ontology ont = (Ontology) i.next();
                OntologyMonitor om = new OntologyMonitor(ont);
                monitors.put(ont, om);
                ont.addChangeListener(om, ChangeType.UNKNOWN);
            }
        } catch (AlreadyExistsException ex) {
            throw new BioException(ex, "Duplicate term name in BioSQL");
        } catch (ChangeVetoException ex) {
            throw new BioError(ex, "Assertion failed: couldn't modify Ontology");
        }
        
        seqDB.getPool().putConnection(conn);
        conn = null;
    }
    
    private class OntologyMonitor implements ChangeListener {
        private Ontology ontology;
        
        OntologyMonitor(Ontology o) {
            this.ontology = o;
        }
        
        public void preChange(ChangeEvent cev) 
            throws ChangeVetoException
        {
            ChangeType type = cev.getType();
            if (type.isMatchingType(Ontology.TERM)) {
                ChangeEvent chain = cev.getChainedEvent();
                if (chain != null) {
                    // looks like it's an actual term which has been added.
                    throw new ChangeVetoException("BioSQL does not handle mutable terms");
                } else {
                    if (cev.getChange() != null && cev.getPrevious() == null) {
                        // Adding
                        if (! (cev.getChange() instanceof Term)) {
                            throw new ChangeVetoException("Can't understand this change");
                        }
                        Term addedTerm = (Term) cev.getChange();
                        if (addedTerm instanceof OntologyTerm) {
                            throw new ChangeVetoException("BioSQL doesn't (currently) represent OntologyTerms");
                        } else if (addedTerm instanceof TripleTerm) {
                            throw new ChangeVetoException("BioSQL doesn't (currently) represent OntologyTerms");
                        } else if (addedTerm instanceof RemoteTerm) {
                            Term gopher = addedTerm;
                            while (gopher instanceof RemoteTerm) {
                                gopher = ((RemoteTerm) gopher).getRemoteTerm();
                                if (!ontologiesByID.values().contains(gopher.getOntology()) && !blessedExternalAliases.containsKey(gopher)) {
                                    throw new ChangeVetoException("BioSQL ontologies can't contain references to external ontologies");
                                }
                            }
                        } else {
                            // We presume it's a term with no special semantics
                        }
                    } else if (cev.getChange() == null && cev.getPrevious() != null) {
                        throw new ChangeVetoException("FIXME: can't remove terms from biosql ontology");
                    } else {
                        throw new ChangeVetoException("Unknown TERM change");
                    }
                }
            } else if (type.isMatchingType(Ontology.TRIPLE)) {
                // Any triple-changes that we can't handle?  I don't think so.
            } else {
                throw new ChangeVetoException("BioSQL does not understand this change");
            }
        }
        
        public void postChange(ChangeEvent cev) {
            ChangeType type = cev.getType();
            if (type.isMatchingType(Ontology.TERM)) {
                if (cev.getChange() != null && cev.getPrevious() == null) {
                    // Adding
                    if (! (cev.getChange() instanceof Term)) {
                        throw new BioError("Assertion failed: added object isn't a term");
                    }
                    Term addedTerm = (Term) cev.getChange();
                    if (addedTerm instanceof RemoteTerm) {
                        // We're not actually persisting these explicitly.  Hope this isn't a problem
                    }
                    persistTerm(addedTerm);
                }
                // Don't support removal yet, but it should have been vetoed.
            } else if (type.isMatchingType(Ontology.TRIPLE)) {
                if (cev.getChange() != null && cev.getPrevious() == null) {
                    // Adding
                    if (! (cev.getChange() instanceof Triple)) {
                        throw new BioError("Assertion failed: added object isn't a triple");
                    }
                    persistTriple(ontology, (Triple) cev.getChange());
                }
            } 
        }
    }
    
    private void persistTerm(Term term) {
        Connection conn = null;
        try {
            conn = seqDB.getPool().takeConnection();
            conn.setAutoCommit(false);
            
            persistTerm(conn, term);
            
            conn.commit();
            seqDB.getPool().putConnection(conn);
        } catch (SQLException ex) {
            boolean rolledback = false;
            if (conn != null) {
                try {
                    conn.rollback();
                    rolledback = true;
                } catch (SQLException ex2) {}
            }
            throw new BioRuntimeException(ex, "Error removing from BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
        }
    }
    
    private void persistTerm(Connection conn, Term term)
        throws SQLException
    {
        PreparedStatement import_term = conn.prepareStatement(
            "insert into ontology_term " +
            "       (name, definition, ontology_id) " +
            "values (?, ?, ?)"
        );
        import_term.setString(1, term.getName());
        import_term.setString(2, term.getDescription());
        import_term.setInt(3, ontologyID(term.getOntology()));
        import_term.executeUpdate();
        int id = seqDB.getDBHelper().getInsertID(conn, "ontology_term", "ontology_term_id");
        Integer tid = new Integer(id);
        termsByID.put(tid, term);
        IDsByTerm.put(term, tid);
    }
    
    private void persistTriple(Ontology ont, Triple triple) {
        Connection conn = null;
        try {
            conn = seqDB.getPool().takeConnection();
            conn.setAutoCommit(false);
            
            persistTriple(conn, ont, triple);
            
            conn.commit();
            seqDB.getPool().putConnection(conn);
        } catch (SQLException ex) {
            boolean rolledback = false;
            if (conn != null) {
                try {
                    conn.rollback();
                    rolledback = true;
                } catch (SQLException ex2) {}
            }
            throw new BioRuntimeException(ex, "Error removing from BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
        }
    }
    
    private void persistTriple(Connection conn, Ontology ont, Triple triple)
        throws SQLException
    {
        PreparedStatement import_trip = conn.prepareStatement(
            "insert into ontology_relationship " +
            "       (subject_id, predicate_id, object_id, ontology_id) " +
            "values (?, ?, ?, ?)"
        );
        import_trip.setInt(1, termID(triple.getSubject()));
        import_trip.setInt(2, termID(triple.getRelation()));
        import_trip.setInt(3, termID(triple.getObject()));
        import_trip.setInt(4, ontologyID(ont));
        import_trip.executeUpdate();
    }
    
    private void persistOntology(Ontology onto)
    {
        Connection conn = null;
        try {
            conn = seqDB.getPool().takeConnection();
            conn.setAutoCommit(false);
            
            persistOntology(conn, onto);
            
            conn.commit();
            seqDB.getPool().putConnection(conn);
        } catch (SQLException ex) {
            boolean rolledback = false;
            if (conn != null) {
                try {
                    conn.rollback();
                    rolledback = true;
                } catch (SQLException ex2) {}
            }
            throw new BioRuntimeException(ex, "Error removing from BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
        }
    }
    
    private void persistOntology(Connection conn, Ontology ont)
        throws SQLException
    {
        PreparedStatement insert_ontology = conn.prepareStatement(
                "insert into ontology " +
                "       (name, definition) " +
                "values (?, ?)"
        );
        insert_ontology.setString(1, ont.getName());
        insert_ontology.setString(2, ont.getDescription());
        insert_ontology.executeUpdate();
        int id = seqDB.getDBHelper().getInsertID(conn, "ontology", "ontology_id");
        Integer tid = new Integer(id);
        ontologiesByID.put(tid, ont);
        ontologiesByName.put(ont.getName(), ont);
        OntologyMonitor om = new OntologyMonitor(ont);
        monitors.put(ont, om);
        ont.addChangeListener(om, ChangeType.UNKNOWN);
    }
    
    private int ontologyID(Ontology ont) {
        for (Iterator i = ontologiesByID.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry me = (Map.Entry) i.next();
            if (me.getValue().equals(ont)) {
                return ((Integer) me.getKey()).intValue();
            }
        }
        throw new BioError("Couldn't find ontology " + ont.getName());
    }
    
    private int termID(Term t) {
        while (t instanceof RemoteTerm) {
            t = ((RemoteTerm) t).getRemoteTerm();
        }
        if (blessedExternalAliases.containsKey(t)) {
            t = (Term) blessedExternalAliases.get(t);
        }
        return ((Integer) IDsByTerm.get(t)).intValue();
    }
    
}
