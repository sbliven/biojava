package org.biojava.bio.program.xff;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;

public class PropertyWriter extends BasicXFFHelper {
    public void writeDetails(XMLWriter xw, Feature f)
    throws IOException {
        Annotation a = f.getAnnotation();
        for (Iterator ai = a.keys().iterator(); ai.hasNext(); ) {
            Object key =  ai.next();
            if (! (key instanceof String))
                continue;
            Object value = a.getProperty(key);
            if (! (value instanceof String)) {
                continue;
            }
            
            xw.openTag("biojava:prop");
            xw.attribute("key", (String) key);
            xw.print((String) value);
            xw.closeTag("biojava:prop");
        }
    }
}
