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
package search;

import java.lang.System;
import java.io.*;
import java.util.*;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * <code>IndexFastaDB</code> will perform Biojava indexing of DNA or
 * protein Fasta format files, creating an IndexedSequenceDB-compliant
 * database. Setting the database name is optional; if not set, it
 * defaults to being the same as the first file indexed. The index and
 * store files are given the database name with the extension '.index'
 * and '.store' respectively.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @version 1.0
 * @since 1.1
 */
public class IndexFastaDB
{
    static final String USAGE =
	"\nUsage: java -Dtype=(aa|nt) [-Dname=<database name>] "
	+ "IndexFastaDB file(s)";
    static final String INDEX = ".index";
    static final String  LIST = ".list";

    public static void main (String [] args)
	throws Exception
    {
	Properties             props;
	String                 seqType;
	String                 dbName;

	Alphabet               alpha;
	SymbolParser           parser;
	SequenceFormat         seqFormat;
	IDMaker                idMaker;
	SequenceBuilderFactory sbFactory;

	TabIndexStore          indexStore;
	IndexedSequenceDB      indexedDB;

	// Check arguments and system properties
	if (args.length < 1)
	    throw new Exception(USAGE);

	props = System.getProperties();

	seqType = props.getProperty("type");
	if (seqType == null)
	    throw new Exception(USAGE);
	if (! seqType.equalsIgnoreCase("aa") || ! seqType.equalsIgnoreCase("aa"))
	    throw new Exception(USAGE);

	// Set the appropriate alphabet
	if (seqType.equalsIgnoreCase("aa"))
	    alpha = ProteinTools.getAlphabet();
	else
	    alpha = DNATools.getDNA();

	// Check for database name below
	dbName = props.getProperty("name");

	seqFormat = new FastaFormat();
	idMaker   = new IDMaker.ByName();
	sbFactory = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);

	try
	{
	    parser = alpha.getParser("token");

	    String fastaFileName = args[0];

	    // If database name is not set, use the name of the Fasta
	    // file
	    if (dbName == null)
		dbName = fastaFileName;

	    File fastaFile = new File(fastaFileName);
	    File indexFile = new File(dbName + INDEX);
	    File indexList = new File(dbName + LIST);

	    System.out.println("Creating database with name '"
			       + dbName
			       + "' from "
			       + fastaFileName);

	    // Instantiate the store and index the first file
	    indexStore = new TabIndexStore(indexFile,
					   indexList,
					   dbName,
					   seqFormat,
					   sbFactory,
					   parser);
	    indexedDB = new IndexedSequenceDB(idMaker, indexStore);

	    indexedDB.addFile(fastaFile);

	    // Add any subsequent files to the index
	    for (int i = 1; i < args.length; i++)
	    {
		fastaFileName = args[i];
		fastaFile = new File(fastaFileName);
		System.out.println("Indexing and adding file "
				   + fastaFileName);
		
		indexedDB.addFile(fastaFile);
	    }
	}
	catch (BioException be)
	{
	    be.printStackTrace();
	}
	catch (IOException ioe)
	{
	    ioe.printStackTrace();
	}
    }
}
