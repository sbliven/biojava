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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ensembl.datamodel.AssemblyException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AssemblyExceptionAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.util.mapper.Coordinate;
import org.ensembl.util.mapper.Mapper;

/**
 * Provides access to AssemblyExceptions and useful methods for dealing with
 * them.
 * 
 * 
 * TODO create a separate mapper for each seqRegion2seqRegion pair. TODO update
 * dereference to use appropriate mapper. TODO implement rereference. TODO add
 * tests to LocationConverterTest: test HAP and PAR in both directions.
 * 
 * @author arne, craig
 * @see org.ensembl.driver.impl.LocationConverterImpl
 */
public class AssemblyExceptionAdaptorImpl extends BaseFeatureAdaptorImpl
    implements AssemblyExceptionAdaptor {

  private final static String REFERENCE = "REFERENCE";

  private final static String COMPONENT = "COMPONENT";

  private AssemblyException[] exceptionCache;

  private Mapper mapper;

  public AssemblyExceptionAdaptorImpl(CoreDriverImpl driver)
      throws AdaptorException {
    super(driver, "AssemblyExceptions");
  }

  protected String[] columns() {
    String columns[] = { "ae.seq_region_id", "ae.seq_region_start",
        "ae.seq_region_end", "ae.exc_seq_region_id", "ae.exc_seq_region_start",
        "ae.exc_seq_region_end", "ae.ori", "ae.exc_type",
        "ae.assembly_exception_id" };
    return columns;
  }

  public String getType() {
    return TYPE;
  }

  protected String[][] tables() {
    String[][] tables = { { "assembly_exception", "ae" } };
    return tables;
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {
    LocationConverter lc = driver.getLocationConverter();
    AssemblyException ae = null;
    try {

      if (rs.next()) {
        Location loc = lc.idToLocation(rs.getLong(1), rs.getInt(2), rs
            .getInt(3), 1);
        Location linkLoc = lc.idToLocation(rs.getLong(4), rs.getInt(5), rs
            .getInt(6), rs.getInt(7));
        ae = new AssemblyException(rs.getLong(9), loc, linkLoc, rs.getString(8));
        ae.setDriver(getDriver());

      }
    } catch (SQLException e) {
      throw new AdaptorException("rethrow ", e);
    }
    return ae;
  }

  /**
   * @see org.ensembl.driver.AssemblyExceptionAdaptor#fetch()
   */
  public List fetch() throws AdaptorException {
    lazyLoad();
    return Arrays.asList(exceptionCache);
  }

  /**
   * Retrieves AssemblyExceptions with a location overlaps the parameter.
   * 
   * @see org.ensembl.driver.AssemblyExceptionAdaptor#fetch(org.ensembl.datamodel.Location)
   */
  public List fetch(Location loc) throws AdaptorException {

    lazyLoad();

    Location completeLoc = driver.getLocationConverter().fetchComplete(loc);

    ArrayList result = new ArrayList();
    for (int i = 0; i < exceptionCache.length; i++) {
      AssemblyException ae = exceptionCache[i];
      if (ae.getLocation().overlaps(completeLoc))
        result.add(ae);
    }

    return result;
  }

  public List fetchLinked(Location loc) throws AdaptorException {

    lazyLoad();

    // read result from cache
    ArrayList result = new ArrayList();
    for (int i = 0; i < exceptionCache.length; i++) {
      AssemblyException ae = exceptionCache[i];
      if (ae.getTarget().getSeqRegionName().equals(loc.getSeqRegionName())
          && ae.getTarget().getCoordinateSystem().equals(
              loc.getCoordinateSystem())) {
        result.add(ae);
      }
    }

    return result;
  }

  /**
   * @see org.ensembl.driver.AssemblyExceptionAdaptor#fetch(long)
   */
  public AssemblyException fetch(long internalID) throws AdaptorException {
    lazyLoad();

    for (int i = 0; i < exceptionCache.length; i++)
      if (exceptionCache[i].getInternalID() == internalID)
        return exceptionCache[i];

    return null;
  }

  /**
   * @throws AdaptorException
   */
  private final void lazyLoad() throws AdaptorException {
    if (exceptionCache == null) {
      List buf = genericFetch("", null);
      exceptionCache = (AssemblyException[]) buf
          .toArray(new AssemblyException[buf.size()]);
    }

    if (mapper == null) {
      mapper = new Mapper(REFERENCE, COMPONENT);

      for (int i = 0; i < exceptionCache.length; i++) {

        Location location = exceptionCache[i].getLocation();
        Location target = exceptionCache[i].getTarget();

        if (exceptionCache[i].getType().equals("PAR")) {

          mapper.addMapCoordinates(target.getSeqRegionName(),
              target.getStart(), target.getEnd(), target.getStrand(), location
                  .getSeqRegionName(), location.getStart(), location.getEnd());
          
        }

        else if (exceptionCache[i].getType().equals("HAP")) {

          // assembly exception defines 3 reference components:
          // 1 - a part on the target sequence before the HAP
          // 2 - a part on the HAP
          // 3 - a part on the target sequence after the HAP

          int locationLen = driver.getLocationConverter().getLengthByLocation(
              location);
          int targetLen = driver.getLocationConverter().getLengthByLocation(
              target);

          // add the bit before the HAP
          mapper.addMapCoordinates(target.getSeqRegionName(), 1, target
              .getStart() - 1, exceptionCache[i].getTarget().getStrand(),
              location.getSeqRegionName(), 1, location.getStart() - 1);

          // add the middle bit of sequence that is on the HAP
          mapper.addMapCoordinates(location.getSeqRegionName(), location
              .getStart(), location.getEnd(), location.getStrand(), location
              .getSeqRegionName(), location.getStart(), location.getEnd());

          // now the bit after the HAP, need to find the end coordinates
          mapper.addMapCoordinates(target.getSeqRegionName(),
              target.getEnd() + 1, targetLen, target.getStrand(), location
                  .getSeqRegionName(), location.getEnd() + 1, locationLen);

          
          
        }

        else {
          throw new AdaptorException(
              "Unknown AssemblyException type. Know HAP and PAR currently");
        }
      }
    }
  }

  /**
   * @see org.ensembl.driver.AssemblyExceptionAdaptor#dereference(org.ensembl.datamodel.Location)
   */
  public Location dereference(Location loc) throws AdaptorException {
    
    Location r = map(loc, COMPONENT);
    
    for(Location node=r; node!=null;node=node.next())
      if (node.getSeqRegionName()==null) {
        node.setSeqRegionName(loc.getSeqRegionName());
        node.setGap(false);
      }
    
    return r.mergeAdjacentNodes();
  }

  /**
   * @param loc
   * @param completeLoc
   * @return
   * @throws AdaptorException
   */
  private Location map(Location loc, String direction) throws AdaptorException {

    // TODO we need a separate mapper for each seqRegion2seqRegion

    lazyLoad();
    if (exceptionCache.length == 0)
      return loc;

    Location completeLoc = driver.getLocationConverter().fetchComplete(loc);

    Location result = null;
    for (Location node = completeLoc; node != null; node = node.next()) {

      Coordinate[] coords = mapper.mapCoordinate(node.getSeqRegionName(), node
          .getStart(), node.getEnd(), node.getStrand(), direction);

      for (int i = 0; i < coords.length; i++) {

        Location tmp = new Location(node.getCoordinateSystem(), coords[i].id,
            coords[i].start, coords[i].end, coords[i].strand, coords[i].isGap());

        if (result == null)
          result = tmp;
        else
          result.append(tmp);
      }
    }

    return result;
  }

  /**
   * @see org.ensembl.driver.AssemblyExceptionAdaptor#hasReferences(org.ensembl.datamodel.Location)
   */
  public boolean hasReferences(Location loc) throws AdaptorException {
    lazyLoad();
    // TODO Auto-generated method stub
    // mapper.
    return false;
  }

  /**
   * @see org.ensembl.driver.AssemblyExceptionAdaptor#rereference(Location,
   *      org.ensembl.datamodel.SequenceRegion)
   */
  public Location rereference(Location loc, SequenceRegion targetSeqRegion)
      throws AdaptorException {
    
    Location r = map(loc, REFERENCE);
    
    for(Location node=r; node!=null;node=node.next())
      if (node.getSeqRegionName()==null) {
        node.setSeqRegionName(targetSeqRegion.getName());
        node.setGap(false);
      }
    
    return r.mergeAdjacentNodes();
  }

}
