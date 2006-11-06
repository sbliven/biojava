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

package org.ensembl.util;
import java.util.Properties;

import org.ensembl.datamodel.Location;

/**
 * Utility functions for dealing with sequence data.
 */
public class SequenceUtil {


  private final static String CODON_2_PROTEIN_MAPPING_FILE = "resources/data/codon2protein.properties";

  /** holds codon -> amino acid mappings */
  private static Properties mappings;


  /**
   * Converts string of nuceic acids into string of amino acids. If the
   * string is not of length modulus 3 then it's end is modified: if has 2 of
   * the 3 final bases then an N is appended, however, if it only has 1 then
   * that is removed.
   * @param dna string of nucleic acids. Supports upper and lower case
   * characters.
   * @param cropStopCodon do not create amino acids for and after stop codon id present in dna.
   * @return string of amino acids.
   */
  public static final String dna2protein(String dna, boolean cropStopCodon) 
    throws IllegalArgumentException {
  
    StringBuffer buf = new StringBuffer();
    final int len = dna.length();
    String padded = dna;
    switch( len%3 ) {
    case 1:
      //padded += "NN";
      padded = dna.substring(0, dna.length()-1);
      break;
      
    case 2:
      padded += "N";
      break;
    }
    final int nCodons = padded.length()/3;
    for ( int i=0; i<nCodons; ++i) {
      String aminoAcid = codon2aminoAcid( padded.substring(i*3, (i+1)*3) );
      if (cropStopCodon && i+1==nCodons && "*".equals(aminoAcid))
        break;
      buf.append( aminoAcid );
    }
    
    return buf.toString();
  }

  public static final byte[] dna2protein(byte[] dna)  throws IllegalArgumentException {
  	byte[] padded = new byte[dna.length];
  	final int len = dna.length;
  	
  	int padlength = dna.length;
  	
  	switch(len%3) {    
  		case 1:
  		     padded = new byte[dna.length - 1];
  		     padlength = padded.length;
  		     break;
  		     
  		case 2:
  		     padded = new byte[dna.length + 1];
  		     padlength = dna.length;
  		     padded[padded.length - 1] = 'N';
  		     break;
  	}
	System.arraycopy(dna, 0, padded, 0, padlength);
	
	final int nCodons = padded.length/3;
	byte[] buf = new byte[nCodons];
	
	for(int i = 0; i < nCodons; ++i) {
	    byte[] codon = new byte[3];

	    for(int k = i * 3, coditer = 0; k < (i + 1) * 3; ++k) {
	        codon[coditer] = padded[k];
	        coditer++;
	    }
	    
	    buf[i] = codon2aminoAcid( new String( codon ) ).getBytes()[0];
	}
	
	return buf;
  }

  /**
   * Converts 3 nuceic acids into an amino acid. If codon is unrecognised,
   * e.g. "ANN" then "X" is returned.
   * @param codon 3 nucleic acids. Supports upper and lower case characters.
   * @return amino acid.
   * @throws IllegalArgumentException codon string is wrong length.
   */
  public static String codon2aminoAcid(String codon) 
    throws IllegalArgumentException {

    if ( codon.length() != 3 )
      throw new IllegalArgumentException("codon should be 3 characters: " + codon);

    if ( mappings==null ) 
      mappings = PropertiesUtil.createProperties( CODON_2_PROTEIN_MAPPING_FILE );
    
    String aminoAcid = mappings.getProperty( codon.toUpperCase() );
    if ( aminoAcid==null ) aminoAcid="X";

    return aminoAcid;
  }


	/**
	 * Reverses the string and complements each base.
	 * @param dna
	 * @return reverse complement of dna
	 */
	public static String reverseComplement(String dna) {
		char[] src = dna.toCharArray();
		int seqLen = src.length;
		char[] tgt = new char[seqLen];
		int s=seqLen;
		int t=0;
		while ( t<seqLen) {
		  char base = src[--s];
		  switch(base) {
			// Maintain case sensitivity because some users may attach
			// significance to the uppercase/lowercase distinction.
		  case 'A':
			tgt[t++] = 'T';
			break;

		  case 'T':
			tgt[t++] = 'A';
			break;

		  case 'C':
			tgt[t++] = 'G';
			break;

		  case 'G':
			tgt[t++] = 'C';
			break;

		  case 'a':
			tgt[t++] = 't';
			break;

		  case 't':
			tgt[t++] = 'a';
			break;

		  case 'c':
			tgt[t++] = 'g';
			break;

		  case 'g':
			tgt[t++] = 'c';
			break;

		  default:
			tgt[t++] = base;

		  }
		}

		return new String(tgt);
	}

    public static byte[] reverseComplement(byte[] dna)  {
    	int seqLen = dna.length;
		byte[] tgt = new byte[seqLen];
		int s=seqLen;
		int t=0;
		while ( t<seqLen) {
		  byte base = dna[--s];
		  switch(base) {
			// Maintain case sensitivity because some users may attach
			// significance to the uppercase/lowercase distinction.
		  case 'A':
			tgt[t++] = 'T';
			break;

		  case 'T':
			tgt[t++] = 'A';
			break;

		  case 'C':
			tgt[t++] = 'G';
			break;

		  case 'G':
			tgt[t++] = 'C';
			break;

		  case 'a':
			tgt[t++] = 't';
			break;

		  case 't':
			tgt[t++] = 'a';
			break;

		  case 'c':
			tgt[t++] = 'g';
			break;

		  case 'g':
			tgt[t++] = 'c';
			break;

		  default:
			tgt[t++] = base;

		  }
		}

		return tgt;
    }

    /**
     * Replace a sub sequence from sourceSeq with replacementSeq.
     * 
     * The replacementLoc defines the part of sourceSeq to replace. 
     * 
     * Handles the situation where sourceSeq and replacementSeq on different strands.
     * 
     * @param sourceSeq original sequence string.
     * @param sourceLoc genomic location of source sequence.
     * @param replacementSeq sequence with which to replace the part of sourceSeq corresponding to 
     * replacementLoc. 
     * @param replacementLoc genomic location for replacementSeq.
     * @return string corresponding to the insertion of replacementSeq in sourceSeq.
     */
    public static String replaceSubSequence(String sourceSeq, Location sourceLoc,
        String replacementSeq, Location replacementLoc) {
    
      int originalLen = sourceSeq.length();
    
      if (originalLen != sourceLoc.getLength())
        throw new RuntimeException(
            "Length of original string ("+originalLen+") differs from originalLoc ("+sourceLoc.getLength()+").");
    
      if (replacementLoc.getStart() > sourceLoc.getEnd()
          || replacementLoc.getEnd() < sourceLoc.getStart())
        throw new RuntimeException(
            "AlleleLoc should completely overlap originalLoc.");
    
      int startIdx = replacementLoc.getStart() - sourceLoc.getStart();
      int endIdx = replacementLoc.getEnd() - sourceLoc.getStart();
      
      if (sourceLoc.getStrand() == -1) {
    
        String forwardDNA = reverseComplement(sourceSeq);
        if (replacementLoc.getStrand() == -1) 
          replacementSeq = reverseComplement(replacementSeq);
        forwardDNA = replace(forwardDNA, originalLen, replacementSeq, startIdx, endIdx);
        return reverseComplement(forwardDNA);
        
      } else {
        
        if (replacementLoc.getStrand() == -1) 
          replacementSeq = reverseComplement(replacementSeq);
        return replace(sourceSeq, originalLen, replacementSeq, startIdx, endIdx);
        
      }
    }

    private static String replace(String source, int sourceLen, String substring, int startIdx, int endIdx) {
      String replaced = source.substring(startIdx, endIdx);
    
      if (substring.equals(replaced))
        return source;
    
      String before = source.substring(0, startIdx);
      String after = source.substring(endIdx, sourceLen - 1);
    
      return before + substring + after;
    }
}
