package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;

import junit.framework.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Tests that serilization works as advertised.
 *
 * @author Thomas Down
 * @since 1.3
 */
 
public class SymbolSerializationTest extends TestCase {
    public SymbolSerializationTest(String name){
        super(name);
    }
    
    private void doSymbolTest(Symbol s)
        throws Exception
    {
        File f = File.createTempFile("SymbolSerializationTest", ".jo");
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
        oos.writeObject(s);
        oos.flush();
        oos.close();
        
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
        Symbol s2 = (Symbol) ois.readObject();
        ois.close();
        
        assertTrue(s == s2);
    }
    
    public void testAtomic()
        throws Exception
    {
        doSymbolTest(DNATools.t());
    }
    
    public void testAmbiguous()
        throws Exception
    {
        doSymbolTest(DNATools.n());
    }
}
