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

package org.biojava.bio.search;

/**
 * Class for implementing tests with BlastLikeSearchFilter
 * objects.  Several precanned tests are included.
 * @author David Huen
 */
public interface FilterTest
{
    /**
     * @return returns true if test is successful.
     */
    public boolean accept(Object value);

    public static class Equals
        implements FilterTest
    {
        private Object value;

        public Equals(Object value) { this.value = value; }

        public boolean accept(Object value)
        {
            return this.value.equals(value);
        }
    }

    /**
     * Tests that the value associated with the specified
     * key is less than the specified threshold.  The test
     * assumes that value is a String representing a real
     * number.  If not, the test will return null.
     */
    public static class LessThan
        implements FilterTest
    {
        private double threshold;

        public LessThan(double threshold) { this. threshold = threshold; }

        public boolean accept(Object value)
        {
            return (value instanceof String) && (Double.parseDouble((String) value) < threshold);
        }
    }
}

