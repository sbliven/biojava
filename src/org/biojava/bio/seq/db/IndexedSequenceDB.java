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
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

/**
 * [FIXME] Broken since newio changes [td2].
 * This class reads in a file or a set of files containing sequence data.  It
 * contains methods for automatically indexing these sequences.
 */


public final class IndexedSequenceDB
extends AbstractSequenceDB
implements SequenceDB, Serializable {
  private final String name;
  private final IDMaker idMaker;
  private final File indexFile;
  private final Set files;
  private final SequenceFormat format;
  private final SequenceFactory sFact;
  private final SymbolParser symParser;
  private final Map idToSource; // seqID -> Source {file, start}
  private transient Map fileToReaders; // file -> set ( readers )
  
  {
    fileToReaders = new HashMap();
    idToSource = new HashMap();
  }

  /**
   * Open an index at indexFile.
   * <P>
   * If indexFile exists, it will just load the indexes from there. If it
   * does not exist, a new index file will be created.
   *
   * @param the File to use for persistantly storing the indexes
   * @throws IOException if for any reason indexFile can't be used
   */
  public static IndexedSequenceDB openDB(File indexFile)
  throws IOException, BioException {
    if(indexFile.exists()) {
      FileInputStream fis = new FileInputStream(
        indexFile
      );
      try {
        ObjectInputStream p = new ObjectInputStream(fis);
        IndexedSequenceDB isd = (IndexedSequenceDB) p.readObject();
        fis.close();
        return isd;
      } catch (ClassNotFoundException cnfe) {
        throw new BioException(cnfe, "Couldn't load index file: " + indexFile);
      }
    } else {
      throw new BioException(
        "Couldn't open index as it doesn't exist: " + indexFile
      );
    }
  }
  
  /**
  *Create a sequence database
  *@param name a name for the database
  *@param indexFile the indexed file of sequences
  *@param format the kind of format being read in e.g. EMBL/FASTA
  *@param sFact the sequence factory object for generating sequence objects from the file.
  *@param symParser the SymbolParser object for the sequences read in e.g. DNA or RNA parsers.
  *@param idMaker sets the idMaker to map the set of sequences encountered.      
  */
  public static IndexedSequenceDB createDB(
    String name,
    File indexFile,  
    SequenceFormat format,
    SequenceFactory sFact,
    SymbolParser symParser,
    IDMaker idMaker
  ) throws IOException, BioException {
    if(!indexFile.exists()) {
      try {
        IndexedSequenceDB isd = new IndexedSequenceDB(
          name,
          indexFile,
          format,
          sFact,
          symParser,
          idMaker
        );
        isd.commit();
        return isd;
      } catch (Throwable t) {
        if(indexFile.exists()) {
          if(!indexFile.delete()) {
            throw new BioException(
              t, "Could not create index file. Failed to remove it"
            );
          }
        }
        throw new BioException(t, "Could not create index file");
      }
    } else {
      throw new BioException(
        "Couldn't create index file as it already exists: " + indexFile
      );
    }
  }

  private IndexedSequenceDB(
    String name,
    File indexFile,
    SequenceFormat format,
    SequenceFactory sFact,
    SymbolParser symParser,
    IDMaker idMaker
  ) 
    {
	this.name = name;
	this.indexFile = indexFile;
	this.format = format;
	this.sFact = sFact;
	this.symParser = symParser;
	this.idMaker = idMaker;
	this.files = new HashSet();

	throw new BioError("[FIXME] broken after newio");
    }

  private RandomAccessFile getReader(File f) throws IOException {
    if(fileToReaders == null) {
      fileToReaders = new HashMap();
    }
    synchronized(fileToReaders) {
      Set readers = (Set) fileToReaders.get(f);
      if(readers == null) {
        fileToReaders.put(f, readers = new HashSet());
      }
      if(readers.isEmpty()) {
        return new RandomAccessFile(f, "r");
      } else {
        RandomAccessFile raf = (RandomAccessFile) readers.iterator().next();
        readers.remove(raf);
        return raf;
      }
    }
  }


/**
*set a reading context to the database.
*@param f the file which will be read.
*@param raf the random access file object for the reader
*/
  private void putReader(File f, RandomAccessFile raf) {
    if(fileToReaders == null) {
      fileToReaders = new HashMap();
    }
    synchronized(fileToReaders) {
      Set readers = (Set) fileToReaders.get(f);
      if(readers == null) {
        fileToReaders.put(f, readers = new HashSet());
      }
      readers.add(raf);
    }
  }
      
  /**
   * Retrieve an unmodifiable set of files.
   *
   * @return a Set of all files indexed by this indexer
   */
  public Set getFiles() {
    return Collections.unmodifiableSet(files);
  }
  
  /**
  * Add sequences from a file to the sequence database.  This method works
  * on an "all or nothing" principle.  If it can successfully interpret the
  * entire file, all the sequences will be read in.  However, if it
  * encounters any problems, it will abandon the whole file; an IOException
  * will be thrown.  A bioexception will be thrown if it has problems
  * understanding the sequences.
  *
  * @param seqFile the file containing the sequence or set of sequences
  * @throws IOException if the IO fails
  * @throws BioException if for any reason the sequences can't be read
  *         correctly
  * @throws ChangeVetoException if there is a listener that vetoes adding
  *         the files
  */
  
  public void addFile(File seqFile)
  throws IOException, BioException, ChangeVetoException {
    seqFile = seqFile.getAbsoluteFile();
    HackedBufferedReader bReader = new HackedBufferedReader(seqFile); 
    StreamReader.Context context = new StreamReader.Context(bReader);
    HashMap index = new HashMap();
    long pos = bReader.getFilePointer();
    while(!context.isStreamEmpty()) {
      Sequence seq = null; /* [FIXME] format.readSequence(context, symParser, sFact); */
      Source s = new Source(seqFile, pos);
      String id = idMaker.calcID(seq);
      if(index.containsKey(id)) {
        throw new BioError(
          "Sequences must have unique IDs: " + id +
          " in file " + seqFile + " at " + pos
        );
      }
      if(idToSource.containsKey(id)) {
        Source t = (Source) idToSource.get(id);
        throw new BioError("Sequences must have unique IDs: " + id +
          " in file " + seqFile + " at " + pos +
          " conflicts with " + t.file + " at " + t.start
        );
      }
      index.put(id, s);
      pos = bReader.getFilePointer();
    }
    
    if(changeSupport == null) {
      idToSource.putAll(index);
    } else {
      ChangeEvent ce = new ChangeEvent(
        this,
        SequenceDB.SEQUENCES,
        index,
        null
      );
      changeSupport.firePreChangeEvent(ce);
      idToSource.putAll(index);
      changeSupport.firePostChangeEvent(ce);
    }
    commit();
  }

  /**
  *Remove a file from the database
  *@param seqFile the file to remove
  */
  public void removeFile(File seqFile) throws IOException {
    files.remove(seqFile);
    commit();
  }
  
  /**
   * Commits the state of this indexed seq db to indexFile.
   */
  private void commit() throws IOException {
    FileOutputStream fos = new FileOutputStream(indexFile);
    ObjectOutputStream p = new ObjectOutputStream(fos);
    p.writeObject(this);
    p.flush();
    fos.close();
  }
  
  public String getName() {
    return this.name;
  }
  
  public Sequence getSequence(String id) throws BioException {
    try {
      Source s = (Source) idToSource.get(id);
      if(s == null) {
        throw new BioException("Couldn't find sequence for id " + id);
      }
      RandomAccessFile raf = getReader(s.file);
      raf.seek(s.start);
      HackedBufferedReader hbr = new HackedBufferedReader(raf);
      StreamReader.Context context = new StreamReader.Context(hbr);
      Sequence seq = null; /* [FIXME] format.readSequence(context, symParser, sFact); */
      putReader(s.file, raf);
      return seq;
    } catch (IOException ioe) {
      throw new BioException(ioe, "Couldn't grab region of file"); 
    }
  }
  
  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      private Iterator idI = ids().iterator();
      
      public boolean hasNext() {
        return idI.hasNext();
      }
      
      public Sequence nextSequence() throws BioException {
        return getSequence((String) idI.next());
      }
    };
  }
  
  public Set ids() {
    return Collections.unmodifiableSet(idToSource.keySet());
  }
  
  /**
   * A useful tuple to locate an individual record.
   */
  private final class Source implements Serializable {
    public final File file;
    public final long start;
    
    public Source(File file, long start) {
      this.file = file;
      this.start = start;
    }
  }
  
  private class HackedBufferedReader extends BufferedReader {
    private final RandomAccessFile raf;
    private long marker;
    
    public HackedBufferedReader(File file)
    throws IOException {
      this(new RandomAccessFile(file, "r"));
    }
    
    public HackedBufferedReader(RandomAccessFile raf)
    throws IOException {
      super(new Reader() {
        public void close() {}
        public int read(char[] cbuf, int off, int len) { return 0; }
      });
      this.raf = raf;
    }
    
    public long getFilePointer() throws IOException {
      return raf.getFilePointer();
    }
    
    public void close() throws IOException {
      raf.close();
    }
    
    public void mark(int readAheadLimit) throws IOException {
      marker = raf.getFilePointer();
    }
    
    public boolean markSupported() {
      return true;
    }
    
    public int read() throws IOException {
      return raf.read();
    }
    
    public int read(char[] cbuf) throws IOException {
      for(int i = 0; i < cbuf.length; i++) {
        cbuf[i] = raf.readChar();
      }
      return cbuf.length;
    }
    
    public int read(char[] cbuf, int off, int len) throws IOException {
      for(int i = off; i < off+len; i++) {
        cbuf[i] = raf.readChar();
      }
      return len;
    }
    
    public String readLine() throws IOException {
      return raf.readLine();
    }
    
    public boolean ready() {
      return true;
    }
    
    public void reset() throws IOException {
      raf.seek(marker);
    }
    
    public long skip(long n) throws IOException {
      for(int i = 0; i < n; i++) {
        raf.readChar();
      }
      return n;
    }
  }
}
