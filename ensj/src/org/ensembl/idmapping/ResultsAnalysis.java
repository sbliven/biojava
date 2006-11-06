/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.idmapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ensembl.datamodel.Gene;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.EnsemblDriver;
import org.ensembl.idmapping.StableIDData.GeneTypeData;
import org.ensembl.util.JDBCUtil;
import org.ensembl.util.LongSet;

import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenLongObjectHashMap;

/**
 * Generates mapping statistics and click lists for genes specified in an IDMapping config file.
 * 
 * Can be run from command line, see main(), or implicitly as part of IDMappingApplication.
 * 
 * This class uses an additional configuration parameter idmapping.source.click_list_prefix
 * to construct the click lists. e.g. if your source database was the the human database
 * in Ensembl 37 then
 *  idmapping.source.clicklist_prefix=http://feb2006.archive.ensembl.org/Homo_sapiens/geneview?gene=
 *   
 * @see #main(String[])
 * @see IDMappingApplication
 * 
 */
public class ResultsAnalysis {

  private Config conf;
  private StableIDData src = new StableIDData();
  private StableIDData tgt = new StableIDData();
  private Set deleted = new HashSet();
  private Set deletedSimilar = new HashSet();
  private Set deletedDefinately = new HashSet();
  private Set mapped = new HashSet();
  
  private static NumberFormat percentageFormatter = (NumberFormat) NumberFormat
      .getPercentInstance().clone();
  
  static {
    percentageFormatter.setMinimumFractionDigits(2);
    percentageFormatter.setMaximumFractionDigits(2);
  }

  /**
   * Analyse mapping data in source and target databases.
   *
   * Requires gene_stable_ids and stable_id_event to be correctly populated on both source and target 
   * databases. 
   *
   * @param conf configuration including source and target drivers.
   * @throws AdaptorException
   */
  public ResultsAnalysis(Config conf) throws AdaptorException {
    this.conf = conf;
    
    loadDataFromDatabase(src, conf.getSourceDriver());
    loadDataFromDatabase(tgt, conf.getTargetDriver());
    classifySrc(conf);
    classifySrcByTypes();
  }


  private void classifySrc(Config conf) {
    mapped.addAll(src.getStableIDs());
    mapped.retainAll(tgt.getStableIDs());
    
    deleted.addAll(src.getStableIDs());
    deleted.removeAll(tgt.getStableIDs());

    fetchSimilarDeleted(deletedSimilar, conf.getTargetDriver());

    deletedDefinately.addAll(deleted);
    deletedDefinately.removeAll(deletedSimilar);
  }


  /**
   * Analyse mapping data in data structures created by IDMapping program.
   * 
   * @param sourceGenesByInternalID source genes
   * @param targetGenesByInternalID target genes
   * @param geneMappings sourceGeneID->targetGeneID pairs
   * @param similarities similarities between genes in src and tgt datasets
   */
  public ResultsAnalysis(Config conf, OpenLongObjectHashMap sourceGenesByInternalID, 
      OpenLongObjectHashMap targetGenesByInternalID, 
      List geneMappings, 
      Collection similarities) {
    
    this.conf = conf;
    
    loadSrc(sourceGenesByInternalID);
    loadTgt(sourceGenesByInternalID, targetGenesByInternalID, geneMappings);
    classifySrc(sourceGenesByInternalID, geneMappings, similarities);
    classifySrcByTypes();
  }


  /**
   * Load target gene data.
   * 
   * Assign stableIDs where mapped and unmapped:
   *    a) tgtGene -> tgtID -> srcID -> srcGene -> accessionID
   *    b) tgtGene -> tgtID -> X -> "unmapped"
   *  
   * @param sourceGenesByInternalID
   * @param targetGenesByInternalID
   * @param geneMappings
   */
  private void loadTgt(OpenLongObjectHashMap sourceGenesByInternalID, OpenLongObjectHashMap targetGenesByInternalID, List geneMappings) {
    
    tgt = new StableIDData();
    OpenLongObjectHashMap tgtID2srcGene = new OpenLongObjectHashMap();
    for (int i = 0; i < geneMappings.size(); i++) {
      Entry e = (Entry) geneMappings.get(i);
      tgtID2srcGene.put(e.target, sourceGenesByInternalID.get(e.source));
    }
    
    List genes = targetGenesByInternalID.values().toList();
    for (int i = 0; i < genes.size(); i++) {
      Gene g = (Gene) genes.get(i);
      if (g.getStatus()==null 
          || g.getAnalysis()==null 
          || g.getAnalysis().getLogicalName()==null
          || g.getBioType()==null) {
          System.err.println("WARNING: Missing data for target gene : "+g.getInternalID());
          continue;
      }
      String type = g.getStatus()+"-"+g.getAnalysis().getLogicalName()+"-"+g.getBioType();
      Gene srcGene = (Gene) tgtID2srcGene.get(g.getInternalID());
      String stableID = (srcGene)!=null? srcGene.getAccessionID() : "unmapped";
      tgt.add(type, stableID);
    }
  }


  /**
   * Loads _data_ from database specified by driver.
   * @param data
   * @param driver
   * @throws AdaptorException
   */
  private void loadDataFromDatabase(StableIDData data, EnsemblDriver driver) throws AdaptorException {
    
    Connection conn = null;
    
    try {
    
      conn = driver.getConnection();
      
      String sql = "select concat(status,'-',logic_name,'-',biotype) as name, stable_id " +
      "from gene g ,analysis a, gene_stable_id gsi " +
      "where g.analysis_id=a.analysis_id and gsi.gene_id=g.gene_id";
      ResultSet rs = conn.createStatement().executeQuery(sql);
      while(rs.next()) {
        
        String type = rs.getString(1);
        String stableID = rs.getString(2);
        
        data.add(type, stableID);
        
      }
      rs.close();
    } catch (SQLException e) {
      throw new AdaptorException(e);
    } finally {
      JDBCUtil.close(conn);
    }
    
  }

  
  /**
   * Load source gene data.
   * @param sourceGenesByInternalID
   */
  private void loadSrc(OpenLongObjectHashMap sourceGenesByInternalID) {

    src = new StableIDData();
    List genes = sourceGenesByInternalID.values().toList();
    for (int i = 0; i < genes.size(); i++) {
      Gene g = (Gene) genes.get(i);
      String type = g.getStatus()+"-"+g.getAnalysis().getLogicalName()+"-"+g.getBioType();
      src.add(type, g.getAccessionID());
    }
  }

  /**
   * Classifies genes as mapped, deleted, deletedSimilar or deletedDefinately
   * based on IDMapper data structures.
   * 
   * @param sourceGenesByInternalID source genes.
   * @param geneMappings mappings from source to target genes.
   * @param similarities 
   */
  private void classifySrc(OpenLongObjectHashMap sourceGenesByInternalID, List geneMappings, Collection similarities) {
    
    LongSet mappedID = new LongSet();
    for (int i = 0; i < geneMappings.size(); i++) {
      Entry e = (Entry) geneMappings.get(i);
      mappedID.add(e.source);
      mapped.add(((Gene)sourceGenesByInternalID.get(e.source)).getAccessionID());
    }
    
    // Source genes that are similar to one or more target genes
    Set similarToTarget = new HashSet();
    for (Iterator iter = similarities.iterator(); iter.hasNext();) {
      StableIDEventRow row = (StableIDEventRow) iter.next();
      similarToTarget.add(row.getOldStableID());
    }
    
    ObjectArrayList genes = sourceGenesByInternalID.values();
    for (int i = 0; i < genes.size(); i++) {
      
      Gene gene = (Gene) genes.get(i);
      long id = gene.getInternalID();
      String stableID = gene.getAccessionID();
      
      if (!mappedID.contains(id)) {
        deleted.add(stableID);
        if (similarToTarget.contains(stableID))
          deletedSimilar.add(stableID);
        else
          deletedDefinately.add(stableID);
            
      }
    }
    
    
  }


  /**
   * Runs analysis directly against databases specified in an idmapping configuration
   * file that should be specified on the command line.
   * 
   * @param args
   * @throws AdaptorException
   */
  public static void main(String[] args) throws AdaptorException {
    if (args.length < 1 || !new File(args[0]).exists()) {
      System.out.println("Usage: ResultsAnalysis IDMAPPING.PROPERTIES");
      System.exit(0);
    }
    new ResultsAnalysis(new Config(args[0])).dump();
  }

  
  /**
   * Create statistics about mapped and lost stable ids.
   * 
   * @return string representing results table.
   * @throws AdaptorException
   */
  private String resultTable() throws AdaptorException {

    StringBuffer buf = new StringBuffer();

    buf.append("                               Gene Type                Mapped          Lost(similar)   Lost(definate)\n")
    .append("-------------------------------------------------------------------------------------------------------\n");
    
    // sort rows alphabetically
    ArrayList types = new ArrayList(src.types());
    Collections.sort(types);

    for (Iterator iter = types.iterator(); iter.hasNext();) {
      String type = (String) iter.next();
      StableIDData.GeneTypeData d = src.get(type); 
      resultTableRow(buf, type, d.stableIDs.size(), new int[]{d.mapped.size(), 
        d.deletedSimilar.size(), d.deletedDefinately.size()});
    }
    
    return buf.toString(); 
  }


  private void classifySrcByTypes() {
    
    for (Iterator iter = src.types().iterator(); iter.hasNext();) {
      String type = (String) iter.next();
      StableIDData.GeneTypeData d = src.get(type); 
      for (Iterator stableIDIter = d.stableIDs.iterator(); stableIDIter.hasNext();) {
        String stableID = (String) stableIDIter.next();
        if (mapped.contains(stableID))
          d.mapped.add(stableID);
        else if (deletedSimilar.contains(stableID))
          d.deletedSimilar.add(stableID);
        else if (deletedDefinately.contains(stableID))
          d.deletedDefinately.add(stableID);
        else
          System.err.println("Unclassified source gene " + stableID);
      }
    }
  }


  /**
   * Writes one row of results table to buf.
   * 
   * @param buf buffer to write row to.
   * @param type gene type to use as first column.
   * @param total total to use when calculating percentages.
   * @param values each value to be presented as value and percentage.
   */
  private void resultTableRow(StringBuffer buf, String type, int total, int[] values) {
    for (int i = type.length(); i < 40; i++)
      buf.append(" ");
    buf.append(type).append("\t");
    for (int i = 0; i < values.length; i++) 
      buf.append("\t").append(values[i]).append("\t")
      .append(percentageFormatter.format((float)values[i] / total));
    buf.append("\n");

  }
  



  /**
   * Fetch the genes from the source database that were deleted (not mapped) and
   * are similar to one or more genes in the target database.
   * 
   * @param deletedSimilar
   * @param driver
   */
  private void fetchSimilarDeleted(Set similarDeleted, CoreDriver targetDriver) {
    Connection conn = null;
    
    try {
      
      conn = targetDriver.getConnection();
      
      // Assume that the latest mapping session is the one with the highest
      // internal_id because the mapping_session rows aren't ranked
      // we assume
      long mappingSessionID = -1;
      String sql = "select max(mapping_session_id) from mapping_session where old_db_name!='ALL' ";
      ResultSet rs = conn.createStatement().executeQuery(sql);
      if (rs.next()) 
        mappingSessionID = rs.getLong(1);
      else
        throw new RuntimeException("Couldn't find mapping_session_id for latest mapping session in target database.");
        

      // find genes from the src database that were deleted during the latest
      // mapping session which are similar to >=1 genes in the new database.
      sql = "select e1.old_stable_id from stable_id_event e1, stable_id_event e2"
        +" where e1.mapping_session_id="+mappingSessionID
        +" and e1.new_stable_id is NULL"
        +" and e1.type='gene'"
        +" and e1.old_stable_id= e2.old_stable_id"
        +" and e2.new_stable_id is not NULL"
        +" and e2.mapping_session_id="+mappingSessionID
        +" group by e1.old_stable_id";

      
      rs = conn.createStatement().executeQuery(sql);
      while (rs.next()) 
        similarDeleted.add(rs.getString(1));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (AdaptorException e) {
      throw new RuntimeException(e);
    } finally {
      JDBCUtil.close(conn);
    }
  }





  /**
   * Dump statistics and click list files.
   * 
   * @param conf
   *          configuration containing source and target drivers.
   * @return stats description.
   * @throws AdaptorException
   */
  public void dump() throws AdaptorException {

    writeResultsFile();
    writeClickLists();
  }


  private void writeResultsFile() throws AdaptorException {
    
    String fileName = conf.rootDir + File.separator + "gene_detailed_mapping_statistics.txt";

    String stats = resultTable();
    
    try {
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(
          fileName));
      writer.write(stats);
      writer.close();
      System.out.println("\nStatistics written to " + fileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }


  private void writeClickLists() {
    
    String urlPrefix = System.getProperty("idmapping.source.click_list_prefix", "idmapping.source.click_list_prefix_UNSET");
    
    for (Iterator iter = src.types().iterator(); iter.hasNext();) {
    
      String type =  (String) iter.next();
      
      writeClickList(urlPrefix, 
          src.get(type).deletedDefinately,
          "gene_lost_definately_"+type+".html");
      
      writeClickList(urlPrefix, 
          src.get(type).deletedSimilar,
          "gene_lost_similar_"+type+".html");
    }
  }


  private void writeClickList(String urlPrefix, Set stableIDs, String filename) {
    try {
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(
          conf.rootDir + File.separator + filename));
      
      writer.write("<h1>");
      writer.write(filename);
      writer.write("</h1>");
      
      // e.g. http://apr2006.archive.ensembl.org/Homo_sapiens/geneview?gene=ENSG00000194666
      for (Iterator iterator = stableIDs.iterator(); iterator.hasNext();) {
        String stableID = (String) iterator.next();
        writer.write("<a href='");
        writer.write(urlPrefix);
        writer.write(stableID);
        writer.write("'>");
        writer.write(stableID);
        writer.write("</a><br>\n");
      }
      
      writer.close();
      System.out.println("Lost genes written to "+filename);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
