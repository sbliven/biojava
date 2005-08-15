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
public interface PositionResolver {
    
    public int getMin(Position start);
    public int getMax(Position end);
    
    public static class MaximalResolver implements PositionResolver {
        // maximal range is from min(s) to max(e)
        public int getMin(Position s) {
            if (s.hasFuzzyStart()) return Integer.MIN_VALUE;
            else return s.getStart();
        }
        public int getMax(Position e) {
            if (e.hasFuzzyEnd()) return Integer.MAX_VALUE;
            else return e.getEnd();
        }
    }
    
    public static class MinimalResolver implements PositionResolver {
        // minimal range is from max(s) to min(e)
        public int getMin(Position s) {
            return s.getEnd();
        }
        public int getMax(Position e) {
            return e.getStart();
        }
    }
    
    public static class AverageResolver implements PositionResolver {
        // average range is from avg(min(s),max(s))) to avg(min(e),max(e))
        public int getMin(Position s) {
            int min;
            if (s.hasFuzzyStart()) min = Integer.MIN_VALUE;
            else min = s.getStart();
            int max = s.getEnd();
            return (min+max) / 2;
        }
        public int getMax(Position e) {
            int max;
            if (e.hasFuzzyEnd()) max = Integer.MAX_VALUE;
            else max = e.getEnd();
            int min = e.getStart();
            return (min+max) / 2;
        }
    }
}
