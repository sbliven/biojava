package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.utils.io.*;

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
  private String primaryKey;
  private Map idToList;
  private RAF[] fileIDToRAF;
  private PrimaryIDList primaryList;
  private int fileCount;

  public BioStore(File location)
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
    
    //System.out.println("Primary key: " + plFile);
    
    readFileIDs();
  }
  
  private void readFileIDs()
  throws
    IOException,
    BioException
  {
    fileIDToRAF = new RAF[5];
    fileCount = 0;
    
    for(Iterator i = metaData.keys().iterator(); i.hasNext(); ) {
      String key = (String) i.next();
      if(key.startsWith("fileid_")) {
        int indx = Integer.parseInt(key.substring("fileid_".length()));
        String fileLine = (String) metaData.getProperty(key);
        int tab = fileLine.indexOf("\t");
        File file = new File(fileLine.substring(0, tab));
        RAF raf = new RAF(file, "r");
        long length = Long.parseLong(fileLine.substring(tab+1));
        
        if(file.length() != length) {
          throw new BioException("File changed length: " + file);
        }
        
        if(indx >= fileCount) {
          // beyond end
          
          if(indx >= fileIDToRAF.length) {
            // beyond array end
            RAF[] tmpr = new RAF[indx];
            System.arraycopy(fileIDToRAF, 0, tmpr, 0, fileIDToRAF.length);
            fileIDToRAF = tmpr;
          }
          
          fileCount = indx;
        }
        //System.out.println(indx + " "  + file);
        fileIDToRAF[indx] = raf;
      }
    }
  }
  
  private void writeFileIDs()
  throws BioException, IOException, ChangeVetoException {
    for(int i = 0; i < fileCount; i++) {
      RAF file = fileIDToRAF[i];
      long length = file.length();
      
      metaData.setProperty("fileid_" + i, file.getFile().toString() + "\t" + length);
    }
  }
  
  RAF getFileForID(int fileId) {
    return fileIDToRAF[fileId];
  }
  
  int getIDForFile(RAF file)
  throws IOException {
    // scan list
    for(int i = 0; i < fileCount; i++) {
      if(file.equals(fileIDToRAF[i])) {
        return i;
      }
    }
    
    // extend fileIDToFile array
    if(fileCount >= fileIDToRAF.length) {
      RAF[] tmpr = new RAF[fileIDToRAF.length + 4]; // 4 is magic number
      System.arraycopy(fileIDToRAF, 0, tmpr, 0, fileCount);
      fileIDToRAF = tmpr;
    }
    
    // add the unseen file to the list
    fileIDToRAF[fileCount] = file;
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
    RAF file,
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
        List svals = (List) me.getValue();
        for(Iterator i = svals.iterator(); i.hasNext(); ) {
          String sval = (String) i.next();
          sfl.add(new KeyPair.Impl(sval, id));
        }
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
    } catch (IOException ioe) {
      throw new BioException(ioe);
    }
    
    metaData.commit();
  }
}
