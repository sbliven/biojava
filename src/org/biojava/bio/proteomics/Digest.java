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

package org.biojava.bio.proteomics;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.Annotation;

import org.biojava.bio.BioException;
import org.biojava.utils.ChangeVetoException;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This class contains methods for calculating the results of proteolytic digestion
 * of a protein sequence.
 * @author Michael Jones
 */
public class Digest {
    
    private Protease protease;
    
    private Sequence sequence;
    
    private int maxMissedCleavages = 0;
    
    public static String PEPTIDE_FEATURE_TYPE = "Peptide";
    
    public LinkedList peptideQue;
    // private
    
    /** Creates new Digest */
    public Digest() {
        try{
            protease = new Protease();
        }catch (Exception e){
            //Should never happen
            e.printStackTrace();
        }
    }
    
    public void setProtese(Protease protease) {
        this.protease = protease;
    }
    
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
    
    public Sequence getSequence() {
        return sequence;
    }
    
    public void setMaxMissedCleavages(int maxMissedCleavages) {
        this.maxMissedCleavages = maxMissedCleavages;
    }
    
    /** Adds peptides as features to the Sequence in this class.
     * For Example:
     * <CODE>
     * Protease protease = Protease.getProteaseByName(Protease.TRYPSIN);
     *         Sequence sequence = ...
     *         Digest bioJavaDigest = new Digest();
     *
     *         bioJavaDigest.setMaxMissedCleavages(2);
     *         bioJavaDigest.setProtese(Protease.getProteaseByName(Protease.ASP_N));
     *         bioJavaDigest.setSequence(sequence);
     *         bioJavaDigest.addDigestFeatures();
     * </CODE>
     * @throws BioException
     */
    public void addDigestFeatures() throws BioException, ChangeVetoException {
        peptideQue = new LinkedList();
        
        if(protease == null){
            throw new BioException("Protease is null");
        }
        
        List cleaveSites = protease.getCleaveageResidues().toList();
        boolean endoProtease = protease.isEndoProtease();
        List notCleave = protease.getNotCleaveResidues().toList();
        int nTerm = 1;
        
        if(cleaveSites == null || notCleave == null){
            throw new BioException("Protease contains null parameter");
        }
        
        for (int j = 1; j <= sequence.length(); j++) {
            Symbol aa = sequence.symbolAt(j);
            if(cleaveSites.contains(aa)){
                if (endoProtease) {
                    boolean cleave = true;
                    if (j < sequence.length())  {
                        Symbol nextAA = sequence.symbolAt(j+1);
                        if(notCleave.contains(nextAA)){
                            cleave = false;
                        }
                    }
                    
                    if (cleave)  {
                        Location loc = new RangeLocation(nTerm, j);
                        peptideQue.add(loc);
                        nTerm = j + 1;
                    }
                } else {
                    if (j > 1) {
                        Location loc = new RangeLocation(nTerm, j-1);
                        peptideQue.add(loc);
                        System.out.println(peptideQue);
                        nTerm = j;
                    }
                }
            }
        }
        
        if (nTerm <= sequence.length()) {
            Location loc = new RangeLocation(nTerm, sequence.length());
            peptideQue.add(loc);
        }
        
        addMissedCleavages();
        
        //Now add the locations as Peptide freatures to the Sequence
        for(ListIterator li = peptideQue.listIterator(); li.hasNext(); ){
            createPeptideFeature((Location)li.next());
        }
    }
    
    private void addMissedCleavages() throws BioException {
        LinkedList missedList = new LinkedList();
        if(maxMissedCleavages>0){
            for(ListIterator li = peptideQue.listIterator(); li.hasNext(); ){
                Location loc = (Location)li.next();
                Location loc2 = null;
                int min = loc.getMin();
                int max = 0;
                
                //Get the numMissedCleavages location ahead of the current location
                int numAdvanced = 0;
                for(int i=0; i<maxMissedCleavages; i++){
                    if(li.hasNext()) {
                        numAdvanced++;
                        loc2 = ((Location)li.next());
                        max = loc2.getMax();
                        missedList.add(new RangeLocation(min, max));
                    }
                }
                //Revert back to the original location
                for(int i=0; i<numAdvanced; i++){
                    loc = ((Location)li.previous());
                }
            }
            //Add all the missed peptides to the overall list
            peptideQue.addAll(missedList);
        }
    }
    
    private void createPeptideFeature(Location loc)
    throws BioException, ChangeVetoException {
        Feature.Template template = new Feature.Template();
        template.type = PEPTIDE_FEATURE_TYPE;
        template.source = this.getClass().getName();
        template.location = loc;
        template.annotation = Annotation.EMPTY_ANNOTATION;
        sequence.createFeature(template);
    }
}
