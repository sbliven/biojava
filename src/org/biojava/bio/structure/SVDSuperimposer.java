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
 * Created on Dec 4, 2005
 *
 */
package org.biojava.bio.structure;

import org.biojava.bio.structure.jama.Matrix;
import org.biojava.bio.structure.jama.SingularValueDecomposition;


/** a class that calculates the superimposition between two sets of atoms
 * heavily influenced by the biopython SVDSuperimposer class...
 * 
 * @author Andreas Prlic
 *
 */
public class SVDSuperimposer {

    Matrix rot;
    Matrix tran;
    
    Matrix centroidA;
    Matrix centroidB;

    public SVDSuperimposer(Atom[] atomSet1,Atom[]atomSet2)
    throws StructureException{
        
        if ( atomSet1.length != atomSet2.length ){
            throw new StructureException("The two atom sets are not of same length!");
        }
        
        Atom cena = Calc.getCentroid(atomSet1);
        Atom cenb = Calc.getCentroid(atomSet2);
        
        double[][] centAcoords = new double[][]{{cena.getX(),cena.getY(),cena.getZ()}};
        centroidA = new Matrix(centAcoords);
        
        double[][] centBcoords = new double[][]{{cenb.getX(),cenb.getY(),cenb.getZ()}};
        centroidB = new Matrix(centBcoords);
        
        
        //      center at centroid
        
        Atom[] ats1 = Calc.centerAtoms(atomSet1);
        Atom[] ats2 = Calc.centerAtoms(atomSet2);
        
        double[][] coordSet1 = new double[atomSet1.length][3];
        double[][] coordSet2 = new double[atomSet2.length][3];
        
        // copy the atoms into the internal coords;
        for (int i =0 ; i< atomSet1.length;i++) {
            coordSet1[i] = ats1[i].getCoords();
            coordSet2[i] = ats2[i].getCoords();
        }
        
                
        calculate(coordSet1,coordSet2);
    }
    
    /** do the a ctual calculation
     * 
     * @param coordSet1
     * @param coordSet2
     */
    private void calculate(double[][] coordSet1, double[][]coordSet2){
        // now this is the bridge to the Jama package:
        Matrix a = new Matrix(coordSet1);
        Matrix b = new Matrix(coordSet2);
        
        Matrix b_trans = b.transpose();
                
        Matrix corr = b_trans.times(a);
        
        SingularValueDecomposition svd = corr.svd();
        
        // A = U*S*V'.


        Matrix u = svd.getU();
        Matrix v =svd.getV();
        
        
        Matrix u_transp = u.transpose();
        Matrix v_transp = v.transpose();
        
        rot = v_transp.times(u_transp);
        rot = rot.transpose();
      
        // check if we have found a reflection
        double det = rot.det();
        //System.out.println(det);
        
        if (det<0) {
            
            v_transp.set(2,0,(0 - v_transp.get(2,0)));
            v_transp.set(2,1,(0 - v_transp.get(2,1)));
            v_transp.set(2,2,(0 - v_transp.get(2,2)));
            
            rot = v_transp.times(u_transp).transpose();
                    
        }
       
        tran = centroidA.minus(centroidB.times(rot));
       
       
    }
    
    public Matrix getRotation(){
        return rot;
    }
    
    public Atom getTranslation(){
       // printMatrix(tran);
        Atom a = new AtomImpl();
        a.setX(tran.get(0,0));
        a.setY(tran.get(0,1));
        a.setZ(tran.get(0,2));
        return a;
    }
    
    public void printMatrix(Matrix m){
        for (int i = 0 ; i < m.getRowDimension(); i++){
            for (int j = 0 ; j< m.getColumnDimension(); j++){
                System.out.print("\t" + m.get(i,j) + " ");
            }
            System.out.println("");
        }
    }
    
}
