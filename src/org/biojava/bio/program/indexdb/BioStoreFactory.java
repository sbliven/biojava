package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.utils.*;

public class BioStoreFactory {
  public static final String PRIMARY_KEY_NAME = "primary_namespace";
  public static final String KEYS = "secondary_namespaces";
  
  private File storeLoc;
  private String primaryKey;
  private Map keys;
  
  public BioStoreFactory() {
    keys = new SmallMap();
  }
  
  public void setStoreLocation(File storeLoc) {
    this.storeLoc = storeLoc;
  }
  
  public File getStoreLocation() {
    return storeLoc;
  }
  
  public void setPrimaryKey(String primaryKey) {
    this.primaryKey = primaryKey;
  }
  
  public String getPrimaryKey() {
    return primaryKey;
  }
  
  public void addKey(String keyName, int length) {
    keys.put(keyName, new Integer(length));
  }
  
  public void removeSecondaryKey(String keyName) {
    keys.remove(keyName);
  }

  public BioStore createBioStore()
  throws BioException {
    try {
    if(storeLoc.exists()) {
      throw new BioException("Store location already exists. Delete first: " + storeLoc);
    }

    if(!keys.containsKey(primaryKey)) {
      throw new BioException("Primary key is not listed as a key: " + primaryKey);
    }
    
    storeLoc.mkdirs();
    ConfigFile ann = new ConfigFile(makeConfigFile(storeLoc));
    ann.setProperty("index", "flat/1");
    
    // primary key data
    ann.setProperty(PRIMARY_KEY_NAME, primaryKey);
    
    StringBuffer keyList = new StringBuffer();
    
    // other keys data
    for(Iterator ki = keys.keySet().iterator(); ki.hasNext(); ) {
      String key = (String) ki.next();
      int length = ((Integer) keys.get(key)).intValue();
      
      if(key.equals(primaryKey)) {
        new PrimaryIDList(
          makePrimaryKeyFile(storeLoc, key),
          calculatePrimRecLen(length),
          null
        );
      } else {
        new SecondaryFileAsList(
          makeSecondaryFile(storeLoc, key),
          calculateSecRecLen(length, primaryKey)
        );
      
        if(keyList.length() != 0) {
          keyList.append("\t");
        }
        keyList.append(key);
      }
    }
    ann.setProperty(KEYS, keyList.toString());
    
    ann.commit();
    
    BioStore bStore = new BioStore(storeLoc);
    
    return bStore;
    } catch (ChangeVetoException cve) {
      throw new BioError(cve, "Assertion Failure: Can't update annotation");
    } catch (IOException ioe) {
      throw new BioError(ioe, "Could not initialize store");
    }
  }
  
  static File makeConfigFile(File storeLoc)
  throws IOException {
    return new File(storeLoc, "config.dat");
  }
  
  static File makePrimaryKeyFile(File storeLoc, String key)
  throws IOException {
    return new File(storeLoc, "key_" + key + ".dat");
  }
  
  static File makeSecondaryFile(File storeLoc, String key)
  throws IOException {
    return new File(storeLoc, "id_" + key + ".index");
  }
  
  static int calculatePrimRecLen(int idLen) {
    return
      idLen +                                     // space for ids
      "\t".length() +
      4 +                                         // file id
      "\t".length() +
      String.valueOf(Long.MAX_VALUE).length() +   // offset
      "\t".length() +
      String.valueOf(Integer.MAX_VALUE).length(); // length
  }
  
  static int calculateSecRecLen(int idLen, String primaryKey) {
    return
      idLen +
      "\t".length() +
      primaryKey.length();
  }
}
