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

package org.biojava.bio.seq.impl;

import java.lang.reflect.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
/**
 * A reverse complement view onto <code>Sequence</code> interface.
 * <p>
 * All features of the underlying sequence are reflected onto the RevCompSequence using a ProjectedFeatureHolder</p>
 * calling createFeature() on a RevCompSequence creates a feature on the underlying
 * sequence. Non-Stranded features will return the reverse compemented view of the sequence
 * when getSymbols() is called that is to say if you get what you expect as if your RevCompSequence
 * was a regular Sequence.
 *
 * @author David Waring
 */
public class RevCompSequence extends SimpleSequence{
    private ProjectedFeatureHolder pfh;
    protected Sequence origSeq;
    
    
    /**
    *  URN, Name and Annotation are copied as is from the original Sequence, unless you use the
    *  the other contructor that sets these.
    */
    
    public RevCompSequence(Sequence seq)throws IllegalAlphabetException{
        this(seq,seq.getURN(),seq.getName(),seq.getAnnotation());
//        pfh = new ProjectedFeatureHolder(seq,this,seq.length()+1,true);
//        pfh.setIsCachingProjections(false);
//        origSeq = seq;
    }
    
    
    public RevCompSequence(Sequence seq, String urn, String name, Annotation annotation)throws IllegalAlphabetException {
        super(DNATools.reverseComplement(seq),urn,name,annotation);
        pfh = new ProjectedFeatureHolder(seq,this,seq.length()+1,true);
        pfh.setIsCachingProjections(false);
        origSeq = seq;
    }
    
    
    // default is off but lets give people the opportunity to change this
    public void setIsCachingProjections(boolean b){
        pfh.setIsCachingProjections(b);
    }

    // SymbolList stuff
    /**
    * edit() will try to edit the underlying Sequence. So if it is editable this will be too
    * <p>Since I have not seen and editable Sequence I have not tested this </p>
    *
    */
    public void edit(Edit e)throws ChangeVetoException,IndexOutOfBoundsException{
        int pos = (this.length() - (e.pos + e.length)) + 2;
        Edit newE = null;
        try {
            newE = new Edit (pos,e.length,DNATools.reverseComplement(e.replacement));
            origSeq.edit(newE);
        }catch (IllegalAlphabetException iae){
            throw new BioError("Error while editing RevCompSequence " + iae.getMessage());
        }
        
    }
    
    
    // Sequence stuff
    public Iterator features(){
        return pfh.features();
    }
    public int countFeatures(){
        return pfh.countFeatures();
    }
    
    /**
    * containsFeature() will return true if this seq contains the feature in question, or
    * if if the original (non reverse complement) sequence contains the feature;
    */ 
    
    public boolean containsFeature(Feature f) {
        return pfh.containsFeature(f) || origSeq.containsFeature(f);
    }
    
    public void removeFeature(Feature f) throws ChangeVetoException{
        pfh.removeFeature(f);
    }
    
//    // for testing
//    public Feature projectFeature(Feature f){
//        return pfh.projectFeature(f);
//    }

    /**
    * createFeature() will call createFeature() on the underlying Sequence.
    * returns the feature as it will be projected onto the reverse complement sequence 
    * not the actual feature that was created.
    *
    */
    public Feature createFeature(Feature.Template ft) throws ChangeVetoException,BioException{
        ft.location = pfh.getProjectedLocation(ft.location);
        if (ft instanceof StrandedFeature.Template){
            ((StrandedFeature.Template)ft).strand = pfh.getProjectedStrand(((StrandedFeature.Template)ft).strand);
    	 }
    	 Feature featureOnOrig = origSeq.createFeature(ft);
    	 return pfh.projectFeature(featureOnOrig);
    }
    
    /**
    * getFeatureFromOriginal() Since you can not create a feature on a projectedFeature at this time, I am 
    * including this method so that you can get the corresponding feature from the original sequence.
    * (which is not projected) and do something with that such as createFeature(). 
    */
    
    public Feature getFeatureFromOriginal(Feature f){
        Feature pFeature;
        Feature oFeature;
        for (Iterator i = origSeq.features();i.hasNext();){
            oFeature = (Feature)i.next();
            pFeature = pfh.projectFeature(oFeature);
           if (pFeature.equals(f)){
                return oFeature;
            }
        }
        return null;
    }
    
    /**
    * clone() should make a complete copy of the Sequence with  all features (and children) and return
    * a SimpleSequence that is unconnected from the orignial sequence.
    */
    
    public Object clone(){
        SymbolList sl = new SimpleSymbolList(this);
        Sequence newSeq = new SimpleSequence(sl,this.getURN(),this.getName(),this.getAnnotation());
        try{
            cloneFeatures(this,newSeq);
        } catch ( BioException e){
            throw new BioError( "Error while cloning RevCompSequenece: " + e.getMessage());
        }
            
        return newSeq;
        
    }
    
    private void cloneFeatures(FeatureHolder source, FeatureHolder dest)throws BioException{
        for (Iterator i = source.features(); i.hasNext();){
            Feature f = (Feature)i.next();
            try {
                Feature newf = dest.createFeature(f.makeTemplate());
                cloneFeatures(f,newf);
            }catch (ChangeVetoException cve){
                throw new BioError("Should be able to create that Feature");
            }
        }
    }
    
    
        
        

}