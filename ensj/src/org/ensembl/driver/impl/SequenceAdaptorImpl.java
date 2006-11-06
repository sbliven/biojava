/*
  Copyright (C) 2003 EBI, GRL

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.impl.SequenceImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.SequenceAdaptor;
import org.ensembl.util.SequenceUtil;
/**
 * SequenceAdaptorImpl.java
 *
 *
 * Created: Wed Nov 14 10:06:32 2001
 *
 * @author <a href="mailto: "Craig Melsopp</a>
 */

public class SequenceAdaptorImpl
  extends BaseAdaptor
  implements SequenceAdaptor {


  public SequenceAdaptorImpl(CoreDriverImpl driver) {
    super(driver);
  }

  public String getType() {
    return TYPE;
  }


  /**
   * @return Sequence if available, otherwise null.
   */
  public Sequence fetch(long internalID) throws AdaptorException {

    Sequence sequence = null;

    String sql = "SELECT sequence FROM dna WHERE dna_id=? ";

    Connection conn = null;
    try {

      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setLong(1, internalID);
      ResultSet rs = executeQuery(ps, sql);

      if (rs.next()) {
        String sequenceStr = rs.getString(1);
        sequence = new SequenceImpl(sequenceStr);
        sequence.setInternalID(internalID);
      }
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to retrieve sequence " + "where internalID=" + internalID,
        e);
    } finally {
      close(conn);
    }

    return sequence;
  }

  /**
   * Gets sequence for location. Invalid locations result in Ns being
   * returned.
   *
   * @return sequence corresponding to location.
   */
  /*public Sequence fetch(CloneFragmentLocation location)
  	throws AdaptorException {
  
  	logger.fine("Getting sequence for CFLoc : " + location);
  
  	StringBuffer buf = new StringBuffer();
  	Connection conn = null;
  	String sql =
  		" SELECT "
  			+ " SUBSTRING(d.sequence, ?, ? ) "
  			+ " FROM "
  			+ " dna d, "
  			+ " contig c "
  			+ " WHERE "
  			+ " c.contig_id = ? "
  			+ " AND "
  			+ " c.dna_id=d.dna_id";
  
  	try {
  		conn = getConnection();
  		PreparedStatement ps = conn.prepareStatement(sql);
  
  		for (CloneFragmentLocation element = location;
  			element != null;
  			element = element.nextCFL()) {
  			if (element.isGap()) {
  
  				final int gapLen = element.getGap();
  
  				for (int g = 0; g < gapLen; ++g)
  					buf.append('N');
  			} else {
  				retrieveSequenceForElement(ps, sql, buf, element);
  			}
  
  		}
  	} catch (SQLException e) {
  		throw new AdaptorException(
  			"Failed to retrieve sequence for " + location,
  			e);
  	} finally {
  		close(conn);
  	}
  
  	String seqStr = buf.toString();
  	Sequence sequence = factory.createSequence();
  	sequence.setString(seqStr);
  	sequence.setLocation(location);
  
  	logger.fine("Sequence for\n" + location + "\n" + sequence.getString());
  
  	return sequence;
  }
  */

  /**
   * Retrieves location from a single clone fragment corresponding to the
   * head of location list element.
   */
  /*	private void retrieveSequenceForElement(
  		PreparedStatement ps,
  		String sql,
  		StringBuffer buf,
  		CloneFragmentLocation element)
  		throws SQLException, AdaptorException {
  
  		final int len = element.getNodeLength();
  
  		ps.setInt(1, element.getStart());
  		ps.setInt(2, len);
  		ps.setLong(3, element.getCloneFragmentInternalID());
  		ResultSet rs = executeQuery(ps, sql);
  		if (!rs.next()) {
  			// No dna found in database. This could be because a) the location is
  			// valid but there is no dna available, e.g. for the start of
  			// chromosome22, or it could be a nonsense location e.g. part of
  			// chromosome 30 on a human. We just return a string of _length_ Ns.
  			appendNString(buf, len);
  			return;
  		}
  
  		String sequenceStr = rs.getString(1);
  
  		if (element.getStrand() > -1) {
  			buf.append(sequenceStr);
  		} else {
  			// possible: refactor this to write striaght into string buffer- it's a
  			// bit of a hang over.
  			logger.fine("Reverse base order and complement each");
  			buf.append(SequenceUtil.reverseComplement(sequenceStr));
  
  		}
  
  		if (logger.isLoggable(Level.FINE)) {
  			logger.info(
  				"Sequence for "
  					+ element.get()
  					+ ", "
  					+ element.getNodeLength()
  					+ ", "
  					+ element.getLength()
  					+ sequenceStr);
  		}
  
  	}
  
   */

  /**
   * Gets sequence for location. Invalid locations result in Ns being
   * returned e.g. bases 1-100 on human chromosome 33 (which doesn't exist),
   * result in 100 Ns being returned.
   *
   * @return sequence corresponding to assembly location. 'N's are inserted
   * where gaps exist in the assembly.
   */
  /*
  public Sequence fetch(AssemblyLocation location) throws AdaptorException {
  	logger.fine("Getting sequence for AssemblyLoc : " + location);
  
  	Location tmpLoc = driver.getMySQLLocationConverter().convert(location, driver.getDefaultCoordinateSystem(), true, true);
  
  	Sequence seq = null;
  
  	if (tmpLoc != null) {
  		seq = fetch(tmpLoc);
  	} else {
  		// No dna found in database. This could be because a) the location is
  		// valid but there is no dna available, e.g. for the start of
  		// chromosome22, or it could be a nonsense location e.g. part of
  		// chromosome 30 on a human. We just return a string of _length_ Ns.
  		seq = fetch(new CloneFragmentLocation(true, location.getLength()));
  	}
  
  	// set same location to same value as we started with
  	seq.setLocation(location);
  
  	return seq;
  }
  */

  private String fetchUncompressed(Location location) throws AdaptorException {

    Location cloc = location.copy(); // make a copy because we might change it by setting seqRegionIDs

    // we need location.seqRegionID to be set to fetch sequence
    LocationConverter converter = driver.getLocationConverter();
    for (Location tmp=cloc; tmp!=null;tmp=tmp.next()) 
      if (tmp.getSegRegionID()<1)
        tmp.setSegRegionID(converter.nameToId(
            tmp.getSeqRegionName(),
            tmp.getCoordinateSystem()));
    
    String sql =
      "SELECT SUBSTRING( sequence, ?, ? ) FROM dna WHERE seq_region_id = ?";

    Connection conn = null;
    StringBuffer seq = new StringBuffer();

    try {

      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet rs;

      while (cloc != null) {

        if (cloc.isGap()) {
          for(int i=0; i< cloc.getNodeLength();++i)
            seq.append('N');
        } else {

          ps.setInt(1, cloc.getStart());
          int length = (cloc.getEnd() - cloc.getStart()) + 1;
          ps.setInt(2, length);
          ps.setLong(3, cloc.getSegRegionID());
          rs = ps.executeQuery();
          if (rs.next()) {
            if (cloc.getStrand() != -1) {
              seq.append(rs.getString(1));
            } else {
              seq.append(SequenceUtil.reverseComplement(rs.getString(1)));
            }
          }
        }
        cloc = cloc.next();
      }
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to fetch sequence: " + " " + cloc + " : " + sql,
        e);
    } finally {
      close(conn);
    }

    return seq.toString();
  }

  private String fetchCompressed(Location location) {
    return null;
  }

  public Sequence fetch(Location location) throws AdaptorException {
    
    // flesh out the location so can use it for retrieving sequence
    // Interpret strand=0 as positive strand.
    Location completeLoc = driver.getLocationConverter().fetchComplete(location);
    if (completeLoc.getStrand()==0)
      completeLoc.setStrand(1);
    if (completeLoc==null) return null;
    
    CoordinateSystem seqCS = driver.getCoordinateSystemAdaptor().fetchSequenceLevel();
    Location seqLevelLocation = driver.getLocationConverter().convert(completeLoc, seqCS);
    
    // check in meta table wether to use compressed sequence
    boolean compressed = false;    String seqString;
    if (compressed) {
      seqString = fetchCompressed(seqLevelLocation);
    } else {
      seqString = fetchUncompressed(seqLevelLocation);
    }
    Sequence sequence = new SequenceImpl(seqString);
    sequence.setLocation(location);

    return sequence;
  }

  /**
   * Stores Sequence in database. 
   */
  public long store(Sequence sequence) throws AdaptorException {
    String sql = "INSERT INTO dna " + " ( sequence ) " + " VALUES ( ? ) ";

    Connection conn = null;
    try {

      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, sequence.getString());

      sequence.setInternalID(executeAutoInsert(ps, sql));

    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to store sequence: " + " " + sequence + " : " + sql,
        e);
    } finally {
      close(conn);
    }

    return sequence.getInternalID();
  }

  public void delete(long internalID) throws AdaptorException {
    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);

      delete(conn, internalID);

      conn.commit();
    } catch (Exception e) {
      rollback(conn);
      throw new AdaptorException(
        "Failed to delete sequence with internalID= " + internalID,
        e);
    } finally {
      close(conn);
    }
  }

  public void delete(Sequence sequence) throws AdaptorException {
    delete(sequence.getInternalID());
    sequence.setInternalID(0);
  }

  void delete(Connection conn, long internalID) throws AdaptorException {

    if (internalID < 1)
      throw new AdaptorException(
        "internalID is invalid, should be >0 but is " + internalID);

    executeUpdate(conn, "delete from dna where dna_id=" + internalID);
  }

} // SequenceAdaptorImpl
