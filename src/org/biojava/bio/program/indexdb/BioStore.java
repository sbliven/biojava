package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.utils.*;

public class BioStore implements IndexStore {
  static Comparator STRING_CASE_SENSITIVE_ORDER = new Comparator() {
    public int compare(Object a, Object b) {
      return ((String) a).compareTo(b);
    }
  };
  
  public int size() {
    return primaryList.size();
  }
  
  private ConfigFile metaData;
  private File location;
  private PrimaryIDList primaryList;
  private String primaryKey;
  private Map idToList;
  private File[] fileIDToFile;
  private int fileCount;

  BioStore(File location)
  throws IOException, BioException {
    this.location = location;
    metaData = new ConfigFile(BioStoreFactory.makeConfigFile(location));
    idToList = new SmallMap();
    
    primaryKey = (String) metaData.getProperty(BioStoreFactory.PRIMARY_KEY_NAME);
    String keyList = (String) metaData.getProperty(BioStoreFactory.KEYS);

    File plFile = BioStoreFactory.makePrimaryKeyFile(location, primaryKey);
    primaryList = new PrimaryIDList(plFile, this);
    
    StringTokenizer sTok = new StringTokenizer(keyList, "\t");
    while(sTok.hasMoreTokens()) {
      String k = sTok.nextToken();
      
      File file = BioStoreFactory.makeSecondaryFile(location, k);
      idToList.put(k, new SecondaryFileAsList(file));
    }
    
    readFileIDs();
  }
  
  private void readFileIDs()
  throws
    IOException,
    BioException
  {
    fileIDToFile = new File[5];
    fileCount = 0;
    
    for(Iterator i = metaData.keys().iterator(); i.hasNext(); ) {
      String key = (String) i.next();
      if(key.startsWith("fieldid_")) {
        int indx = Integer.parseInt(key.substring("fileid_".length()));
        String fileLine = (String) metaData.getProperty(key);
        int tab = fileLine.indexOf("\t");
        File file = new File(fileLine.substring(0, tab));
        long length = Long.parseLong(fileLine.substring(tab+1));
        
        if(file.length() != length) {
          throw new BioException("File changed length: " + file);
        }
        
        if(indx >= fileCount) {
          // beyond end
          
          if(indx >= fileIDToFile.length) {
            // beyond array end
            File[] tmp = new File[indx];
            System.arraycopy(fileIDToFile, 0, tmp, 0, fileIDToFile.length);
          }
          
          fileCount = indx;
        }
        fileIDToFile[indx] = file;
      }
    }
  }
  
  private void writeFileIDs()
  throws BioException, ChangeVetoException {
    for(int i = 0; i < fileCount; i++) {
      File file = fileIDToFile[i];
      long length = file.length();
      
      metaData.setProperty("fileid_" + i, file + "\t" + length);
    }
  }
  
  File getFileForID(int fileId) {
    return fileIDToFile[fileId];
  }
  
  int getIDForFile(File file)
  throws IOException {
    // scan list
    for(int i = 0; i < fileCount; i++) {
      if(file.equals(fileIDToFile[i])) {
        return i;
      }
    }
    
    file = file.getCanonicalFile();
    
    for(int i = 0; i < fileCount; i++) {
      if(file.equals(fileIDToFile[i])) {
        return i;
      }
    }

    // extend fileIDToFile array
    if(fileCount >= fileIDToFile.length) {
      File[] tmp = new File[fileIDToFile.length + 4]; // 4 is magic number
      System.arraycopy(fileIDToFile, 0, tmp, 0, fileCount);
      fileIDToFile = tmp;
    }
    
    // add the unseen file to the list
    fileIDToFile[fileCount] = file;
    return fileCount++;
  }
  
  public Annotation getMetaData() {
    return metaData;
  }
  
  public Record get(String id) {
    return (Record) primaryList.search(id);
  }
  
  public List get(String id, String namespace)
  throws BioException {
    List hits = new ArrayList();
    if(namespace.equals(primaryKey)) {
      hits.add(primaryList.search(id));
    } else {
      SecondaryFileAsList secList = (SecondaryFileAsList) idToList.get(namespace);
      List kpList = secList.searchAll(id);
      for(Iterator i = kpList.iterator(); i.hasNext(); ) {
        KeyPair keyPair = (KeyPair) secList.search(id);
        kpList.add(primaryList.search(keyPair.getSecondary()));
      }
    }
    
    return hits;
  }
  
  public void writeRecord(
    File file,
    long offset,
    int length,
    String id,
    Map secIDs
  ) {
    primaryList.add(new Record.Impl(id, file, offset, length));
    if(!secIDs.isEmpty()) {
      for(Iterator mei = secIDs.entrySet().iterator(); mei.hasNext(); ) {
        Map.Entry me = (Map.Entry) mei.next();
        String sid = (String) me.getKey();
        SecondaryFileAsList sfl = (SecondaryFileAsList) idToList.get(sid);
        sfl.add(new KeyPair.Impl(sid, id));
      }
    }
  }
  
  public void commit()
  throws BioException {
    primaryList.commit();
    for(Iterator i = idToList.values().iterator(); i.hasNext(); ) {
      FileAsList fal = (FileAsList) i.next();
      fal.commit();
    }

    try {
      writeFileIDs();
    } catch (ChangeVetoException cve) {
      throw new BioException(cve);
    }
    
    metaData.commit();
  }
}
