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
 * inspired by the biopython SVDSuperimposer class...
 * 
 *
 * example usage:
 * <pre>
 * try{
           
            // get some arbitrary amino acids from somewhere
            String filename   =  "/Users/ap3/WORK/PDB/5pti.pdb" ;
            
            PDBFileReader pdbreader = new PDBFileReader();
            Structure struc = pdbreader.getStructure(filename);
            Group g1 = (Group)struc.getChain(0).getGroup(21).clone();
            Group g2 = (Group)struc.getChain(0).getGroup(53).clone();
           
            if ( g1.getPDBName().equals("GLY")){
                if ( g1 instanceof AminoAcid){ 
                    Atom cb = Calc.createVirtualCBAtom((AminoAcid)g1);
                    g1.addAtom(cb);
                }
            }
            
            if ( g2.getPDBName().equals("GLY")){
                if ( g2 instanceof AminoAcid){ 
                    Atom cb = Calc.createVirtualCBAtom((AminoAcid)g2);
                    g2.addAtom(cb);
                }
            }
            
            Structure struc2 = new StructureImpl((Group)g2.clone());
            
            System.out.println(g1);
            System.out.println(g2);
                    
            
            Atom[] atoms1 = new Atom[3];
            Atom[] atoms2 = new Atom[3];
            
            atoms1[0] = g1.getAtom("N");
            atoms1[1] = g1.getAtom("CA");
            atoms1[2] = g1.getAtom("CB");
            
            
            atoms2[0] = g2.getAtom("N");
            atoms2[1] = g2.getAtom("CA");
            atoms2[2] = g2.getAtom("CB");
           
                       
            SVDSuperimposer svds = new SVDSuperimposer(atoms1,atoms2);
            
                     
            Matrix rotMatrix = svds.getRotation();
            Atom tranMatrix = svds.getTranslation();
            
                        
            // now we have all the info to perform the rotations ...
            
            Calc.rotate(struc2,rotMatrix);

            //          shift structure 2 onto structure one ...
            Calc.shift(struc2,tranMatrix);            
           
            //
            // write the whole thing to a file to view in a viewer
              
            String outputfile = "/Users/ap3/WORK/PDB/rotated.pdb";
            
            FileOutputStream out= new FileOutputStream(outputfile); 
            PrintStream p =  new PrintStream( out );
            
            Structure newstruc = new StructureImpl();
            
            Chain c1 = new ChainImpl();
            c1.setName("A");
            c1.addGroup(g1);
            newstruc.addChain(c1);
            
            Chain c2 = struc2.getChain(0);
            c2.setName("B");
            newstruc.addChain(c2);
            
            // show where the group was originally ...
            Chain c3 = new ChainImpl();
            c3.setName("C");
            //c3.addGroup(g1);
            c3.addGroup(g2);
            
            newstruc.addChain(c3);
            p.println(newstruc.toPDB());
            
            p.close();
            
            System.out.println("wrote to file " + outputfile);
            
        } catch (Exception e){
            e.printStackTrace();
        }
        </pre>
 *
 *
 * @author Andreas Prlic
 * @since 1.5
 * @version %I% %G%

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
        
        double[][] coordSet1 = new double[ats1.length][3];
        double[][] coordSet2 = new double[ats2.length][3];
        
        // copy the atoms into the internal coords;
        for (int i =0 ; i< ats1.length;i++) {
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
        
        
//      # correlation matrix
      
        Matrix b_trans = b.transpose();               
        Matrix corr = b_trans.times(a);
       
        
        SingularValueDecomposition svd = corr.svd();
        
        // A = U*S*V'.
        
          
        Matrix u = svd.getU();
        // v is alreaady transposed ! difference to numermic python ...
        Matrix vt =svd.getV();
        
        Matrix vt_orig = (Matrix) vt.clone();
        Matrix u_transp = u.transpose();
      
        Matrix rot_nottrans = vt.times(u_transp);
        rot = rot_nottrans.transpose();
      
        // check if we have found a reflection
               
        //printMatrix(rot);
        
        double det = rot.det();
        //System.out.println(det);
       
         if (det<0) {
            vt = vt_orig.transpose();
            vt.set(2,0,(0 - vt.get(2,0)));
            vt.set(2,1,(0 - vt.get(2,1)));
            vt.set(2,2,(0 - vt.get(2,2)));
            
            Matrix nv_transp = vt.transpose();
            rot_nottrans = nv_transp.times(u_transp);
            rot = rot_nottrans.transpose();
            
        }
     
        Matrix cb_tmp = centroidB.times(rot);
        tran = centroidA.minus(cb_tmp);
      
       
    }
    
    public Matrix getRotation(){
        return rot;
    }
    
    public Atom getTranslation(){
      
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
