/* -*- c-basic-offset: 4; indent-tabs-mode: nil -*- */
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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.taxa.CircularReferenceException;
import org.biojava.bio.taxa.EbiFormat;
import org.biojava.bio.taxa.Taxon;
import org.biojava.bio.taxa.TaxonFactory;
import org.biojava.bio.taxa.WeakTaxonFactory;
import org.biojava.utils.ChangeVetoException;

/**
 * Methods for dealing with retrieving Taxa stored in a BioSQL database.
 *
 * @author Len Trigg
 */
public class TaxonSQL {

    /** 
     * Attempts to get a Taxon object corresponding to the specified
     * NCBI taxon ID.
     *
     * @param conn the connection to the database
     * @param ncbi_taxon_id the NCBI taxon ID.
     * @return the corresponding Taxon (which may have already been
     * present in memory after an earlier retrieval), or null if the
     * Taxon could not be found in the database.
     */
    public static Taxon getTaxon(Connection conn, int ncbi_taxon_id) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            int taxon_id = 0;
            statement = conn.prepareStatement("select taxon_id " 
                                              + "from taxon " 
                                              + "where ncbi_taxon_id = ? ");
            statement.setInt(1, ncbi_taxon_id);
            rs = statement.executeQuery();
            if (rs.next()) {
                taxon_id = rs.getInt(1);
                if (rs.wasNull()) {
                    taxon_id = 0;
                }
            }
            rs.close();
            statement.close();

            return (taxon_id != 0) ? getDBTaxon(conn, taxon_id) : null;
	} catch (ChangeVetoException ex) {
	    throw new BioRuntimeException("Couldn't manipulate in-memory taxonomy", ex);
	} catch (SQLException ex) {
	    throw new BioRuntimeException("Error fetching taxonomy annotations", ex);
        } finally {
            attemptClose(rs);
            attemptClose(statement);
        }
    }


    /** 
     * Attempts to get a Taxon object corresponding to the specified
     * taxon_id (i.e. the database's internal id for the taxon).
     *
     * @param conn the connection to the database
     * @param taxon_id the database-specific id for the Taxon.
     * @return the corresponding Taxon (which may have already been
     * present in memory after an earlier retrieval).
     */
    static Taxon getDBTaxon(Connection conn, int taxon_id) throws SQLException, ChangeVetoException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            // Constants for our wee id array
            final int NCBI_ID = 1;
            final int TAXON_ID = 0;
            
            // First, get the taxon ids up to the root.
            statement = conn.prepareStatement("select ncbi_taxon_id, parent_taxon_id " 
                                              + "from taxon " 
                                              + "where taxon_id = ? ");
            
            ArrayList path = new ArrayList();
            while (taxon_id != 0) {
                statement.setInt(1, taxon_id);
                rs = statement.executeQuery();
                if (rs.next()) {
                    path.add(new int [] {taxon_id, rs.getInt(1)});
                    taxon_id = rs.getInt(2);
                    if (rs.wasNull()) {
                        taxon_id = 0;
                    }
                } else {
                    throw new BioRuntimeException("Error fetching taxonomy structure. No taxon with taxon_id=" + taxon_id);
                }
                rs.close();
            }
            statement.close();
            
            // Traverse from the root down as far has has been created previously...
            TaxonFactory factory = WeakTaxonFactory.GLOBAL;
            Taxon taxon = factory.getRoot();
            int pos = path.size() - 1;
            int []ids = (int[]) path.get(pos--);
            Map names = getTaxonNames(conn, ids[TAXON_ID]);
            taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_NCBI_TAXON, "" + ids[NCBI_ID]);
            taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_TAXON_NAMES, names);
            for (; pos >= 0; pos--) {
                // Who's the next id down the path?
                ids = (int[]) path.get(pos);
                String nextID = "" + ids[NCBI_ID];
                // Now look among the children for the next child.
                Set children = taxon.getChildren();
                for (Iterator it = children.iterator(); it.hasNext(); ) {
                    Taxon child = (Taxon) it.next();
                    Annotation anno = child.getAnnotation();
                    if (anno.containsProperty(EbiFormat.PROPERTY_NCBI_TAXON)) {
                        String childID = (String) anno.getProperty(EbiFormat.PROPERTY_NCBI_TAXON);
                        if (childID.equals(nextID)) {
                            taxon = child;
                            continue;
                        }
                    } else {
                        throw new BioRuntimeException("Taxon has not been annotated with NCBI taxon ids.");
                    }
                }
                // No child with desired ncbi_id has been found.
                break;
            }
            
            // Now create taxa from here on down.
            try {
                for (; pos >= 0; pos--) {
                    // Now look for the next child.
                    ids = (int[]) path.get(pos);
                    String nextID = "" + ids[NCBI_ID];
                    names = getTaxonNames(conn, ids[TAXON_ID]);
                    String sciName = (String) names.get("scientific name");
                    if (sciName == null) {
                        throw new BioRuntimeException("No scientific name for taxon_id=" + ids[TAXON_ID]);
                    }
                    String commonName = (String) names.get("common name");
                    taxon = factory.addChild(taxon, factory.createTaxon(sciName, commonName));
                    taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_NCBI_TAXON, nextID);
                    taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_TAXON_NAMES, names);
                }           
            } catch (CircularReferenceException ex) {
                throw new BioRuntimeException("Circular references in taxon table. taxon_id=" + ids[TAXON_ID]);
            }
            return taxon;
        } finally {
            attemptClose(rs);
            attemptClose(statement);
        }
    }
    

    /**
     * Look up all the names associated with a taxon_id.
     *
     * @param conn the current <code>Connection</code>.
     * @param taxon_id the NCBI taxon id for the taxon of interest.
     * @return a <code>Map</code> from name_class (e.g.: "scientific
     * name") to name.
     */
    private static Map getTaxonNames(Connection conn, int taxon_id) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement("select name_class, name " 
                                              + "from taxon_name " 
                                              + "where taxon_id = ? ");
            statement.setInt(1, taxon_id);
            rs = statement.executeQuery();

            Map names = new HashMap();
            while (rs.next()) {
                String name_class = rs.getString(1);
                String name = rs.getString(2);
                //System.err.println("Got " + name_class + "=" + name + " for taxon_id=" + taxon_id); 
                names.put(name_class, name);
            }

            return names;
	} catch (SQLException ex) {
	    throw new BioRuntimeException("Error fetching taxonomy annotations", ex);
        } finally {
            attemptClose(rs);
            attemptClose(statement);
        }
    }


    /**
     * Adds a <code>Taxon</code> (along with its parents) to the
     * database. If it is already present in the database, no action
     * is taken.  Returns the id by which the database refers to the
     * specified <code>Taxon</code> object.
     *
     * @param taxon a <code>Taxon</code>. The <code>Taxon</code> must
     * be annotated with the NCBI taxon id
     * (<code>key=EbiFormat.PROPERTY_ORGANISM</code>).
     * @return an <code>int</code> that corresponds to the
     * <code>Taxon</code> in the database.
     */
    public static int putTaxon(Connection conn, DBHelper helper, Taxon taxon) throws SQLException {
        // Find the NCBI taxon id annotation
        Annotation anno = taxon.getAnnotation();
        Object t  = anno.getProperty(EbiFormat.PROPERTY_NCBI_TAXON);
        if (t instanceof List) {
            t = (String) ((List) t).get(0);
        }
        int ncbi_taxon_id = Integer.parseInt((String) t);
        PreparedStatement selectTaxon = conn.prepareStatement(
                                                              "select taxon_id " 
                                                              + "from taxon " 
                                                              + "where ncbi_taxon_id = ? "
                                                              );
        selectTaxon.setInt(1, ncbi_taxon_id);
        ResultSet trs = selectTaxon.executeQuery();
        int taxon_id;
        if (trs.next()) {
            // entry exists - link to it
            taxon_id = trs.getInt(1);
        } else {
            // Taxon entry does not exist - create it
            Taxon parent = taxon.getParent();
            PreparedStatement createTaxon = null;
            if (parent != null) {
                int parent_taxon_id = putTaxon(conn, helper, parent);
                createTaxon = conn.prepareStatement(
                                                    "insert into taxon " 
                                                    + "(ncbi_taxon_id, parent_taxon_id) " 
                                                    + "values (?, ?)"
                                                    );
                createTaxon.setInt(1, ncbi_taxon_id);
                createTaxon.setInt(2, parent_taxon_id);
            } else {
                createTaxon = conn.prepareStatement(
                                                    "insert into taxon " 
                                                    + "(ncbi_taxon_id) " 
                                                    + "values (?)"
                                                    );
                createTaxon.setInt(1, ncbi_taxon_id);
            }
            createTaxon.executeUpdate();
            createTaxon.close();
            taxon_id = helper.getInsertID(conn, "taxon", "taxon_id");
            putTaxonNames(conn, (Map) taxon.getAnnotation().getProperty(EbiFormat.PROPERTY_TAXON_NAMES), taxon_id);
        }
        trs.close();
        selectTaxon.close();
        return taxon_id;
    }


    private static void putTaxonNames(Connection conn, Map names, int taxon_id) throws SQLException {
        if (names != null) {
            Iterator it = names.keySet().iterator();
            while (it.hasNext()) {
                String nameClass = (String) it.next();
                String name = (String) names.get(nameClass);
                PreparedStatement createTaxon = conn.prepareStatement(
                                                                      "insert into taxon_name " 
                                                                      + "(taxon_id, name, name_class) " 
                                                                      + "values (?, ?, ?)"
                                                                      );
                createTaxon.setInt(1, taxon_id);
                createTaxon.setString(2, name);
                createTaxon.setString(3, nameClass);
                createTaxon.executeUpdate();
                createTaxon.close();
            }
        }
    }


    /** Attempt to close the Statement. Continue on if there is a problem during the close. */
    static void attemptClose(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }


    /** Attempt to close the ResultSet. Continue on if there is a problem during the close. */
    static void attemptClose(ResultSet resultset) {
        if (resultset != null) {
            try {
                resultset.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
