package org.biojava.utils.xml;

import java.util.*;
import java.io.*;

/**
 * Object which knows how to represent itself as an XML element.
 *
 * @author Thomas Down
 */

public interface XMLWritable {
    public void writeXML(XMLWriter writer)
        throws IOException;
}
