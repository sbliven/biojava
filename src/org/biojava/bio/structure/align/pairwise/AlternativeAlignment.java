/*
 *                  BioJava development code
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
 * Created on Jan 28, 2006
 *
 */
package org.biojava.bio.structure.align.pairwise;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.StrucAligParameters;
import org.biojava.bio.structure.align.helper.AlignTools;
import org.biojava.bio.structure.align.helper.IndexPair;
import org.biojava.bio.structure.align.helper.JointFragments;
import org.biojava.bio.structure.jama.Matrix;

/**
 * Implements a class which handles one possible (alternative) solution.

     Alternative alignments arise from different seed
     alignments or seed FPairs. The AltAlg class contains methods
     for refinement (Dynamic Programming based) and filtering
     (i.e. removing probably wrongly matched APairs). In the refinement
     phase, different seed alignments can converge to the same solution.
    
 * @author Andreas Prlic, 
 * @author Peter Lackner (original Python and C code)
 * @since 3:04:26 PM
 * @version %I% %G%
 */
public class AlternativeAlignment {
   
    
    int[] idx1;
    int[] idx2;
    String[] pdbresnum1;
    String[] pdbresnum2;
    //short[] alig1;
    //short[] alig2;
    
    int nfrags;
    List flist;
    Atom center;
    Matrix rot;
    Atom tr;
    
    
    // the scores...
    int gaps0;
    int eqr0;
    int rms0;
    int joined;
    int percId;
    int cluster;
    float score;
    IndexPair[] aligpath;
    int fromia;
    Matrix currentRotMatrix;
    Atom currentTranMatrix;
    
    double rms;
    

    public static Logger logger =  Logger.getLogger("org.biojava.bio.structure.align");
    
    
    public AlternativeAlignment() {
        super();
        
        nfrags = 0;
        aligpath = new IndexPair[0];
        //alig1 = new short[0];
        //alig2 = new short[0];
        idx1 = new int[0];
        idx2 = new int[0];
        flist = new ArrayList();
        center = new AtomImpl();
        rot = null;
        tr = new AtomImpl();
        eqr0 = -99;
        rms0 = 99;
        joined = 0;
        gaps0 = -99;
        fromia = 0;
        
        currentRotMatrix = new Matrix(0,0);
        currentTranMatrix = new AtomImpl();
    }
    
    
    
    
    /** print the idx positions of this alignment
     * 
     * @return a String representation
     */
    public String toString(){
    	DecimalFormat d2 = new DecimalFormat();
    	 	// the result can be localized. To change this and enforce UK local do...
    	 	//(DecimalFormat)NumberFormat.getInstance(java.util.Locale.UK);
         d2.setMaximumIntegerDigits(3);	
         d2.setMinimumFractionDigits(2);
         d2.setMaximumFractionDigits(2);
        StringBuffer s = new StringBuffer();
        s.append("#" + getAltAligNumber() +
        		" cluster:" + cluster +
        		" eqr:" + getEqr() + 
        		" rmsd:" + d2.format(getRmsd()) +
        		" %id:" + getPercId() + 
        		" gaps:" + getGaps() + 
        		" score:" + d2.format(score)	);
        /*
        s.append("idx1:");
        for (int i=0;i< idx1.length;i++){
            s.append(" " + idx1[i] );
        }
        s.append( "\nidx2:");
        for (int i=0;i< idx2.length;i++){
            s.append(" " + idx2[i]) ;
        }*/
        return s.toString();
    }
    

    /** get the number of the cluster this alignment belongs to
     * 
     * @return an int giving the number of the cluster
     */
    public int getCluster() {
		return cluster;
	}



    /** set the number of the cluster this alignment belongs to.
     * All alignments in a cluster are quite similar.
     * 
     * @param cluster the number of the cluster
     */
	public void setCluster(int cluster) {
		this.cluster = cluster;
	}




	public double getRmsd() {
        return rms;
    }

    /** the rms in the structurally equivalent regions
     * 
     * @param rms
     */
    public void setRms(double rms) {
        this.rms = rms;
    }

    /** the alignment score
     * 
     * @return the score of this alignment
     */
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
    
    
    /** return the number of gaps in this alignment
     * 
     * @return the number of Gaps
     */
    public int getGaps(){
        return gaps0;
    }
    
    /** returns the number of euqivalent residues in this alignment
     * 
     * @return the number of equivalent residues
     */
    public int getEqr(){
       return eqr0 ; 
    }
    
    /** the positions of the structure equivalent positions in atom set 1
     * 
     * @return the array of the positions
     */
    public int[] getIdx1(){
        return idx1;
    }
    
    /** the positions of the structure equivalent atoms in atom set 2
     * 
     * @return the array of the positions
     */
    public int[] getIdx2(){
        return idx2;
    }
    
    public int getPercId() {
		return percId;
	}

    public void setPercId(int percId) {
		this.percId = percId;
	}

	/** Set apairs according to a seed position.
     * 
     * @param l
     * @param i
     * @param j
     */
    public void  apairs_from_seed(int l,int i, int j){
        aligpath = new IndexPair[l];
        idx1 = new int[l];
        idx2 = new int[l];
        for (int x = 0 ; x < l ; x++) {
            idx1[x]=i+x;
            idx2[x]=j+x;
            aligpath[x] = new IndexPair((short)(i+x),(short)(j+x));
        }   
    }

    /** Set apairs according to a list of (i,j) tuples.
     * 
     * @param jf a JoingFragment
     */
    public void apairs_from_idxlst(JointFragments jf) {
        List il = jf.getIdxlist();
        //System.out.println("Alt Alig apairs_from_idxlst");
        
        aligpath = new IndexPair[il.size()];
        idx1 = new int[il.size()];
        idx2 = new int[il.size()];
        for (int i =0 ; i < il.size();i++) {
            int[] p = (int[])il.get(i);
            //System.out.print(" idx1 " + p[0] + " idx2 " + p[1]);
            idx1[i] = p[0];
            idx2[i] = p[1];
            aligpath[i] = new IndexPair((short)p[0],(short)p[1]);
            
        }
        eqr0  = idx1.length;
        //System.out.println("eqr " + eqr0);
        gaps0 = count_gaps(idx1,idx2);

    }

    
    /** returns the sequential number of this alternative alignment
     * 
     * @return the sequential number of this alternative alignment 
     */
    public int getAltAligNumber() {
        return fromia;
    }

    public void setAltAligNumber(int fromia) {
        this.fromia = fromia;
    }
    
    /** rotate and shift atoms with currentRotMatrix and current Tranmatrix
     * 
     * @param ca
     */
    private void rotateShiftAtoms(Atom[] ca){
       
       for (int i  = 0 ; i < ca.length; i++){
           Atom c = ca[i];
          
           Calc.rotate(c,currentRotMatrix);
           Calc.shift(c,currentTranMatrix);
           //System.out.println("after " + c);
           ca[i] = c;
       }
       //System.out.println("after " + ca[0]);
    }
    
    public void finish(StrucAligParameters params,Atom[]ca1,Atom[]ca2) throws StructureException{
        
        Atom[] ca3 = new Atom[ca2.length];
        for ( int i = 0 ; i < ca2.length;i++){
            ca3[i] = (Atom) ca2[i].clone();
            
        }
        // do the inital superpos...
       
        super_pos_alig(ca1,ca3,idx1,idx2,true);
        rotateShiftAtoms(ca3);
        
        calcScores(ca1,ca2);
       logger.fine("eqr " + eqr0 + " " + gaps0 + " "  +idx1[0] + " " +idx1[1]);
       
       getPdbRegions(ca1,ca2);
        
    }
    
    
    private Matrix getScoreMatrixFromDistanceMatrix(Matrix dismat , float co, int m1l, int m2l){

        Matrix sim = new Matrix(m1l,m2l);
//      convert the distances into a score
        // which is stored in the sim matrix
        for (int i=0;i< m1l;i++){
            for (int j=0;j<m2l;j++){
            
                double d = dismat.get(i,j);
               
                if (d > co) {
                    sim.set(i,j,0);
                } else {
                    // Peter Lackner tested several approaches from different authors for converting distances to scores
                    // there are no big differences, but probably worthwhile to optimize
                    double s =  2 * co / ( 1 + ( d/co) * (d/co)) - co;
                    sim.set(i,j,s); 
                    //System.out.println(i + " " + j + " " + " " + d + " " + sim.get(i,j));
                }   
            }
        }
        return sim;
    }
    
    
    /** Refinement procedure based on superposition and dynamic programming. 
     * Performs an iterative refinement. Several methods apply such a procedure,
     * e.g. CE or ProSup. Here we additionally test for circular permutation,
     * which are in the same frame of superposition as the optimal alignment.
     * This feature may be switched off by setting permsize to -1.
     * 
     * @param params the parameters
     * @param ca1 atoms of structure 1
     * @param ca2 atoms of structure 2
     * @throws StructureException
     */
         
    public void refine(StrucAligParameters params,Atom[]ca1,Atom[]ca2) throws StructureException{
       // System.out.println("refine Alternative Alignment #"+ getAltAligNumber()+" l1:" + ca1.length + " l2:" + ca2.length);
//        for ( int i= 0 ; i < idx1.length;i++){
//            System.out.println(idx1[i] + " " + idx2[i]);
//        }
        
        // avoid any operations on the original Atoms ...
        Atom[] ca3 = new Atom[ca2.length];
        for ( int i = 0 ; i < ca2.length;i++){
            ca3[i] = (Atom) ca2[i].clone();
            
        }
       //Atom[] ca3 = (Atom[]) ca2.clone();
       
       // do the inital superpos...
        
       super_pos_alig(ca1,ca3,idx1,idx2,false);
       
       //JmolDisplay jmol1 = new JmolDisplay(ca1,ca2,"after first alig " );
       //jmol1.selectEquiv(idx1,idx2);
       //int[] jmoltmp1 = idx1;
       //int[] jmoltmp2 = idx2;
       
       int lenalt =idx1.length;
       int lenneu = aligpath.length;
       //Matrix rmsalt = currentRotMatrix;
     
       float co = params.getCreate_co();
       
       int ml = Math.max(ca1.length, ca3.length);
       idx1 = new int[ml];
       idx2 = new int[ml];
       
       int m1l = ca1.length;
       int m2l = ca3.length;
       
       Matrix sim = new Matrix(ca1.length,ca3.length,0);
       
       int maxiter = params.getMaxIter();
       for (int iter = 0 ; iter< maxiter; iter++){
           
           float  subscore = 0.0f;          
         
           //System.out.println("before " + c2[0]);
           rotateShiftAtoms(ca3);
           //
           
           //System.out.println("after " + c2[0]);
           //System.out.println("after " + ca2[0]);
             
           // get the matrix of all distances
           
           Matrix dismat = AlignTools.getDistanceMatrix(ca1,ca3);
           
           sim = getScoreMatrixFromDistanceMatrix(dismat,co,m1l,m2l);
           
           
          
           
           // do the dynamic alignment...
           Alignable ali;
           try {
            ali = AligNPE.align_NPE(sim,params);
           } catch (Exception e){
               e.printStackTrace();
               return;
           }
           this.score = ali.getScore();
           //System.out.println("score " + score);
           IndexPair[] path = ali.getPath();
         
           int pathsize = ali.getPathSize();
           
           
           // now for a superimposable permutation. First we need to check the size and
           // position of the quadrant left out by the optimal path
           int firsta = path[0].getRow();
           int firstb = path[0].getCol();
           int lasta  = path[pathsize-1].getRow();
           int lastb  = path[pathsize-1].getCol();
           
           int quada_beg, quada_end, quadb_beg,quadb_end;
           
           if ( firsta == 0){
                quada_beg = lasta +1;
                quada_end = ca1.length -1;
                quadb_beg = 0;
                quadb_end = firstb-1;
           } else {
               quada_beg = 0;
               quada_end = firsta-1;
               quadb_beg = lastb+1;
               quadb_end = ca3.length -1;
           }
           
          // if the unaligned quadrant is larger than permsize 
           int permsize = params.getPermutationSize();
           
           if ( permsize > 0 &&
                   (quada_end - quada_beg >= permsize) &&
                   ( quadb_end - quadb_beg >= permsize)) {
               
               Matrix submat = new Matrix(quada_end-quada_beg, quadb_end - quadb_beg) ;
               //then we copy the score values of the quadrant into a new matrix
               
               //System.out.println(quada_beg + " " + quada_end + " " +quadb_beg+" " + quadb_end);  
               //System.out.println(sim.getRowDimension() + " " + sim.getColumnDimension());
               for ( int s = quada_beg; s < quada_end; s++){
                   for ( int t = quadb_beg; t < quadb_end; t++){
                       //System.out.println(s+" " +t);
                       double val = sim.get(s,t);
                       submat.set(s-quada_beg,t-quadb_beg,val);
                   }
               }
               // and perform a dp alignment again. (Note, that we fix the superposition).
               
               Alignable subali = AligNPE.align_NPE(submat,params);
               
               subscore = subali.getScore();
               //System.out.println("join score" + score + " " + subscore);
               this.score = score+subscore;
               
               IndexPair[] subpath = subali.getPath();
               int subpathsize = subali.getPathSize();
               for ( int p=0;p<subpath.length;p++){
                   IndexPair sp = subpath[p];
                   sp.setRow((short)(sp.getRow()+quada_beg));
                   sp.setCol((short)(sp.getCol()+quadb_beg));
               }
               
               // now we join the two solutions
               if ( subpathsize > permsize){
                   IndexPair[] wholepath = new IndexPair[pathsize+subpathsize];
                   for ( int t=0; t < pathsize; t++){
                       wholepath[t] = path[t];
                   }
                   for ( int t=0 ; t < subpathsize; t++){
                       wholepath[t+pathsize] = subpath[t];
                   }
                   pathsize += subpathsize;
                   path = wholepath;
                   ali.setPath(path);
                   ali.setPathSize(pathsize);
               }
                
           }
           
           
           int j =0 ;
           //System.out.println("pathsize,path " + pathsize + " " + path.length);
           
           for (int i=0; i < pathsize; i++){
               int x = path[i].getRow();
               int y = path[i].getCol();
               //System.out.println(x+ " " + y);
               double d = dismat.get(x,y);
               // now we apply the evaluation distance cutoff 
               if ( d < params.getEvalCutoff()){
                   //System.out.println("j:"+j+ " " + x+" " + y + " " + d );
                   
                   idx1[j] = x;
                   idx2[j] = y;
                   j+=1;
               }               
           }
           
           lenneu = j;
           //System.out.println("lenalt,neu:" + lenalt + " " + lenneu);
          
           
           int[] tmpidx1 = new int[j];
           int[] tmpidx2 = new int[j];
           String idx1str ="idx1: ";
           String idx2str ="idx2: ";
           for (int i = 0 ; i<j;i++){
               idx1str += idx1[i]+ " ";
               idx2str += idx2[i]+ " ";
               tmpidx1[i] = idx1[i];
               tmpidx2[i] = idx2[i];
           }
           //System.out.println(idx1str);
           //System.out.println(idx2str);
           //jmol.selectEquiv(idx1,idx2);
           
           //if ( j > 0)
             //  System.out.println("do new superimpos " + idx1.length + " " + idx2.length + " " + idx1[0] + " " + idx2[0]);
           super_pos_alig(ca1,ca3,tmpidx1,tmpidx2,false);
           
           this.aligpath = path;
           
           if (lenneu == lenalt)
               break;
           
       }
       
       
       
       idx1 = (int[]) FragmentJoiner.resizeArray(idx1,lenneu);
       idx2 = (int[]) FragmentJoiner.resizeArray(idx2,lenneu);
//     new ... get rms...
       // now use the original atoms to get the rotmat relative to the original structure...
       super_pos_alig(ca1,ca2,idx1,idx2,true);
       eqr0 = idx1.length;
       gaps0 = count_gaps(idx1,idx2);
       
      getPdbRegions(ca1,ca2);
       //System.out.println("eqr " + eqr0 + " aligpath len:"+aligpath.length+ " gaps:" + gaps0 + " score " + score);
    }
    
    private void getPdbRegions(Atom[] ca1, Atom[] ca2){
        pdbresnum1 = new String[idx1.length];
        pdbresnum2 = new String[idx2.length];
        
        for (int i =0 ; i < idx1.length;i++){
            Atom a1 = ca1[idx1[i]];
            Atom a2 = ca2[idx2[i]];
            Group p1 = a1.getParent();
            Group p2 = a2.getParent();
            Chain c1 = p1.getParent();
            Chain c2 = p2.getParent();
            
            String cid1 = c1.getName();
            String cid2 = c2.getName();
            
            String pdb1 = p1.getPDBCode();
            String pdb2 = p2.getPDBCode();
           

            if ( ! cid1.equals(" "))
                pdb1 += ":" + cid1;
           
            
            if ( ! cid2.equals(" "))
                pdb2 += ":" + cid2;
            

            pdbresnum1[i] = pdb1;
            pdbresnum2[i] = pdb2;
        }
    }
    
    
    public String[] getPDBresnum1() {
        return pdbresnum1;
    }




    public void setPDBresnum1(String[] pdbresnum1) {
        this.pdbresnum1 = pdbresnum1;
    }




    public String[] getPDBresnum2() {
        return pdbresnum2;
    }




    public void setPDBresnum2(String[] pdbresnum2) {
        this.pdbresnum2 = pdbresnum2;
    }




    /** Count the number of gaps in an alignment represented by idx1,idx2.
     * 
     * @param idx1
     * @param idx2
     * @return the number of gaps in this alignment
     */
    private int count_gaps(int[] i1, int[] i2){
  
        int i0 = i1[0];
        int j0 = i2[0];
        int gaps = 0;
        for (int i =1 ; i<i1.length;i++ ){
            if ( Math.abs(i1[i]-i0) != 1 ||
                    ( Math.abs(i2[i]-j0) != 1)){
                gaps +=1;
            }
            i0 = i1[i];
            j0 = i2[i];
        }
        
        return gaps;
    }
    
    
    
   
    
    public void calculateSuperpositionByIdx(Atom[] ca1, Atom[] ca2)throws StructureException {
        
        super_pos_alig(ca1,ca2,idx1,idx2,false);
        
    }
    
    /** Superimposes two molecules according to residue index list idx1 and idx2.
     *  Does not change the original coordinates.
     *  as an internal result the rotation matrix and shift vectors for are set
     * 
     * @param ca1 Atom set 1
     * @param ca2 Atom set 2
     * @param idx1 idx positions in set1
     * @param idx2 idx positions in set2
     * @param getRMS a flag if the RMS should be calculated
     * @throws StructureException
     */
        
          
          
    private void super_pos_alig(Atom[]ca1,Atom[]ca2,int[] idx1, int[] idx2, boolean getRMS) throws StructureException{
        
        //System.out.println("superpos alig ");
        Atom[] ca1subset = new Atom[idx1.length];
        Atom[] ca2subset = new Atom[idx2.length];
        
        for (int i = 0 ; i < idx1.length;i++){
            //System.out.println("idx1 "+ idx1[i] + " " + idx2[i]);
            int pos1 =  idx1[i];
            int pos2 =  idx2[i];
            
            ca1subset[i] =  ca1[pos1];
            ca2subset[i] = (Atom) ca2[pos2].clone();
        }
        
        SVDSuperimposer svd = new SVDSuperimposer(ca1subset,ca2subset);
        this.currentRotMatrix  = svd.getRotation();
        this.currentTranMatrix = svd.getTranslation();
        //currentRotMatrix.print(3,3);
        if ( getRMS) {
            rotateShiftAtoms(ca2subset);
            this.rms = SVDSuperimposer.getRMS(ca1subset,ca2subset);
        }
      
       
    }
    
    
    /** returns the rotation matrix that needs to be applied to structure 2 to rotate on structure 1
     * 
     * @return the rotation Matrix
     */
    public Matrix getRotationMatrix(){
        return currentRotMatrix;
    }
    
    /** returns the shift vector that has to be applied on structure to to shift on structure one
     * 
     * @return the shift vector
     */
    public Atom getShift(){
        return currentTranMatrix;
    }

    
    
    /** calculates varios scores for this alignment like %id
     * @param ca1 set of Atoms for molecule 1 
     * @param ca2 set of Atoms for molecule 2

     */
    public void calcScores(Atom[] ca1, Atom[] ca2){
    		eqr0 = idx1.length;
    		gaps0 = count_gaps(idx1,idx2);
    		
    		percId = 0;
    		// calc the % id
    		for (int i=0 ; i< idx1.length; i++){
    			 Atom a1 = ca1[idx1[i]];
    			 Atom a2 = ca2[idx2[i]];
    			 
    			 Group g1 = a1.getParent();
    			 Group g2 = a2.getParent();
    			 if ( g1.getPDBName().equals(g2.getPDBName())){
    				 percId++;
    			 }
    			 
    		}
    }
    
    /** converts the alignment to a PDB file
     * each of the structures will be represented as a model.
     * 
     *  
     * @param s1
     * @param s2
     * @return a PDB file as a String
     */
    public String toPDB(Structure s1, Structure s2){
        
        
        // do not change the original coords ..
        Structure s3 = (Structure)s2.clone();
       
        currentRotMatrix.print(3,3);
        
        Calc.rotate(s3, currentRotMatrix);
        Calc.shift( s3, currentTranMatrix);
       
        Structure newpdb = new StructureImpl();
        newpdb.setPDBCode("Java");
        newpdb.setName("Aligned with BioJava");
        newpdb.setNmr(true);
        
        newpdb.addModel(s1.getChains(0));
        newpdb.addModel(s3.getChains(0));
        
        return newpdb.toPDB();
    }
    
    
    
}

