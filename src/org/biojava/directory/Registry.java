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

package org.biojava.directory;

import java.util.Iterator;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.db.SequenceDBLite;
import org.biojava.utils.Services;

/**
 * <p><code>SequenceDBFactory</code> is a factory which gets
 * implementations of the BioJava <code>SequenceDB</code>
 * interface.</p>
 *
 * @author Brian Gilman
 * @author Thomas Down
 * @author Keith James
 *
 * @version $Revision$
 */
public class Registry {

    /**
     * Registry Configuration instance
     */
    private RegistryConfiguration regConfig = null;

    /**
     * Creates a new OBDA <code>Registry</code> with the specified
     * configuration.
     *
     * @param regConfig a <code>RegistryConfiguration</code>.
     */
    public Registry(RegistryConfiguration regConfig) {
        this.regConfig = regConfig;
    }

    /**
     * <code>getDatabase</code> retrieves a database instance known by
     * a name <code>String</code>.
     *
     * @param dbName a <code>String</code> database name.
     *
     * @return a <code>SequenceDBLite</code>.
     *
     * @exception RegistryException if the registry does not contain a
     * configuration for the specified name.
     * @exception BioException if the provider fails.
     */
    public SequenceDBLite getDatabase(String dbName)
        throws RegistryException, BioException {

        Map dbConfig = null;
        String providerName = "";

        dbConfig =
            (Map) getRegistryConfiguration().getConfiguration().get(dbName);

        if (dbConfig == null) {
            throw new RegistryException("Couldn't find a configuration"
                                        + " for database: "
                                        + dbName);
        }

        try {
            providerName = (String) dbConfig.get("protocol");
        } catch (Exception e) {
            throw new RegistryException("File for configuration "
                                        + " cannot be found: "
                                        + e.toString());
        }

        return getProvider(providerName).getSequenceDB(dbConfig);
    }

    private SequenceDBProvider getProvider(String providerName)
        throws RegistryException {
        try {
            ClassLoader loader = getClass().getClassLoader();
            Iterator implNames =
                Services.getImplementationNames(SequenceDBProvider.class, loader).iterator();

            while (implNames.hasNext()) {
              String className = (String) implNames.next();
              try {
                Class clazz = loader.loadClass(className);
                SequenceDBProvider seqDB =
                    (SequenceDBProvider) clazz.newInstance();
                if (seqDB.getName().equals(providerName)) {
                    return seqDB;
                }
              } catch (ClassNotFoundException ce) {
                throw new RegistryException(
                  ce,
                  "Could not find class: " + className +
                  " for service provider " + providerName
                );
              }
            }

            throw new ProviderNotFoundException("No such provider exists: "
                                                + providerName);
        } catch (Exception e) {
            throw new RegistryException(e, "Error accessing"
                                        + " SequenceDBProvider services");
        }
    }

    /**
     * <code>getRegistryConfiguration</code> returns the configuration
     * of the registry.
     *
     * @return a <code>RegistryConfiguration</code>.
     */
    public RegistryConfiguration getRegistryConfiguration() {
        return this.regConfig;
    }
}
