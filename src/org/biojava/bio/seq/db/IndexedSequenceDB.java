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

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;
import org.biojava.utils.io.RandomAccessReader;

/**
 * <p>This class implements SequenceDB on top of a set of sequence files
 * and sequence offsets within these files.</p>
 *
 * <p> This class is primarily responsible for managing the sequence
 * IO, such as calculating the sequence file offsets, and parsing
 * individual sequences based upon file offsets. The actual persistant
 * storage of all this information is delegated to an instance of
 * <code>IndexStore</code>.</p>
 *
 * <p>Note: We may be able to improve the indexing speed further by
 * discarding all feature creation & annotation requests during index
 * parsing.</p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author Keith James
 */

public final class IndexedSequenceDB extends AbstractSequenceDB
    implements SequenceDB, Serializable 
{
    private final IDMaker idMaker;
    private final IndexStore indexStore;
    
    /**
     * Create an IndexedSequenceDB by specifying both the IDMaker and
     * IndexStore used.
     *
     * <P>
     * The IDMaker will be used to calculate the ID for each
     * Sequence. It will delegate the storage and retrieval of the
     * sequence offsets to the IndexStore.
     *
     * @param idMaker  the IDMaker used to calculate Sequence IDs
     * @param indexStore the IndexStore delegate
     */
    public IndexedSequenceDB(IDMaker idMaker, IndexStore indexStore) {
      this.idMaker = idMaker;
      this.indexStore = indexStore;
    }
    
    /**
     * Create an IndexedSequenceDB by specifying IndexStore used.
     *
     * <P>
     * IDMaker.byName will be used to calculate the ID for each
     * Sequence. It will delegate the storage and retrieval of the
     * sequence offsets to the IndexStore.
     *
     * @param indexStore the IndexStore delegate
     */
    public IndexedSequenceDB(IndexStore indexStore) {
      this(IDMaker.byName, indexStore);
    }
    
    /**
     * Retrieve the IndexStore.
     *
     * @return the IndexStore delegate
     */
    public IndexStore getIndexStore() {
      return indexStore;
    }
    
    /**
     * Add sequences from a file to the sequence database. This method
     * works on an "all or nothing" principle. If it can successfully
     * interpret the entire file, all the sequences will be read
     * in. However, if it encounters any problems, it will abandon the
     * whole file; an IOException will be thrown.  Multiple files may
     * be indexed into a single database. A BioException will be
     * thrown if it has problems understanding the sequences.
     *
     * @param seqFile the file containing the sequence or set of sequences
     * @throws BioException if for any reason the sequences can't be read
     *         correctly
     * @throws ChangeVetoException if there is a listener that vetoes adding
     *         the files
     */
  
    public void addFile(File seqFile)
	throws IllegalIDException, BioException, ChangeVetoException 
    {
      boolean completed = false; // initially assume that we will fail
      try {
        seqFile = seqFile.getAbsoluteFile();
        CountedBufferedReader bReader = new CountedBufferedReader(new FileReader(seqFile));
        SequenceFormat format = indexStore.getFormat();
        SymbolTokenization symParser = indexStore.getSymbolParser();
        SequenceBuilderFactory sbFact = indexStore.getSBFactory();
        long pos = bReader.getFilePointer();
        boolean hasNextSequence = true;
        while(hasNextSequence) {
          SequenceBuilder sb = new ElideSymbolsSequenceBuilder(sbFact.makeSequenceBuilder());
          hasNextSequence = format.readSequence(bReader, symParser, sb);
          Sequence seq = sb.makeSequence();
          String id = idMaker.calcID(seq);

          indexStore.store(new SimpleIndex(seqFile, pos, id));

	  pos = bReader.getFilePointer();
	}
        
        if(changeSupport == null) {
          indexStore.commit();
        } else {
            ChangeEvent ce = new ChangeEvent(
                this,
                SequenceDB.SEQUENCES
            );
            changeSupport.firePreChangeEvent(ce);
            indexStore.commit();
            changeSupport.firePostChangeEvent(ce);
        }
        completed = true; // we completed succesfuly
      } catch (IOException ioe) {
        throw new BioException(ioe, "Failed to read sequence file");
      } finally {
        if(!completed) { // if there was a failure, discard changes
          indexStore.rollback();
        }
      }
    }

    /**
     * SequenceBuilder implementation that explicitly discards the Symbols
     *
     * @author Thomas Down
     * @author Matthew Pocock
     */
    private static class ElideSymbolsSequenceBuilder extends SequenceBuilderFilter {
	public ElideSymbolsSequenceBuilder(SequenceBuilder delegate) {
	    super(delegate);
	}
        
        /**
         * Just ignore the symbols
         */
	public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) {
	}
    }
  
    /**
     * Get the name of this sequence database. The name is retrieved
     * from the IndexStore delegate.
     *
     * @return the name of the sequence database, which may be null.
     */
    public String getName() {
	return indexStore.getName();
    }
  
    public Sequence getSequence(String id)
    throws IllegalIDException, BioException
    {
	try
        {
	    Index indx = indexStore.fetch(id);

            RandomAccessReader rar =
		new RandomAccessReader(new RandomAccessFile(indx.getFile(), "r"));

	    long toSkip = indx.getStart();
	    if (toSkip > rar.length())
		throw new BioException("Reached end of file");
	    rar.seek(toSkip);

	    SequenceBuilder sb =
		indexStore.getSBFactory().makeSequenceBuilder();

	    indexStore.getFormat().readSequence(new BufferedReader(rar),
						indexStore.getSymbolParser(),
						sb);
	    Sequence seq = sb.makeSequence();

	    rar.close();
	    return seq;
	}
        catch (IOException ioe)
        {
	    throw new BioException(ioe, "Couldn't grab region of file"); 
	}
    }
  
    public SequenceIterator sequenceIterator() {
	return new SequenceIterator() {
	    private Iterator idI = indexStore.getIDs().iterator();
	    
	    public boolean hasNext() {
		return idI.hasNext();
	    }
	    
	    public Sequence nextSequence() throws BioException {
		return getSequence((String) idI.next());
	    }
	};
    }
  
    public Set ids() {
	return indexStore.getIDs();
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
	    buffPos += skipInBuffer;

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
	    //	    System.err.println("*** Mark");

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
	    //	    System.err.println("*** Reset");

	    if (mark < 0)
		throw new IOException("The mark is not currently in scope");

	    position = position - buffPos + mark;
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
	    // System.err.println("*** Fill buffer");

	    if (mark < 0) {
		buffFill = stream.read(buffer);
		if (buffFill == -1) {
		    buffFill = 0;
		    reachedEOF = true;
		} 
		// System.out.println("Filled buffer: " + buffFill);
		
		buffPos = 0;
	    } else {
		if (buffPos >= (markLimit + mark)) {
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

}
