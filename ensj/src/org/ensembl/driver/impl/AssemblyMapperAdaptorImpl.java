/*
 * Created on 09-Oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ensembl.datamodel.AssemblyMapper;
import org.ensembl.datamodel.ChainedAssemblyMapper;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.SimpleAssemblyMapper;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AssemblyMapperAdaptor;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.util.mapper.Coordinate;
import org.ensembl.util.mapper.Mapper;
import org.ensembl.util.mapper.Range;
import org.ensembl.util.mapper.RangeRegistry;

/**
 * @author arne
 *  
 */
public class AssemblyMapperAdaptorImpl extends BaseAdaptor implements
    AssemblyMapperAdaptor {

  private HashMap seqRegionCache;

  private HashMap mapperCache;

  private static final int CHUNKFACTOR = 20;

  private static final int MAXPAIRCOUNT = 3000;

  private class MidRange {

    final String regionName;

    final long regionId;

    final int start;

    final int end;

    public MidRange(final String regionName, final long regionId,
        final int start, final int end) {
      this.regionName = regionName;
      this.regionId = regionId;
      this.start = start;
      this.end = end;
    }

  }

  private final String asmsql = "SELECT " + "  asm.cmp_start, "
      + "  asm.cmp_end, " + "  asm.cmp_seq_region_id, " + "  sr.name, "
      + "  asm.ori, " + "  asm.asm_start, " + "  asm.asm_end, "
      + "  sr.length " + "FROM " + "  assembly asm, seq_region sr " + "WHERE "
      + "  asm.asm_seq_region_id = ? AND" + "  ? <= asm.asm_end AND "
      + "  ? >= asm.asm_start AND "
      + "  asm.cmp_seq_region_id = sr.seq_region_id AND "
      + "  sr.coord_system_id = ?";

  private final String cmpsql = "SELECT " + "  asm.asm_start, "
      + "  asm.asm_end, " + "  asm.asm_seq_region_id, " + "  sr.name, "
      + "  asm.ori, " + "  asm.cmp_start, " + "  asm.cmp_end, "
      + "  sr.length " + "FROM " + "  assembly asm, seq_region sr " + "WHERE "
      + "  asm.cmp_seq_region_id = ? AND" + "  ? <= asm.cmp_end AND "
      + "  ? >= asm.cmp_start AND "
      + "  asm.asm_seq_region_id = sr.seq_region_id AND "
      + "  sr.coord_system_id = ?";

  public static void main(String[] args) {
  }

  public AssemblyMapperAdaptorImpl(CoreDriverImpl driver) {
    super(driver);
    mapperCache = new HashMap();
    seqRegionCache = new HashMap();
  }

  public String getType() {
    return TYPE;
  }

  public AssemblyMapper fetchByCoordSystems(CoordinateSystem cs1,
      CoordinateSystem cs2) throws AdaptorException {

    AssemblyMapper resultMapper;
    resultMapper = (AssemblyMapper) mapperCache.get(cs1.toString() + " "
        + cs2.toString());
    if (resultMapper != null) {
      return resultMapper;
    }
    CoordinateSystemAdaptor csa = driver.getCoordinateSystemAdaptor();
    CoordinateSystem[] path = csa.getMappingPath(cs1, cs2);
    if (path == null) {
      throw new AdaptorException("Can't map between coordinate systems " + cs1
          + " and " + cs2);
    }
    if (path.length == 2) {
      // simple mapping
      resultMapper = new SimpleAssemblyMapper(driver, path[0], path[1]);
    } else if (path.length == 3) {
      // chained mapping
      resultMapper = new ChainedAssemblyMapper(driver, path[0], path[1],
          path[2]);
    } else {
      // no mapping
      throw new AdaptorException("Cant map between" + cs1 + " and " + cs2);
    }

    mapperCache.put((cs1.toString() + " " + cs2.toString()), resultMapper);
    mapperCache.put((cs2.toString() + " " + cs1.toString()), resultMapper);
    return resultMapper;
  }

  public void registerAssembled(SimpleAssemblyMapper asmMapper,
      String seqRegionName, int start, int end) throws AdaptorException {

    //	keep the Mapper to a reasonable size
    if (asmMapper.getSize() > MAXPAIRCOUNT)
      asmMapper.flush();

    int startChunk = start >> CHUNKFACTOR;
    int endChunk = end >> CHUNKFACTOR;
    LinkedList regions = new LinkedList();

    Integer beginChunkRegion, endChunkRegion;
    beginChunkRegion = null;

    //	find regions of continuous unregistered chunks
    for (int i = startChunk; i <= endChunk; i++) {
      if (asmMapper.haveRegisteredAssembled(seqRegionName, i)) {
        if (beginChunkRegion != null) {
          // this is the end of an unregistered region.
          endChunkRegion = new Integer((i << CHUNKFACTOR) - 1);
          regions.add(beginChunkRegion);
          regions.add(endChunkRegion);
          beginChunkRegion = null;
        }
      } else {
        if (beginChunkRegion == null) {
          beginChunkRegion = new Integer((i << CHUNKFACTOR) + 1);
        }
        asmMapper.registerAssembled(seqRegionName, i);
      }
    }
    // the last part may have been an unregistered region too
    if (beginChunkRegion != null) {
      endChunkRegion = new Integer(((endChunk + 1) << CHUNKFACTOR) - 1);
      regions.add(beginChunkRegion);
      regions.add(endChunkRegion);
    }

    // nothing new needs registering
    if (regions.size() == 0) {
      return;
    }

    long seqRegionId;
    try {
      seqRegionId = driver.getLocationConverter().nameToId(seqRegionName,
          asmMapper.getAssembledCoordinateSystem());
    } catch (Exception e) {
      throw new AdaptorException(e);
    }

    String sql = "SELECT " + "		asm.cmp_start," + "		asm.cmp_end,"
        + "		asm.cmp_seq_region_id," + "	  sr.name," + "   sr.length,"
        + "		asm.ori," + "   asm.asm_start," + "		asm.asm_end" + "	FROM "
        + "		 assembly asm, seq_region sr " + "	WHERE "
        + "		 asm.asm_seq_region_id = ? AND " + "		 ? <= asm.asm_end AND "
        + "		 ? >= asm.asm_start AND "
        + "		 asm.cmp_seq_region_id = sr.seq_region_id AND "
        + "    sr.coord_system_id = ? ";

    //		Retrieve the description of how the assembled region is made from
    //		component regions for each of the continuous blocks of unregistered,
    //		chunked regions

    Iterator i = regions.iterator();
    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      while (i.hasNext()) {
        int beginRegion = ((Integer) i.next()).intValue();
        int endRegion = ((Integer) i.next()).intValue();

        ps.setLong(1, seqRegionId);
        ps.setInt(2, beginRegion);
        ps.setInt(3, endRegion);
        ps.setLong(4, asmMapper.getComponentCoordinateSystem().getInternalID());
        ResultSet result = ps.executeQuery();
        while (result.next()) {
          String componentRegionName = result.getString(4);
          if (asmMapper.haveRegisteredComponent(componentRegionName)) {
            continue;
          }
          asmMapper.registerComponent(componentRegionName);
          asmMapper.mapper.addMapCoordinates(componentRegionName, result
              .getInt(1), result.getInt(2), result.getInt(6), seqRegionName,
              result.getInt(7), result.getInt(8));
          driver.getLocationConverter().cacheSeqRegion(componentRegionName,
              asmMapper.getComponentCoordinateSystem(), result.getLong(3),
              result.getInt(5));
        }
      }
    } catch (Exception e) {
      throw new AdaptorException(e);
    } finally {
      close(conn);
    }
  }

  public void registerComponent(SimpleAssemblyMapper asmMapper,
      String componentSeqRegionName) throws AdaptorException {

    long componentCoordSystemId = asmMapper.getComponentCoordinateSystem()
        .getInternalID();
    long asmCoordSystemId = asmMapper.getAssembledCoordinateSystem()
        .getInternalID();

    if (asmMapper.haveRegisteredComponent(componentSeqRegionName)) {
      return;
    }

    long seqRegionId = driver.getLocationConverter().nameToId(
        componentSeqRegionName, asmMapper.getComponentCoordinateSystem());
    String sql = "	SELECT " + "    asm.asm_start, " + "    asm.asm_end, "
        + "    asm.asm_seq_region_id, " + "    sr.name, " + "    sr.length "
        + "	FROM " + "    assembly asm, seq_region sr " + " WHERE "
        + "    asm.cmp_seq_region_id = ? AND "
        + "    asm.asm_seq_region_id = sr.seq_region_id AND "
        + "    sr.coord_system_id = ? ";

    Connection conn = null;

    try {
      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);

      ps.setLong(1, seqRegionId);
      ps.setLong(2, asmCoordSystemId);

      ResultSet result = ps.executeQuery();
      if (!result.next()) {
        asmMapper.registerComponent(componentSeqRegionName);
        return;
      }
      String asmSeqRegion = result.getString(4);
      int asmStart = result.getInt(1);
      int asmEnd = result.getInt(2);
      long asmRegionId = result.getLong(3);
      int regionLength = result.getInt(5);

      if (result.next()) {
        throw (new Exception("Cant handle 2 assembly areas for same component"));
      }
      driver.getLocationConverter().cacheSeqRegion(asmSeqRegion,
          asmMapper.getAssembledCoordinateSystem(), asmRegionId, regionLength);
      registerAssembled(asmMapper, asmSeqRegion, asmStart, asmEnd);
    } catch (Exception e) {
      throw new AdaptorException(e);
    } finally {
      close(conn);
    }
  }

  public void registerChained(ChainedAssemblyMapper cam, String startTag,
      String seqRegionName, List ranges) throws AdaptorException {

    final CoordinateSystem midCS = cam.getCsMiddle();
    
    // one step mapping is where we use a chained mapper to handle 1:1 mappings
    // represented by assembly.mapping entries in the meta table that use the
    // '#' separator.
    final boolean oneStepMappingMode = midCS == null;
    
    final Mapper combinedMapper = cam.getFirstLastMapper();

    Mapper startMiddleMapper, endMiddleMapper;
    CoordinateSystem startCS, endCS;
    RangeRegistry startReg, endReg;
    String endTag;

    if (startTag.equals("first")) {

      startMiddleMapper = cam.getFirstMiddleMapper();
      startCS = cam.getCsFirst();
      startReg = cam.getFirstReg();
      endMiddleMapper = cam.getLastMiddleMapper();
      endCS = cam.getCsLast();
      endReg = cam.getLastReg();
      endTag = "last";

    } else if (startTag.equals("last")) {

      startMiddleMapper = cam.getLastMiddleMapper();
      startCS = cam.getCsLast();
      startReg = cam.getLastReg();
      endMiddleMapper = cam.getFirstMiddleMapper();
      endCS = cam.getCsFirst();
      endReg = cam.getFirstReg();
      endTag = "first";

    } else {

      throw new AdaptorException("Wrong coord system tag"); 

    }

    Connection conn = null;
    try {
      conn = getConnection();

      LinkedList midRanges = new LinkedList();
      LinkedList startRanges = new LinkedList();

      if (oneStepMappingMode) {

        loadStartMiddleMapper(combinedMapper, oneStepMappingMode, startCS,
            midCS, endCS, ranges, startRanges, midRanges, seqRegionName,
            startTag, startReg, conn);

        for (Iterator iter = midRanges.iterator(); iter.hasNext();) {
          MidRange r = (MidRange) iter.next();
          endReg.checkAndRegister(r.regionName, r.start, r.end);
        }
        
      } else {

        loadStartMiddleMapper(startMiddleMapper, oneStepMappingMode, startCS,
            midCS, endCS, ranges, startRanges, midRanges, seqRegionName,
            startTag, startReg, conn);

        loadEndMiddleMapper(endMiddleMapper, midCS, endCS, midRanges, endReg,
            conn);

        updateCombinedMapper(combinedMapper, startMiddleMapper,
            endMiddleMapper, startRanges, startTag, seqRegionName);
      }


    } catch (SQLException e) {
      throw new AdaptorException("rethrow", e);
    } finally {
      close(conn);
    }
  }

  /**
   * Do stepwise mapping using both of the loaded mappers to load the final
   * combined start <->end mapper
   */
  private void updateCombinedMapper(Mapper combinedMapper,
      Mapper startMiddleMapper, Mapper endMiddleMapper, LinkedList startRanges,
      String startTag, String seqRegionName) throws IllegalArgumentException {

    for (Iterator srIter = startRanges.iterator(); srIter.hasNext();) {

      Range range = (Range) srIter.next();
      int sum = 0;
      Coordinate[] initialCoords = startMiddleMapper.mapCoordinate(
          seqRegionName, range.start, range.end, 1, startTag);

      for (int coordIdx = 0; coordIdx < initialCoords.length; coordIdx++) {
        Coordinate coord = initialCoords[coordIdx];
        if (coord.isGap()) {
          sum += coord.length();
          continue;
        }

        Coordinate[] finalCoords = endMiddleMapper.mapCoordinate(coord.id,
            coord.start, coord.end, coord.strand, "middle");
        for (int finalCoordIdx = 0; finalCoordIdx < finalCoords.length; finalCoordIdx++) {
          Coordinate fcoord = finalCoords[finalCoordIdx];
          if (!fcoord.isGap()) {
            int totalStart = sum + range.start;
            int totalEnd = fcoord.length() + totalStart - 1;
            if (startTag.equals("first")) {
              combinedMapper.addMapCoordinates(seqRegionName, totalStart,
                  totalEnd, fcoord.strand, fcoord.id, fcoord.start, fcoord.end);
            } else {
              combinedMapper.addMapCoordinates(fcoord.id, fcoord.start,
                  fcoord.end, fcoord.strand, seqRegionName, totalStart,
                  totalEnd);
            }
          }
          sum += fcoord.length();
        }
      }
    }
  }

  /**
   * Load the mid <->end mapper using the mid cs ranges.
   */
  private void loadEndMiddleMapper(Mapper endMiddleMapper,
      CoordinateSystem midCS, CoordinateSystem endCS, LinkedList midRanges,
      RangeRegistry endReg, Connection conn) throws SQLException,
      AdaptorException, IllegalArgumentException {

    // path[0] is assembled, path[1] component
    CoordinateSystem[] path = driver.getCoordinateSystemAdaptor()
        .getMappingPath(midCS, endCS);
    // path[1]==null means we are using a chained mapper in one step mode
    // to do direct mapping.
    if (path.length != 2 && path[1]!=null)
      throw new AdaptorException("should be able to go direct from " + midCS
          + " to " + endCS);
    String sql = (path[0].equals(midCS)) ? asmsql : cmpsql;
    PreparedStatement ps = conn.prepareStatement(sql);

    for (Iterator mrIter = midRanges.iterator(); mrIter.hasNext();) {
      MidRange r = (MidRange) mrIter.next();

      ps.setLong(1, r.regionId);
      ps.setInt(2, r.start);
      ps.setInt(3, r.end);
      ps.setLong(4, endCS.getInternalID());

      for (ResultSet rs = ps.executeQuery(); rs.next();) {

        int endStart = rs.getInt(1);
        int endEnd = rs.getInt(2);
        long endSeqRegionId = rs.getLong(3);
        String endSeqRegionName = rs.getString(4);
        int ori = rs.getInt(5);
        int midStart = rs.getInt(6);
        int midEnd = rs.getInt(7);

        boolean addToCaches = endMiddleMapper.addMapCoordinates(
            endSeqRegionName, endStart, endEnd, ori, r.regionName, midStart,
            midEnd);

        if (addToCaches) {
          driver.getLocationConverter().cacheSeqRegion(endSeqRegionName, endCS,
              endSeqRegionId, rs.getInt(8));
          endReg.checkAndRegister(endSeqRegionName, endStart, endEnd);
        }

      }
    }
  }

  private void loadStartMiddleMapper(Mapper mapper,
      final boolean oneStepMappingMode, CoordinateSystem startCS, final CoordinateSystem midCS,
      CoordinateSystem endCS, List ranges, LinkedList startRanges,
      LinkedList midRanges, String seqRegionName,
      String startTag, RangeRegistry startReg,
      Connection conn) throws SQLException, AdaptorException,
      IllegalArgumentException {
    
    long midCSId;
    CoordinateSystem[] path;
    if (oneStepMappingMode) {
      
      path = driver.getCoordinateSystemAdaptor().getMappingPath(startCS, endCS);
      path = new CoordinateSystem[] { path[0], path[1] };
      if (path.length != 2)
        throw new AdaptorException("should be able to go direct from " + startCS
            + " to " + endCS);
      
      midCSId = endCS.getInternalID();
      
    } else {
      
      path = driver.getCoordinateSystemAdaptor().getMappingPath(startCS, midCS);
      // path[1]==null means we are using a chained mapper in one step mode
      // to do direct mapping.
      if (path.length != 2 && path[1]!=null)
        throw new AdaptorException("should be able to go direct from " + startCS
            + " to " + midCS);
      
      midCSId = midCS.getInternalID();
      
    }
    
    String sql = (path[0].equals(startCS)) ? asmsql : cmpsql;
    PreparedStatement ps = conn.prepareStatement(sql);

    long seqRegionId = driver.getLocationConverter().nameToId(seqRegionName,
        startCS);

    for (Iterator rIter = ranges.iterator(); rIter.hasNext();) {

      Range range = (Range) rIter.next();

      ps.setLong(1, seqRegionId);
      ps.setInt(2, range.start);
      ps.setInt(3, range.end);
      ps.setLong(4, midCSId);

      for (ResultSet rs = executeQuery(ps, sql); rs.next();) {

        int midStart = rs.getInt(1);
        int midEnd = rs.getInt(2);
        long midRegionId = rs.getLong(3);
        String midRegionName = rs.getString(4);
        int ori = rs.getInt(5);
        int startStart = rs.getInt(6);
        int startEnd = rs.getInt(7);

        boolean addToCaches = false;
        if (oneStepMappingMode && !"first".equals(startTag))
          addToCaches = mapper.addMapCoordinates(midRegionName,
              midStart, midEnd, ori, seqRegionName, startStart, startEnd);
        else
          addToCaches = mapper.addMapCoordinates(seqRegionName,
              startStart, startEnd, ori, midRegionName, midStart, midEnd);

        if (addToCaches) {

          CoordinateSystem cs = (oneStepMappingMode) ? endCS : midCS;
          driver.getLocationConverter().cacheSeqRegion(midRegionName, cs,
              midRegionId, rs.getInt(8));

          midRanges.add(new MidRange(midRegionName, midRegionId, midStart,
              midEnd));

          startRanges.add(new Range(startStart, startEnd));

          // there is a chance that we got more back from the query than what
          // we already registered, we register that, too
          if (startStart < range.start || startEnd > range.end) {
            startReg.checkAndRegister(seqRegionName, startStart, startEnd);
          }

        }
      }
    }
  }
}