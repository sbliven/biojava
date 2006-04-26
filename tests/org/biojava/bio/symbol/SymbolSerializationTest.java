package org.biojava.bio.symbol;

import junit.framework.TestCase;
import org.biojava.bio.seq.DNATools;

import java.io.*;

/**
 * Tests that serilization works as advertised.
 *
 * @author Thomas Down
 * @author Mark Schreiber
 * @since 1.3
 */

public class SymbolSerializationTest extends TestCase {
    public SymbolSerializationTest(String name){
        super(name);
    }

    private void doSymbolTest(Symbol s)
        throws Exception
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream((os));
        oos.writeObject(s);
        oos.flush();
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(os.toByteArray()));
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
