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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;

import cern.colt.list.ObjectArrayList;

/**
 * Update the gene_archive and peptide_archive tables.
 */
public class Archiver {

    private Config conf;
    private Cache cache;
    
    private long mappingSession;
    private long currentPeptideId;
    
	private MessageDigest md;
	
    public Archiver(Config conf, Cache cache ) throws NoSuchAlgorithmException {
    		this.cache = cache;
    		this.conf = conf;
    		md = MessageDigest.getInstance( "MD5" ); 
    }
    
    /**
     * create tuples gene gene_version transcript transcript_version translation translation_version peptide
     * if the subtuple (gene, transcript, translation, peptide) is not present in the new database
     * dump it into the right format (gene archive and peptide archive tables)
     *
     */
    public void createGenePeptideArchive( long mappingSession ) {
    		this.mappingSession = mappingSession;
    		// find the startPeptideId in source database
    		// or modify this on import. Lets try 0 based first and offset on load
    		currentPeptideId = 0l;
    		
    		PrintWriter geneOut, peptideOut;    		
    		try {
    			geneOut= new PrintWriter( new FileWriter( conf.rootDir + File.separator + "gene_archive_new.txt"));
    			peptideOut = new PrintWriter( new FileWriter( conf.rootDir + File.separator + "peptide_archive_new.txt" ));

    		} catch( Exception e ) {
    			System.err.println( "Couldnt open output files");
    			return;
    		}

    		try {
    			Statement stm = conf.getSourceConnection().createStatement();
    			stm.execute( "SELECT max( peptide_archive_id ) FROM peptide_archive" );
    			ResultSet rs = stm.getResultSet();
    			rs.next();
    			currentPeptideId = rs.getLong( 1 ) + 1;
    		} catch( Exception e ) {
    			// something went wrong, do nothing
    		}
    		
    		// iterate through source genes
    		// if gene doesnt exist in target, full dump
    		// if gene exists, check the details
     	ObjectArrayList sourceGenes = cache.getSourceGenesByInternalID().values();
     	ObjectArrayList targetGenes = cache.getTargetGenesByInternalID().values();
     	Map targetGeneByStableId = new HashMap();

     	for( int i=targetGenes.size(); i-->0; ) {
 			Gene g = (Gene) targetGenes.getQuick( i );
 			targetGeneByStableId.put( g.getAccessionID(), g );     		
     	}

     	for( int i=sourceGenes.size(); i-->0; ) {
     		Gene sg = (Gene) sourceGenes.getQuick( i );
     		Gene tg = (Gene) targetGeneByStableId.get( sg.getAccessionID());
     		dumpGene( sg, tg, geneOut, peptideOut );
     	}
     	geneOut.close();
     	peptideOut.close();
    }
   
    private void dumpGene( Gene sourceGene, Gene targetGene, PrintWriter geneOut, PrintWriter peptideOut ) {
    		Iterator i = sourceGene.getTranscripts().iterator();
    		
    		// find out if this is a ncRNA.
    		// if so we add it to the archive even though it doesn't translate
    		Boolean isNcRNA = false;
    		if ( sourceGene.getBioType().equals("miRNA") ||
				 sourceGene.getBioType().equals("misc_RNA") || 
				 sourceGene.getBioType().equals("Mt-tRNA") || 
				 sourceGene.getBioType().equals("Mt-rRNA") || 
				 sourceGene.getBioType().equals("rRNA") || 
				 sourceGene.getBioType().equals("snoRNA") || 
				 sourceGene.getBioType().equals("snRNA") ) {
    			isNcRNA = true;
    		}
    		
    		while( i.hasNext()) {
    			Transcript tr = (Transcript) i.next();
    			Translation tl = tr.getTranslation();
    			
    			if( tl != null ) {
    				
    				// this is a coding transcript and will be archived
	    			
    				String peptide = tl.getPeptide();
	    			if( targetGene == null ) {
	    				dumpTuple( sourceGene, tr , tl, peptide, geneOut, peptideOut );
	    			} else {
	    				Iterator j = targetGene.getTranscripts().iterator();
	    				boolean unchanged = false;
	    				
	    				while( j.hasNext() ) {
	    					Transcript ttr = (Transcript) j.next();
	    					Translation ttl = ttr.getTranslation();
	    					if( ttl == null ) continue;
	    					if( tr.getAccessionID().equals( ttr.getAccessionID()) &&
	    					    tl.getAccessionID().equals( ttl.getAccessionID()) &&
	    					    peptide.equals(ttl.getPeptide())) unchanged = true;
	    				}
	    				if( !unchanged ) 
	    					dumpTuple( sourceGene, tr, tl, peptide, geneOut, peptideOut );
	    			}
    			} else if (isNcRNA) {
    				
    				// we got a ncRNA, which we would like to archive too
    				
	    			if( targetGene == null ) {
	    				dumpNoncodingTuple( sourceGene, tr , geneOut );
	    			} else {
	    				Iterator j = targetGene.getTranscripts().iterator();
	    				boolean unchanged = false;
	    				
	    				while( j.hasNext() ) {
	    					Transcript ttr = (Transcript) j.next();
	    					if( tr.getAccessionID().equals( ttr.getAccessionID() ) ) unchanged = true;
	    				}
	    				if( !unchanged ) 
	    					dumpNoncodingTuple( sourceGene, tr, geneOut );
	    			}
    				
    			}
    			
    			// other non-translating transcripts are not archived
    		}
    }
    
    
    private void dumpTuple( Gene g, Transcript tr, Translation tl, String peptide, PrintWriter geneOut, PrintWriter peptideOut ) {

		byte[] checksumBytes;
		
		try {
			checksumBytes = peptide.getBytes( "ISO-8859-1" );
		} catch( Exception e ) {
			throw new RuntimeException( "ISO-8859-1 not supported, should be!" );
		}
		
		md.update( checksumBytes );
		byte[] checksum = md.digest();
		md.reset();
		
		geneOut.println( g.getAccessionID() + "\t" +  g.getVersion() + "\t" +
					tr.getAccessionID() + "\t" +  tr.getVersion() + "\t" +
					tl.getAccessionID() + "\t" + tl.getVersion() + "\t" +
					currentPeptideId + "\t" +  mappingSession );
		
		peptideOut.println( currentPeptideId + "\t" +  hexDumpBytes( checksum ) + "\t" + peptide ) ;
		currentPeptideId++;
    }
    
    private void dumpNoncodingTuple( Gene g, Transcript tr, PrintWriter geneOut ) {

		geneOut.println( g.getAccessionID() + "\t" +  g.getVersion() + "\t" +
					tr.getAccessionID() + "\t" +  tr.getVersion() +
					"\t\\N\t\\N\t\\N\t" +  mappingSession );
		
    }
    
    public String hexDumpBytes(byte[] bytes) {
		StringBuffer res = new StringBuffer();
		byte singleByte, nibble;
		
		for (int i = 0; i < bytes.length; i++) {
			singleByte = bytes[i];
			for (int j = 0; j < 2; j++) {
				if (j == 0) {
					nibble = (byte) ((singleByte >> 4) & 0xf);
				} else {
					nibble = (byte) (singleByte & 0xf);
				}
				if (nibble <= 9) {
					res.append(nibble);
				} else {
					nibble -= 10;
					res.append((char) ('A' + nibble));
				}
			}
		}

		return res.toString();
	}
    // -------------------------------------------------------------------------
}
