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

package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.utils.*;

/**
 * <code>BioStoreFactory</code> creates <code>BioStore</code>
 * instances. These are directory and file structures which index flat
 * files according to the OBDA specification.
 *
 * @author Matthew Pocock
 * @author Keith James
 * @author Greg Cox
 */
public class BioStoreFactory {

    /**
     * <code>PRIMARY_KEY_NAME</code> is the key used to identify the
     * primary namespace in the OBDA config.dat files.
     */
    public static final String PRIMARY_KEY_NAME = "primary_namespace";

    /**
     * <code>KEYS</code> is the key used to identify the secondary
     * namespaces in the OBDA config.dat files.
     */
    public static final String KEYS = "secondary_namespaces";

    private File storeLoc;
    private String primaryKey;
    private Map keys;

    /**
     * Creates a new <code>BioStoreFactory</code>.
     */
    public BioStoreFactory() {
        keys = new SmallMap();
    }

    /**
     * <code>setStoreLocation</code> sets the parent path of the
     * store.
     *
     * @param storeLoc a <code>File</code> indicating the parent path.
     */
    public void setStoreLocation(File storeLoc) {
        this.storeLoc = storeLoc;
    }

    /**
     * <code>getStoreLocation</code> returns the parent path of the
     * store.
     *
     * @return a <code>File</code> indicating the parent path.
     */
    public File getStoreLocation() {
        return storeLoc;
    }

    /**
     * <code>setPrimaryKey</code> sets the primary identifier
     * namespace.
     *
     * @param primaryKey a <code>String</code>.
     */
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * <code>getPrimaryKey</code> returns the primary identifier
     * namespace.
     *
     * @return a <code>String</code>.
     */
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * <code>addKey</code> adds a new secondary identifier namespace.
     *
     * @param keyName a <code>String</code>.
     * @param length an <code>int</code> indicating the byte length of
     * the secondary key records.
     */
    public void addKey(String keyName, int length) {
        keys.put(keyName, new Integer(length));
    }

    /**
     * <code>removeSecondaryKey</code> removes the specified
     * secondary.
     *
     * @param keyName a <code>String</code>.
     */
    public void removeSecondaryKey(String keyName) {
        keys.remove(keyName);
    }

    /**
     * <code>createBioStore</code> creates a <code>BioStore</code>
     * reflecting the current state of the factory and returns a
     * reference to it.
     *
     * @return a <code>BioStore</code>.
     *
     * @exception BioException if an error occurs.
     */
    public BioStore createBioStore()
        throws BioException {
        try {
            if (storeLoc.exists()) {
                throw new BioException("Store location already exists. Delete first: " + storeLoc);
            }

            if (!keys.containsKey(primaryKey)) {
                throw new BioException("Primary key is not listed as a key: " + primaryKey);
            }

            storeLoc.mkdirs();
            ConfigFile ann = new ConfigFile(makeConfigFile(storeLoc));
            ann.setProperty("index", "flat/1");

            // primary key data
            ann.setProperty(PRIMARY_KEY_NAME, primaryKey);

            StringBuffer keyList = new StringBuffer();

            // other keys data
            for (Iterator ki = keys.keySet().iterator(); ki.hasNext(); ) {
                String key = (String) ki.next();
                int length = ((Integer) keys.get(key)).intValue();

                if (key.equals(primaryKey)) {
                    new PrimaryIDList(makePrimaryKeyFile(storeLoc, key),
                                      calculatePrimRecLen(length),
                                      null);
                } else {
                    new SecondaryFileAsList(makeSecondaryFile(storeLoc, key),
                                            calculateSecRecLen(length, primaryKey, keys));

                    if (keyList.length() != 0) {
                        keyList.append("\t");
                    }
                    keyList.append(key);
                }
            }

            ann.setProperty(KEYS, keyList.substring(0));
            ann.commit();

            BioStore bStore = new BioStore(storeLoc, true, true);

            return bStore;
        } catch (ChangeVetoException cve) {
            throw new BioError(cve, "Assertion Failure: Can't update annotation");
        } catch (IOException ioe) {
            throw new BioError(ioe, "Could not initialize store");
        }
    }

    /**
     * <code>makeConfigFile</code> returns a file which represents an
     * OBDA "config.dat" on the specified parent path.
     *
     * @param storeLoc a <code>File</code> indicating the parent path.
     *
     * @return a <code>File</code> representing "config.dat".
     *
     * @exception IOException if an error occurs.
     */
    public static File makeConfigFile(File storeLoc)
        throws IOException {
        return new File(storeLoc, "config.dat");
    }

    /**
     * <code>makePrimaryKeyFile</code> returns a file which represents
     * an OBDA "key_&lt;primary namespace&gt;.key" primary key file on the
     * specified parent path.
     *
     * @param storeLoc a <code>File</code> indicating the parent path.
     * @param key a <code>String</code> primary key namespace.
     *
     * @return a <code>File</code> representing a "key_&lt;primary
     * namespace&gt;.key".
     *
     * @exception IOException if an error occurs.
     */
    public static File makePrimaryKeyFile(File storeLoc, String key)
        throws IOException {
        return new File(storeLoc, "key_" + key + ".dat");
    }

    /**
     * <code>makeSecondaryFile</code> returns a file which represents
     * an OBDA "id_&lt;secondary namespace&gt;.index" secondary key file on
     * the specified
     *
     * @param storeLoc a <code>File</code> indicating the parent path.
     * @param key a <code>String</code> secondary key namespace.
     *
     * @return a <code>File</code> representing an "id_&lt;secondary
     * namespace&gt;.index" file.
     *
     * @exception IOException if an error occurs
     */
    public static File makeSecondaryFile(File storeLoc, String key)
        throws IOException {
        return new File(storeLoc, "id_" + key + ".index");
    }

    /**
     * <code>calculatePrimRecLen</code> calculates the byte length of
     * primary namespace records.
     *
     * @param idLen an <code>int</code> the number of bytes required
     * to hold the primary namespace ID.
     *
     * @return an <code>int</code> record length in bytes.
     */
    public static int calculatePrimRecLen(int idLen) {
        return
            idLen +                                     // space for ids
            "\t".length() +
            4 +                                         // file id
            "\t".length() +
            String.valueOf(Long.MAX_VALUE).length() +   // offset
            "\t".length() +
            String.valueOf(Integer.MAX_VALUE).length(); // length
    }

    /**
     * <code>calculateSecRecLen</code> calculates the byte length of
     * secondary namespace records.
     *
     * @param idLen an <code>int</code> the number of bytes required
     * to hold the secondary namespace ID.
     *
     * @param primaryKey a <code>String</code> the primary namespace
     * ID.
     * @param keys a <code>Map</code> of secondary keys to their byte
     * lengths.
     *
     * @return an <code>int</code> record length in bytes.
     */
    public static int calculateSecRecLen(int idLen, String primaryKey, Map keys) {
        int primLength = ((Integer) keys.get(primaryKey)).intValue();
        return
            idLen +
            "\t".length() +
            primLength;
    }
}
