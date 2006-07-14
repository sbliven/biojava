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
 * Created on May 21, 2006
 *
 */
package org.biojava.bio.structure.align;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.align.helper.AlignTools;
import org.biojava.bio.structure.align.helper.JointFragments;
//import org.biojava.bio.structure.align.jmol.MatrixJPanel;
import org.biojava.bio.structure.align.pairwise.AltAligComparator;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;
import org.biojava.bio.structure.align.pairwise.FragmentJoiner;
import org.biojava.bio.structure.align.pairwise.FragmentPair;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.bio.structure.jama.Matrix;

/* Perform a pairwise protein structure superimposition.
 * The algorithm is based and heavily influenced by the StrComPy package by Peter Lackner.
 * 
 * It is a distance matrix based, rigid body protein structure superimposition.
 *  
 * @author Andreas Prlic
 * @author Peter Lackner
 * @since 1.4
 * @version %I% %G%
 */
public class StructurePairAligner {
    AlternativeAlignment[] alts;
    Matrix distanceMatrix;
    StrucAligParameters params;
    FragmentPair[]  fragPairs;
    
    
    public StructurePairAligner() {
        super();
        params = StrucAligParameters.getDefaultParameters();
        reset();
        alts = new AlternativeAlignment[0];
        distanceMatrix = new Matrix(0,0);
    }
    
    

    
    /** example usage of this class
     * 
     * @param args
     */
    public static void main(String[] args){
        try {
          

            // UPDATE THE FOLLOWING LINES TO MATCH YOUR SETUP

            PDBFileReader pdbr = new PDBFileReader();          
            pdbr.setPath("/Users/ap3/WORK/PDB/20051205");
            String pdb1 = "1boo";
            String pdb2 = "1xva";            
            String outputfile = "/Users/ap3/tmp/alig_"+pdb1+"_"+pdb2+".pdb";
          
            // NO NEED TO DO CHANGE ANYTHING BELOW HERE...
            
            StructurePairAligner sc = new StructurePairAligner();
            
            
            // step1 : read molecules
            
            
            
            System.out.println("aligning " + pdb1 + " vs. " + pdb2);
            Structure s1 = pdbr.getStructureById(pdb1);
            Structure s2 = pdbr.getStructureById(pdb2);                       
          
            // step 2 : do the calculations
            sc.align(s1,s2);
          
            
            // print the result:
            // the AlternativeAlignment object gives also access to rotation matrices / shift vectors.
            AlternativeAlignment[] aligs = sc.getAlignments();
            for (int i=0 ; i< aligs.length; i ++){
                AlternativeAlignment aa = aligs[i];
                String txt = "alig " + (i+1) + " eqr: " + aa.getEqr() + " rms: " + aa.getRms() + " gaps: " + aa.getGaps() + " score:" + aa.getScore();
                System.out.println(txt);
               
            }
            
            // convert AlternativeAlignemnt 1 to PDB file, so it can be opened with a viewer (e.g. Jmol, Rasmol)
            
            if ( aligs.length > 0) {
                AlternativeAlignment aa1 =aligs[0];
                String pdbstr = aa1.toPDB(s1,s2);
                
                System.out.println("writing alignment to " + outputfile);
                FileOutputStream out= new FileOutputStream(outputfile); 
                PrintStream p =  new PrintStream( out );
        
                p.println (pdbstr);

                p.close();
                out.close();
            }
            
            
            
//          TODO: commit the MatrixJPanel class
            
            //Structure s3 = (Structure)s2.clone();
            // finally: display the results...           
           /*Atom[]ca1 = StructureTools.getAtomCAArray(s1);
            Atom[] ca2 = StructureTools.getAtomCAArray(s2);
            StrucAligParameters params = sc.getParams();
            int fragmentLength = params.getFragmentLength();
            FragmentPair[] fragPairs = sc.getFragmentPairs();
           // AlternativeAlignment[] aligs = sc.getAlignments();
            
            MatrixJPanel.show(sc.getDistMat(), s1,s2,ca1,ca2,fragmentLength, fragPairs, aligs);
            */
            
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }

    
    
    private void reset(){
        alts = new AlternativeAlignment[0];
        distanceMatrix = new Matrix(0,0);
        fragPairs = new FragmentPair[0];
        
    }
    
    
    /** get the results of step 1 - the FragmentPairs used for seeding the alignment
     * @return a FragmentPair[] array
     */
    
    public FragmentPair[] getFragmentPairs() {
        return fragPairs;
    }



    public void setFragmentPairs(FragmentPair[] fragPairs) {
        this.fragPairs = fragPairs;
    }


    /** return the alternative alignments that can be found for the two structures
     * 
     * @return AlternativeAlignment[] array
     */
    public AlternativeAlignment[] getAlignments() {
        return alts;
    }
    
    /** return the difference of distance matrix between the two structures
     * 
     * @return a Matrix
     */
    public Matrix getDistMat(){
        return distanceMatrix;
    }
    
    /** get the parameters.
     * 
     * @return the Parameters.
     */
    public StrucAligParameters getParams() {
        return params;
    }

    /** set the parameters to be used for the algorithm
     * 
     * @param params the Parameter object
     */
    public void setParams(StrucAligParameters params) {
        this.params = params;
    }
    
    
    

    public void align(Structure s1, Structure s2)
    throws StructureException {
        
        align(s1,s2,params);
    }
    
    public void align(Structure s1, Structure s2, StrucAligParameters params)
    throws StructureException {
        // step 1 convert the structures to Atom Arrays
        
        String[] atomNames = params.getUsedAtomNames();
        Atom[] ca1 = StructureTools.getAtomArray(s1,atomNames);
        Atom[] ca2 = StructureTools.getAtomArray(s2,atomNames);
        
        align(ca1,ca2,params);
    }
    
    
    /** calculate the actual protein structure superimposition.
     * 
     * 
     * @param ca1 set of Atoms of structure 1
     * @param ca2 set of Atoms of structure 2
     * @param params the parameters to use for the alignment
     * @throws StructureException
     */
    public void align(Atom[] ca1, Atom[] ca2, StrucAligParameters params) 
    throws StructureException {
        
        reset();
        
//      step 1 get all Diagonals of length X that are similar between both structures

        //System.out.println("step 1 - get fragments with similar intramolecular distances ");
        int k  = params.getDiagonalDistance();
        int k2 = params.getDiagonalDistance2();
        int fragmentLength = params.getFragmentLength();
        
        if ( ca1.length < (fragmentLength + 1) || ca2.length < (fragmentLength + 1))  {
            throw new StructureException("structure too short, can not align");
       }
        int rows = ca1.length - fragmentLength + 1;
        int cols = ca2.length - fragmentLength + 1;
        //System.out.println("rows "  + rows + " " + cols + 
          //      " ca1 l " + ca1.length + " ca2 l " + ca2.length);
        distanceMatrix = new Matrix(rows,cols,0.0);
        
        double[] dist1 = AlignTools.getDiagonalAtK(ca1, k);
        
        double[] dist2 = AlignTools.getDiagonalAtK(ca2, k);
        double[] dist3 = new double[0];
        double[] dist4 = new double[0];
        if ( k2 > 0) { 
            dist3 = AlignTools.getDiagonalAtK(ca1, k2);
            dist4 = AlignTools.getDiagonalAtK(ca2, k2);
        }
        
        double[][] utmp = new double[][] {{0,0,1}};
        //Matrix unitv = new Matrix(utmp);
        Atom unitvector = new AtomImpl();
        unitvector.setCoords(utmp[0]);
        
        
        List fragments = new ArrayList();
        
        for ( int i = 0 ; i< rows; i++){
            
            Atom[] catmp1  = AlignTools.getFragment( ca1,  i, fragmentLength);
            Atom   center1 = AlignTools.getCenter( ca1, i, fragmentLength);
            
            for ( int j = 0 ; j < cols ; j++){
                
                double rdd1 = AlignTools.rms_dk_diag(dist1,dist2,i,j,fragmentLength,k);
                double rdd2 = 0;
                if ( k2 > 0) 
                    rdd2 = AlignTools.rms_dk_diag(dist3,dist4,i,j,fragmentLength,k2);
                double rdd = rdd1 + rdd2;
                distanceMatrix.set(i,j,rdd);
               
                
                if ( rdd < params.getFragmentMiniDistance()) {
                    FragmentPair f = new FragmentPair(fragmentLength,i,j);                                       
                    //System.out.println("i " + i + " " + j );
                    try {
                       
                        Atom[] catmp2 = AlignTools.getFragment(ca2, j, fragmentLength);                        
                        Atom  center2 = AlignTools.getCenter(ca2,j,fragmentLength);
                        
                        //System.out.println("c1 : " + center1 + " c2: " + center2);
                        f.setCenter1(center1);
                        f.setCenter2(center2);
                        
                        SVDSuperimposer svd = new SVDSuperimposer(catmp1,catmp2);
                        Matrix rotmat = svd.getRotation();
                        //rotmat.print(3,3);
                        f.setRot(rotmat);
                        
                        Atom aunitv = (Atom)unitvector.clone();
                        Calc.rotate(aunitv,rotmat);
                        f.setUnitv(aunitv);
                        
                        boolean doNotAdd = false;
                        if ( params.reduceInitialFragments()) {
                            doNotAdd = FragmentJoiner.reduceFragments(fragments,f, distanceMatrix);
                            
                        }
                        if ( doNotAdd)
                            continue;
                        
                        fragments.add(f);
                        
                    } catch (StructureException e){
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }
       
        FragmentPair[] fp = (FragmentPair[]) fragments.toArray(new FragmentPair[fragments.size()]);
        setFragmentPairs(fp);
        
        //System.out.println("step 2 - join fragments");
        
        // step 2 combine them to possible models
        FragmentJoiner joiner = new FragmentJoiner();
        
       
        JointFragments[] frags;
        
        if ( ! params.isJoinPlo() ){
            frags =  joiner.approach_ap3(
                    ca1,ca2, fp, params);
        } else {
        
        // this approach by StrComPy (peter lackner):
            frags =  joiner.frag_pairwise_compat(fp,
                    params.getAngleDiff(),
                    params.getFragCompat(),
                    params.getMaxrefine());
        }
        
        //logger.info("number joint fragments:"+frags.length);
        
        //System.out.println("step 3 - refine alignments");
        List aas = new ArrayList();
        for ( int i = 0 ; i < frags.length;i++){
            JointFragments f = frags[i];
            AlternativeAlignment a = new AlternativeAlignment();
            //System.out.println(f.getRms());
            //a.setRms(f.getRms());
            a.apairs_from_idxlst(f);
            a.setAltAligNumber(i+1);
            
         
            try {
                if ( params.getMaxIter() > 0 ){
                    
                    a.refine(params,ca1,ca2);
                }
                else {
                    a.finish(params,ca1,ca2);
                        
                }
            } catch (StructureException e){
                e.printStackTrace();
            }
           // a.getRotationMatrix().print(3,3);
            aas.add(a);
        }
        
        
        // sort the alternative alignments
        Comparator comp = new AltAligComparator();
        Collections.sort(aas,comp);
        Collections.reverse(aas);
        
        alts = (AlternativeAlignment[])aas.toArray(new AlternativeAlignment[aas.size()]);     
        // do final numbering of alternative solutions
        int aanbr = 0;       
        for ( int i = 0 ; i < alts.length; i++){
            AlternativeAlignment a = alts[i];
            aanbr++;
            a.setAltAligNumber(aanbr);
            //System.out.println(aanbr);
            //a.getRotationMatrix().print(3,3);
        }
        
        //System.out.println("calc done");
    }
    
   
   
}
