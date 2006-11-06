/*
  Copyright (C) 2002 EBI, GRL

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

package org.ensembl.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cern.colt.map.OpenLongObjectHashMap;

/**
 * General utilities that don't fit elsewhere.
 */
public class Util {

    public static void addToMapList(Map map, Object key, Object o) {

	if (!map.containsKey(key)) {
	    map.put(key, new ArrayList());
	}
	((ArrayList) map.get(key)).add(o);

    }

    public static void addToMapList(OpenLongObjectHashMap map, long key, Object o) {

      ArrayList l = (ArrayList) map.get(key);
      if (l==null) {
        l = new ArrayList();
        map.put(key, l);
      }
    	l.add(o);

    }    
    
    // -------------------------------------------------------------------------
    /**
     * Dump a table's data to a tab-delimited file. Null values are written as \N
     * so that they don't get read as strings by LOAD DATA INFILE.
     */
    public static void dumpTableToFile(Connection con, String table,
				       String fileName) {

	try {

	    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(
										    fileName));

	    // according to mysql handbook this is the way to make the
	    // result set "streaming" and not reading the full table
	    // into memory
	    Statement stmt = con.createStatement(
						 java.sql.ResultSet.TYPE_FORWARD_ONLY,
						 java.sql.ResultSet.CONCUR_READ_ONLY);
	    stmt.setFetchSize(Integer.MIN_VALUE);

	    ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int cols = rsmd.getColumnCount();
	    while (rs.next()) {
		for (int i = 1; i <= cols; i++) {
		    String s = rs.getString(i);
		    if (s != null) {
			writer.write(s);
		    } else {
			writer.write("\\N");
		    }
		    if (i < cols) {
			writer.write("\t");
		    } else {
			writer.write("\n");
		    }
		}

	    }
	    writer.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    // -------------------------------------------------------------------------
    /**
     * Create a link to the ensembl.org page for a particular gene. Species type
     * is currently guessed from gene prefix.
     * 
     * @param gene
     *          The gene name.
     * @return A link to the geneview page on ensembl.org, or null if the species
     *         can't be deduced.
     */
    public static String makeEnsemblGeneLink(String gene) {

	Map geneStableIDToSpecies = new HashMap();

	geneStableIDToSpecies.put("ENSMUSG[0-9]+", "Mus_musculus");
	geneStableIDToSpecies.put("ENSANGG[0-9]+", "Anopheles_gambiae");
	geneStableIDToSpecies.put("ENSCBRG[0-9]+", "Caenorhabditis_briggsae");
	geneStableIDToSpecies.put("ENSDARG[0-9]+", "Danio_rerio");
	geneStableIDToSpecies.put("ENSGALG[0-9]+", "Gallus_gallus");
	geneStableIDToSpecies.put("SINFRUG[0-9]+", "Fugu_rubripes");
	geneStableIDToSpecies.put("ENSG[0-9]+",    "Homo_sapiens");
	geneStableIDToSpecies.put("ENSPTRG[0-9]+", "Pan_troglodytes");
	geneStableIDToSpecies.put("ENSRNOG[0-9]+", "Rattus_norvegicus");
	geneStableIDToSpecies.put("ENSAPMG[0-9]+", "Apis_mellifera");
	geneStableIDToSpecies.put("ENSCAFG[0-9]+", "Canis_familiaris");
	geneStableIDToSpecies.put("ENSCING[0-9]+", "Ciona_intestinalis");
	geneStableIDToSpecies.put("ENSXETG[0-9]+", "Xenopus_tropicalis");

	String species = null;

	Iterator it = geneStableIDToSpecies.keySet().iterator();
	while (it.hasNext()) {
	    String regexp = (String)it.next();
	    if (gene.matches(regexp)) {
		species = (String)(geneStableIDToSpecies.get(regexp));
	    }
	}

	return species != null ? ("http://www.ensembl.org/" + species + "/geneview?gene=" + gene) : null;

    }

    // -------------------------------------------------------------------------

    /**
     * Splits input array into "batches" where each 
     * batch length is <=maxBatchSize.
     * 
     * @param maxBatchSize maximum batch size, must be >0.
     * @return array of zero or more long arrays.
     */
    public static long[][] batch(long[] input, int maxBatchSize) {
    
	if (maxBatchSize<1)
	    throw new IllegalArgumentException("maxBatchSize must be >0: " + maxBatchSize);
    
	final int max = input.length;
    
	if (max <= maxBatchSize)
	    return new long[][] { input };
    
	final int oneMore = (input.length % maxBatchSize != 0) ? 1 : 0;
	final int nBatches = input.length / maxBatchSize + oneMore;
	long[][] batches = new long[nBatches][];
    
	for (int i = 0; i<nBatches; i++) {
	    int start = i*maxBatchSize;
	    int end = Math.min((i+1)*maxBatchSize, max);
	    int len = end-start; 
	    long[] batch = new long[len];
	    System.arraycopy(input, start, batch,0,len);
	    batches[i] = batch;
	}
    
	return batches;
    }

    public static final String throwableToStackTraceString(Throwable t) {
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	t.printStackTrace(pw);
	pw.close();
	return sw.toString();
    }
}
