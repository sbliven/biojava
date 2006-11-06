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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.Exon;

import cern.colt.list.ObjectArrayList;

/**
 * Dumps exon sequence from database to a FASTA file. Exons are filtered based on a ScoredMappingMatrix.
 */
public class ExonDumper2 {

    private static final int MIN_EXON_LENGTH = 15;

    // -------------------------------------------------------------------------
    /**
     * Dump exons from source and target databases to fasta files. Only those exons that have scores of less than 1 in the matrix
     * are dumped. Short exons (less than MIN_EXON_LENGTH) are not dumped.
     * 
     * @param cache The cached exons etc.
     * @param matrix The matrix defining the scores of the already-mapped exons.
     * @param rootDir The root directory to use.
     */
    public void dumpFilteredExons(Cache cache, ScoredMappingMatrix matrix, String rootDir) {

        List sourceExons = filterExons(cache.getSourceExonsByInternalID().values(), matrix, true);
        writeExons(sourceExons, rootDir + File.separator + System.getProperty("idmapping.source.database"));

        List targetExons = filterExons(cache.getTargetExonsByInternalID().values(), matrix, false);
        writeExons(targetExons, rootDir + File.separator + System.getProperty("idmapping.target.database"));

    }

    // -------------------------------------------------------------------------
    /**
     * Filter a list of exons according to a matrix of scores.
     * 
     * @param allExons The unfiltered exons.
     * @param matrix A matrix of scores from a previous mapping. Exons with a score less than 1 in this are not dumped.
     * @param isSource True if we are dealing with source exons, false if target.
     * @return A list of the filtered exons; the sequence is not fetched yet.
     */
    private List filterExons(ObjectArrayList allExons, ScoredMappingMatrix matrix, boolean isSource) {

        List exons = new ArrayList();

        int totalExons = 0;

        final int n = allExons.size();
        for(int i=0; i<n; ++i) {
            Exon e = (Exon) allExons.getQuick(i);
            totalExons++;
            if (wantExon(e, matrix, isSource)) {
                exons.add(e);
            }
        }

        System.out.println("Got a total of " + totalExons + " exons; after filtering there were " + exons.size());

        return exons;

    }

    // -------------------------------------------------------------------------
    /**
     * Check whether a particular exon is "wanted", i.e. has a score < 1 in the matrix, and is not too short.
     * 
     * @param e The exon to check.
     * @param matrix The scores to check against.
     * @param isSource true if this is a source exon, false if it is a target exon.
     * @return true if the exon should be dumped.
     */
    private boolean wantExon(Exon e, ScoredMappingMatrix matrix, boolean isSource) {

        // filter out artificially short exons
        if (e.getLocation().getLength() < MIN_EXON_LENGTH) {
            return false;
        }

        // only dump exons that DO NOT have a mapping of 1
        List mappings = null;
        if (isSource) {
            mappings = matrix.sourceEntries(e.getInternalID());
        } else {
            mappings = matrix.targetEntries(e.getInternalID());
        }
        Iterator it = mappings.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            if (entry.getScore() > 0.9999f) { // less likely to cause rounding errors than == 1
                return false;
            }
        }

        return true;
    }

    // -------------------------------------------------------------------------
    /**
     * Write a list of exons to a FASTA file.
     * 
     * @param exons The exons to a file.
     * @param outputFileNameBase The start of the file name. _exons.fasta is appended to this.
     */
    private void writeExons(List exons, String outputFileNameBase) {

        String outputFileName = outputFileNameBase + "_exons.fasta";

        int written = 0;

        try {

            System.out.println("Writing exons to " + outputFileName);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFileName));

            for (Iterator iter = exons.iterator(); iter.hasNext();) {
                Exon exon = (Exon) iter.next();
                writeExon(exon, writer);
                written++;
            }

            writer.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

        System.out.println("Wrote " + written + " exons to " + outputFileName);

    }

    // -------------------------------------------------------------------------
    /**
     * Write an exon in FASTA format.
     * 
     * @param exon The exon to be written. Note that getSequence() is only called here to save on memory.
     * @param writer The stream to write to.
     */
    private static void writeExon(Exon exon, OutputStreamWriter writer) throws IOException {

        String id = Long.toString(exon.getInternalID());
        String sequence = exon.getSequence().getString();
        writer.write('>');
        writer.write(id);
        writer.write('\n');
        writer.write(sequence);
        writer.write('\n');

    }

    // -------------------------------------------------------------------------

}