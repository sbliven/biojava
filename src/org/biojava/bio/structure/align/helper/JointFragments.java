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
 * Created on Mar 1, 2006
 *
 */
package org.biojava.bio.structure.align.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JointFragments {

    
        double rms;
        List idxlist;
        public JointFragments(){
            idxlist = new ArrayList();
            rms = 999;
        }
        
        public void setIdxlist(List idxs) {
            Iterator iter = idxs.iterator();
            while (iter.hasNext()){
                int[] e = (int[])iter.next();
                idxlist.add(e);
            }
        }
        
        
        
        public double getRms() {
            return rms;
        }

        public void setRms(double rms) {
            this.rms = rms;
        }

        public List getIdxlist(){
            return idxlist;
        }
        public void add(int p1, int p2,int start,int end){
            //System.out.println("JointFragments add " +p1 + " " + p2 + " " + start + " " + end);
            for ( int k = start;k< end ; k++){
                //e = (f[0]+k,f[1]+k)
                //e = (f[0]+k,f[1]+k);
                int[] e = new int[] {p1+k,p2+k};
                
                // check if already known ...
                Iterator iter = idxlist.iterator();
                while (iter.hasNext()){
                    int[] kno = (int[])iter.next();
                    if ((kno[0] == e[0]) && ( kno[1] == e[1])){
                        System.err.println("already known index pair!! how is this possible?" + e[0] + " " + e[1]);
                    }
                }
                idxlist.add(e);
            }
        }
        
        public String toString(){
            String s = "Joint Fragment idxlist len: " +idxlist.size();
            return s;
        }
    
    
}
