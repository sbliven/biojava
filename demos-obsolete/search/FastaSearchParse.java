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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.db.IndexedSequenceDB;
import org.biojava.bio.seq.db.TabIndexStore;
import org.biojava.bio.seq.db.SimpleSequenceDBInstallation;

import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchSubHit;

import org.biojava.bio.program.search.SearchReader;
import org.biojava.bio.program.search.SearchBuilder;
import org.biojava.bio.program.search.FastaSearchBuilder;
import org.biojava.bio.program.search.SearchParser;
import org.biojava.bio.program.search.FastaSearchParser;

import org.biojava.bio.symbol.Alignment;

/**
 * <code>FastaSearchParse</code> is a demo of Fasta search output
 * parsing. The objects representing the Fasta result and hits are
 * simply printed to STDOUT in this example. Suitable data files are
 * in the demos/files directory: fp_queries.db (a queryFile),
 * fp_demo.db (a subjectFile) and fp_demo.m10 (a fastaOutputFile).
 *
 * Run the demo like so: <code>java search/FastaSearchParse
 * files/fp_queries.db files/fp_demo.db
 * files/fp_demo.m10</code>
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.1
 */
public class FastaSearchParse
{
    public static void main (String [] args)
    {
	String queryFileName       = args[0];
	String subjectFileName     = args[1];
	String fastaOutputFileName = args[2];

	File queryFile    = new File(queryFileName);
	File queryIndex   = new File(queryFileName + ".index");

	File subjectFile  = new File(subjectFileName);
	File subjectIndex = new File(subjectFileName + ".index");

	try
	{
	    SequenceDB                   queryDB;
	    SequenceDB                   subjectDB;
	    SimpleSequenceDBInstallation dbInstallation;

	    queryDB   = new IndexedSequenceDB(TabIndexStore.open(queryIndex));
	    subjectDB = new IndexedSequenceDB(TabIndexStore.open(subjectIndex));

	    dbInstallation = new SimpleSequenceDBInstallation();
	    dbInstallation.addSequenceDB(subjectDB, new HashSet());

	    BufferedReader reader = new BufferedReader(new FileReader(new File(fastaOutputFileName)));

	    SearchBuilder handler =
		new FastaSearchBuilder(dbInstallation, queryDB);
	    SearchParser parser = new FastaSearchParser();

	    SearchReader searchReader = new SearchReader(reader,
							 handler,
							 parser);

	    while (searchReader.hasNext())
	    {
		SeqSimilaritySearchResult result =
		    (SeqSimilaritySearchResult) searchReader.next();

		System.out.println("----------------------------------------");
		System.out.println("Start of result: " + result.toString());
		System.out.println("----------------------------------------");

		System.out.println("Result has query sequence: "
				   + result.getQuerySequence().toString());

		List hits = result.getHits();

		for (Iterator hi = hits.iterator(); hi.hasNext();)
		{
		    SeqSimilaritySearchHit hit = (SeqSimilaritySearchHit) hi.next();

		    System.out.println("\n-> Hit: " + hit);

		    List subHits = hit.getSubHits();

		    for (Iterator shi = subHits.iterator(); shi.hasNext();)
		    {
			SeqSimilaritySearchSubHit subHit = (SeqSimilaritySearchSubHit) shi.next();
			System.out.println("--> SubHit: " + subHit);
                        System.out.println("      Query start: " + subHit.getQueryStart());
                        System.out.println("        Query end: " + subHit.getQueryEnd());
                        System.out.println("     Query strand: " + subHit.getQueryStrand());
                        System.out.println("    Subject start: " + subHit.getSubjectStart());
                        System.out.println("      Subject end: " + subHit.getSubjectEnd());
                        System.out.println("   Subject strand: " + subHit.getSubjectStrand());

			Alignment al = subHit.getAlignment();
			List  labels = al.getLabels();

			try
			{
			    for (Iterator li = labels.iterator(); li.hasNext();)
			    {
				String label = (String) li.next();
				System.out.println(label
						   + ": "
						   + al.symbolListForLabel(label).seqString());
			    }
			}
			catch (ClassCastException cce)
			{
			    cce.printStackTrace();
			}
			catch (NoSuchElementException nse)
			{
			    nse.printStackTrace();
			}
		    }
		}
		System.out.println("----------------------------------------");
		System.out.println("End of result: " + result.toString());
		System.out.println("----------------------------------------");
	    }
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
    }
}
