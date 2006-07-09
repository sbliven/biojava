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

public class AligMatEl {

   
   
        int value;
        IndexPair track;
        int contig;
        
        public AligMatEl(){
            track = new IndexPair((short)-1,(short)-1);
            value = -1;
            contig = -1;
        }

        public int getContig() {
            return contig;
        }
        public void setContig(int contig) {
            this.contig = contig;
        }
        public IndexPair getTrack() {
            return track;
        }
        public void setTrack(IndexPair track) {
            this.track = track;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
        
        public String toString(){
            String ret = "AligMatEl val:" + value + " contig:" + contig + 
            " trackrow:" + track.getRow() + " trackcol:" + track.getCol();
            return ret;
        }
        
    }

    


