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

package org.biojava.bio.seq.db;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * <p>
 * Implements IndexStore as a serialized file for the java data and a 
 * tab-delimited file of offsets.
 * </p>
 * The tab-delimited file looks like:
 * <pre>
 * fileNumber \t offset \t id \n
 * </pre>
 */
public class TabIndexStore implements IndexStore, Serializable {
  public static TabIndexStore open(File storeFile)
  throws IOException {
    try {
      FileInputStream fis = new FileInputStream(storeFile);
      ObjectInputStream p = new ObjectInputStream(fis);
      TabIndexStore indxStore = (TabIndexStore) p.readObject();
      fis.close();
      return indxStore;
    } catch (ClassNotFoundException cnfe) {
      throw new NestedError(cnfe, "Assertion Failure: How did we get here?");
    }
  }
  
  
  // internal book-keeping for indices
  private transient Map idToIndex;
  private transient Map commited;
  private transient Map uncommited;

  // the two files for storing the store info and the actual table of indices
  private final File storeFile;
  private final File indexFile;
  
  private final String name;

  private final Set files;
  private File[] seqFileIndex;

  private final SequenceFormat format;
  private final SequenceBuilderFactory sbFactory;
  private final SymbolTokenization symbolParser;
  
  public TabIndexStore(
    File storeFile,
    File indexFile, 
    String name,
    SequenceFormat format,
    SequenceBuilderFactory sbFactory,
    SymbolTokenization symbolParser
  ) throws IOException, BioException {
    if(storeFile.exists() || indexFile.exists()) {
      throw new BioException("Files already exist");
    }
    
    this.storeFile = storeFile.getAbsoluteFile();
    this.indexFile = indexFile.getAbsoluteFile();
    this.name = name;
    this.format = format;
    this.sbFactory = sbFactory;
    this.symbolParser = symbolParser;
    
    this.files = new HashSet();
    this.seqFileIndex = new File[0];
    
    this.commited = new HashMap();
    this.uncommited = new HashMap();
    this.idToIndex = new OverlayMap(commited, uncommited);
    
    commit();
  }
  
  public void store(Index indx) throws IllegalIDException, BioException {
    if(idToIndex.containsKey(indx.getID())) {
      throw new IllegalIDException("ID already in use: '" + indx.getID() + "'");
    }
    
    addFile(indx.getFile());
    uncommited.put(indx.getID(), indx);
  }
  
  public Index fetch(String id) throws IllegalIDException, BioException {
    Index indx = (Index) idToIndex.get(id);
    
    if(indx == null) {
      throw new IllegalIDException("No Index known for id '" + id + "'");
    }
    
    return indx;
  }
  
  public void commit() throws BioException {
    try {
      PrintWriter out = new PrintWriter(
        new FileWriter(
          indexFile.toString(), true
        )
      );
      for(Iterator i = uncommited.values().iterator(); i.hasNext(); ) {
        Index indx = (Index) i.next();
        
        out.println(
          getFileIndex(indx.getFile()) + "\t" +
          indx.getStart() + "\t" +
          indx.getID()
        );
      }
      
      commitStore();
      
      out.close();

      commited.putAll(uncommited);
      uncommited.clear();
    } catch (IOException ioe) {
      throw new BioException(ioe, "Failed to commit");
    }
  }
  
  public void rollback() {
    uncommited.clear();
  }
  
  public String getName() {
    return name;
  }
  
  public Set getIDs() {
    return Collections.unmodifiableSet(idToIndex.keySet());
  }
  
  public Set getFiles() {
    return Collections.unmodifiableSet(files);
  }
  
  public SequenceFormat getFormat() {
    return format;
  }
  
  public SequenceBuilderFactory getSBFactory() {
    return sbFactory;
  }
  
  public SymbolTokenization getSymbolParser() {
    return symbolParser;
  }
  
  protected void commitStore() throws IOException {
    FileOutputStream fos = new FileOutputStream(storeFile);
    ObjectOutputStream p = new ObjectOutputStream(fos);
    p.writeObject(this);
    p.flush();
    fos.close();
  }
  
  protected void addFile(File f) {
    if(!files.contains(f)) {
      int len = seqFileIndex.length;
      files.add(f);
      File[] sfi = new File[len + 1];
      System.arraycopy(this.seqFileIndex, 0, sfi, 0, len);
      sfi[len] = f;
      this.seqFileIndex = sfi;
    }
  }
  
  protected int getFileIndex(File file) {
    for(int pos = seqFileIndex.length-1; pos >= 0; pos--) {
      File f = seqFileIndex[pos];
       // don't know if this construct is faster than a plain equals()
      if(f == file || file.equals(f)) {
        return pos;
      }
    }
      
    throw new IndexOutOfBoundsException("Index not found for File '" + file + "'");
  }
  
  protected void initialize() throws IOException {
    if(indexFile.exists()) {
      // load in stuff from the files
      BufferedReader reader = new BufferedReader(
        new FileReader(indexFile  )
      );
      
      for(
        String line = reader.readLine();
        line != null;
        line = reader.readLine()
      ) {
        StringTokenizer stok = new StringTokenizer(line);
        int fileNum = Integer.parseInt(stok.nextToken());
        long start = Long.parseLong(stok.nextToken());
        String id = stok.nextToken();
        
        SimpleIndex index = new SimpleIndex(
          seqFileIndex[fileNum],
          start,
          -1,
          id
        );
        
        commited.put(id, index);
      }
    }
  }
  
  private void readObject(ObjectInputStream in)
  throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    
    this.commited = new HashMap();
    this.uncommited = new HashMap();
    this.idToIndex = new OverlayMap(commited, uncommited);
    
    this.initialize();
  }
}
