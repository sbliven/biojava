/**
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
package org.biojava.bio.seq.io;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

import java.util.*;
import java.io.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * JUnit test for MSFAlignmentFormat
 * @author Thomas Down
 * @since 1.4
 */
 
public class MSFAlignmentFormatTest extends TestCase
{

    public MSFAlignmentFormatTest(String name)
    {
        super(name);
    }

    public void testReadDNAAlignment()
    {
        // get access to the test file
        InputStream inputS = this.getClass().getResourceAsStream("/files/dna.msf");
        assertNotNull(inputS);

        Alignment alignment = new MSFAlignmentFormat().read(new BufferedReader(new InputStreamReader(inputS)));
        assertNotNull(alignment);
        
        assertEquals(alignment.length(), 120);
        assertEquals(alignment.getAlphabet().getAlphabets().get(0), DNATools.getDNA());
    }
}
