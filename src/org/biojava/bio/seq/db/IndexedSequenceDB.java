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
 * This class reads in a file or a set of files containing sequence data.  It
 * contains methods for automatically indexing these sequences.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */

public final class IndexedSequenceDB extends AbstractSequenceDB
    implements SequenceDB, Serializable 
{
    private final String name;
    private final IDMaker idMaker;
    private final File indexFile;
    private final Set files;
    private final SequenceFormat format;
    private final SequenceBuilderFactory sbFact;
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
    public static IndexedSequenceDB createDB(String name,
					     File indexFile,  
					     SequenceFormat format,
					     SequenceBuilderFactory sbFact,
					     SymbolParser symParser,
					     IDMaker idMaker) 
	throws IOException, BioException 
    {
	if(!indexFile.exists()) {
	    try {
		IndexedSequenceDB isd = new IndexedSequenceDB(name,
							      indexFile,
							      format,
							      sbFact,
							      symParser,
							      idMaker);
		isd.commit();
		return isd;
	    } catch (Throwable t) {
		if(indexFile.exists()) {
		    if(!indexFile.delete()) {
			throw new BioException(t, "Could not create index file. Failed to remove it");
		    }
		}
		throw new BioException(t, "Could not create index file");
	    }
	} else {
	    throw new BioException("Couldn't create index file as it already exists: " + indexFile);
	}
    }

    private IndexedSequenceDB(String name,
			      File indexFile,
			      SequenceFormat format,
			      SequenceBuilderFactory sbFact,
			      SymbolParser symParser,
			      IDMaker idMaker) 
    {
	this.name = name;
	this.indexFile = indexFile;
	this.format = format;
	this.sbFact = sbFact;
	this.symParser = symParser;
	this.idMaker = idMaker;
	this.files = new HashSet();
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
	throws IOException, BioException, ChangeVetoException 
    {
	seqFile = seqFile.getAbsoluteFile();
	CountedBufferedReader bReader = new CountedBufferedReader(new FileReader(seqFile)); 
	HashMap index = new HashMap();
	long pos = bReader.getFilePointer();
	boolean hasNextSequence = true;
	while(hasNextSequence) {
	    SequenceBuilder sb = new ElideSymbolsSequenceBuilder(sbFact.makeSequenceBuilder());
	    hasNextSequence = format.readSequence(bReader, symParser, sb);
	    Sequence seq = sb.makeSequence();
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

    private static class ElideSymbolsSequenceBuilder implements SequenceBuilder {
	private final SequenceBuilder delegate;

	public ElideSymbolsSequenceBuilder(SequenceBuilder delegate) {
	    this.delegate = delegate;
	}

	public void startSequence() {
	    delegate.startSequence();
	}

	public void endSequence() {
	    delegate.endSequence();
	}

	public void setName(String name) {
	    delegate.setName(name);
	}

	public void setURI(String uri) {
	    delegate.setURI(uri);
	}

	public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) {
	}

	public void addSequenceProperty(String key, Object value) {
	    delegate.addSequenceProperty(key, value);
	}

	public void startFeature(Feature.Template templ) {
	    delegate.startFeature(templ);
	}

	public void endFeature() {
	    delegate.endFeature();
	}

	public void addFeatureProperty(String key, Object value) {
	    delegate.addFeatureProperty(key, value);
	}

	public Sequence makeSequence() throws BioException {
	    return delegate.makeSequence();
	}
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
	    FileReader fr = new FileReader(s.file);
	    long toSkip = s.start;
	    while (toSkip > 0) {
		long skipped = fr.skip(toSkip);
		if (skipped < 0)
		    throw new IOException("Reached end of file");
		toSkip -= skipped;
	    }
	    BufferedReader br = new BufferedReader(fr);

	    SequenceBuilder sb = sbFact.makeSequenceBuilder();
	    format.readSequence(br, symParser, sb);
	    Sequence seq = sb.makeSequence();
	    // putReader(s.file, raf);
	    br.close();
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
    
    private static class CountedBufferedReader extends BufferedReader {
	private final static int DEFAULT_BUFFER_SIZE = 1 << 14;

	private long position;

	private Reader stream;
	private char[] buffer;
	private int buffPos;
	private int buffFill;

	private boolean reachedEOF = false;

	private int mark = -1;
	private int markLimit = -1;

	public long getFilePointer() {
	    return position;
	}

	public CountedBufferedReader(Reader stream) {
	    super(new Reader() {
		public void close() {}
		public int read(char[] cbuf, int off, int len) { return 0; }
	    });

	    this.stream = stream;
	    this.buffer = new char[DEFAULT_BUFFER_SIZE];
	    position = 0;
	    buffPos = 0;
	    buffFill = 0;
	}

	public void close()
	    throws IOException
	{
	    stream.close();
	    stream = null;
	}

	public int read() 
	    throws IOException
	{
	    if (buffPos >= buffFill)
		fillBuffer();

	    if (reachedEOF) {
		return -1;
	    } else {
		position++;
		return buffer[buffPos++];
	    }
	}

	private int peek()
	    throws IOException
	{
	    if (buffPos >= buffFill)
		fillBuffer();

	    if (reachedEOF) {
		return -1;
	    } else {
		return buffer[buffPos];
	    }
	}

	public int read(char[] cbuf)
	    throws IOException
	{
	    return read(cbuf, 0, cbuf.length);
	}

	public int read(char[] cbuf, int off, int len) 
	    throws IOException
	{
	    if (buffPos >= buffFill)
		fillBuffer();

	    if (reachedEOF) {
		return -1;
	    } else {
		int toReturn = Math.min(len, buffFill - buffPos);
		System.arraycopy(buffer, buffPos, cbuf, off, toReturn);
		buffPos += toReturn;
		position += toReturn;

		return toReturn;
	    }
	}

	public boolean ready() 
	    throws IOException
	{
	    if (buffPos < buffFill)
		return true;
	    return stream.ready();
	}

	public long skip(long n)
	    throws IOException
	{
	    int skipInBuffer;
	    if (n < buffer.length) {
		skipInBuffer = Math.min((int) n, buffFill - buffPos);
	    } else {
		skipInBuffer = buffFill - buffPos;
	    }
	    position += skipInBuffer;
	    buffPos = buffFill;

	    if (n > skipInBuffer) {
		long skippedInStream;

		if (mark >= 0) {
		    // Yuck, fix this...
		    char[] dummy = new char[(int) (n - skipInBuffer)];
		    skippedInStream = read(dummy); 
		} else {
		    skippedInStream = stream.skip(n - skipInBuffer);
		}

		position += skippedInStream;
		return skippedInStream + skipInBuffer;
	    } else {
		return skipInBuffer;
	    }
	}

	public boolean markSupported() {
	    return true;
	}

	public void mark(int limit)
	    throws IOException
	{
	    if (limit + 1> buffer.length) {
		char[] newBuffer = new char[limit + 1];
		System.arraycopy(buffer, buffPos, newBuffer, 0, buffFill - buffPos);
		buffer = newBuffer;
		buffFill = buffFill - buffPos;
		buffPos = 0;
	    } else if (buffPos + limit > buffer.length) {
		System.arraycopy(buffer, buffPos, buffer, 0, buffFill - buffPos);
		buffFill = buffFill - buffPos;
		buffPos = 0;
	    }

	    mark = buffPos;
	    markLimit = limit;
	}

	public void reset()
	    throws IOException
	{
	    if (mark < 0)
		throw new IOException("The mark is not currently in scope");

	    position = position - mark + buffPos;
	    buffPos = mark;
	}

	public String readLine()
	    throws IOException 
	{
	    StringBuffer sb = new StringBuffer(100);
	    
	    int c = read();
	    while (c >= 0 && c != '\n' && c != '\r') {
		sb.append((char) c);
		c = read();
	    }

	    if (c == '\r') {
		c = peek();
		if (c == '\n')
		    read();
	    }

	    // System.out.println("Readline: " + sb.toString());

	    String retVal = sb.toString();
	    return retVal;
	}

	private void fillBuffer()
	    throws IOException
	{
	    if (mark < 0) {
		buffFill = stream.read(buffer);
		if (buffFill == -1) {
		    buffFill = 0;
		    reachedEOF = true;
		} 
		// System.out.println("Filled buffer: " + buffFill);
		
		buffPos = 0;
	    } else {
		if (buffPos >= (markLimit - mark)) {
		    // Mark's gone out of scope -- wheee!
		    mark = -1;
		    markLimit = -1;
		    fillBuffer();
		    return;
		}

		System.arraycopy(buffer, mark, buffer, 0, buffFill - mark);
		buffFill = buffFill - mark;
		mark = 0;
		buffPos = buffFill;
		int newChars = stream.read(buffer, buffFill, buffer.length - buffFill);
		if (newChars == -1) {
		    reachedEOF = true;
		} else {
		    buffFill = buffFill + newChars;
		}
	    }
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
	    return read(cbuf, 0, cbuf.length);
	}
    
	public int read(char[] cbuf, int off, int len) throws IOException {
	    for(int i = off; i < off+len; i++) {
		int b = raf.read();
		if (b < 0) {
		    if (i == 0)
			return - 1;
		    else 
			return i - off;
		}

		cbuf[i] = (char) b;	
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
		raf.read();
	    }
	    return n;
	}
    }
}
