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

/*
 * Position.java
 *
 * Created on July 28, 2005, 5:29 PM
 */
package org.biojavax.bio.seq;


/**
 * Holds enough info about positions to keep BioSQL happy if needs be.
 *
 * @author Richard Holland
 */
public interface Position {
    
    public boolean hasFuzzyStart();
    public boolean hasFuzzyEnd();
    public int getStart();
    public int getEnd();
    public Position translate(int distance);
    public String getType();
    
    public static abstract class AbstractPosition implements Position {
        protected boolean fs;
        protected boolean fe;
        public AbstractPosition(boolean fs, boolean fe) {
            this.fs = fs;
            this.fe = fe;
        }
        public boolean hasFuzzyStart() { return this.fs; }
        public boolean hasFuzzyEnd() { return this.fe; }
        public String getType() { return null; }
    }
    
    public static class ExactPosition extends AbstractPosition {
        private int p;
        public ExactPosition(boolean fs,boolean fe,int p) {
            super(fs,fe);
            this.p = p;
        }
        public int getStart() { return this.p; }
        public int getEnd()  { return this.p; }
        public Position translate(int distance) {
            return new ExactPosition(this.fs,this.fe,this.p+distance);
        }
        public String toString() { 
            return (this.fs?"<":"")+this.p+(this.fe?">":"");
        }
    }
    
    public static class RangePosition extends AbstractPosition {
        protected int s;
        protected int e;
        public RangePosition(boolean fs,boolean fe,int s,int e) {
            super(fs,fe);
            this.s = s;
            this.e = e;
        }
        public int getStart() { return this.s; }
        public int getEnd()  { return this.e; }
        public Position translate(int distance) {
            return new RangePosition(this.fs,this.fe,this.s+distance,this.e+distance);
        }
        public String getType() { return "."; }
        public String toString() { 
            return "("+(this.fs?"<":"")+this.s+"."+this.e+(this.fe?">":"")+")";
        }
    }
    
    public static class BetweenPosition extends RangePosition {
        public BetweenPosition(boolean fs,boolean fe,int s,int e) {
            super(fs,fe,s,e);
        }
        public Position translate(int distance) {
            return new BetweenPosition(this.fs,this.fe,this.s+distance,this.e+distance);
        }
        public String getType() { return "^"; }
        public String toString() { 
            return "("+(this.fs?"<":"")+this.s+"^"+this.e+(this.fe?">":"")+")";
        }
    }
}
