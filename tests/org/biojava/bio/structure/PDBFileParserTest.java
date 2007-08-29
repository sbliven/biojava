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
 * Created on Jun 10, 2007
 *
 */
package org.biojava.bio.structure;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

import org.biojava.bio.structure.io.PDBFileParser;

import junit.framework.TestCase;

public class PDBFileParserTest extends TestCase {

    PDBFileParser parser;
    
    protected void setUp(){
        parser = new PDBFileParser();
    }
    
   
    
    /** parse the remark lines and return the resolution
     * 
     * @param fakeFile
     * @return the resolution as a Float or null if no resolution found
     * @throws Exception
     */
    private Object testREMARKParsing(String fakeFile) throws Exception{
        BufferedReader br = new BufferedReader(new StringReader(fakeFile));
        
        Object resolution = null;
        
        Structure s = parser.parsePDBFile(br);
        Map<String, Object> m = s.getHeader();
        resolution =  m.get("resolution");

        return resolution;
    }
    
    public void testREMARK200() {

        // test that the resolution is only read from REMARK 2 lines
        String w1 = "REMARK 200  RESOLUTION RANGE HIGH      (A) : 1.20\n"+
        "REMARK 200  RESOLUTION RANGE LOW       (A) : 20.00"+
        "REMARK   200 RESOLUTION9.9  ANGSTROMS."; // this line could give wrong resolution info, but it should not be parsed;
        boolean parsingOK = true;
        String errorMsg   = "";
        
        try {
            Object resolution = testREMARKParsing(w1);
            assertEquals(resolution,null);
        } catch (Exception e){
            parsingOK = false;
            //e.printStackTrace();
            errorMsg = e.getMessage();
        }
        
       
        assertEquals("parsing failed with error " + errorMsg, parsingOK, true);
    }
    
    public void testREMARK2(){
        
        String w2 = "REMARK   2 \n"+
        "REMARK   2 RESOLUTION. 1.2  ANGSTROMS."; // the correct line
        
        boolean parsingOK = true;
        String errorMsg   = "";
        try {
            Object resolution = testREMARKParsing(w2);
            assertEquals(resolution,new Float(1.2));
        } catch (Exception e){
            parsingOK = false;
            //e.printStackTrace();
            errorMsg = e.getMessage();
        }
        
       
        assertEquals("parsing failed with error " + errorMsg, parsingOK, true);
    }

}
