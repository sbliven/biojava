package org.biojava.directory;

import java.util.*;
import javax.naming.*;
import javax.naming.spi.*;

/**
 * Initialize Bio-directory system
 *
 * @author Thomas Down
 */

public class BioContextFactory implements InitialContextFactory {
    public Context getInitialContext(Hashtable env) {
	return new BioContext(env);
    }
}
