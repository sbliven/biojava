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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.program.indexdb.BioStore;
import org.biojava.bio.program.indexdb.BioStoreFactory;
import org.biojava.bio.program.tagvalue.ChangeTable;
import org.biojava.bio.program.tagvalue.Indexer;
import org.biojava.bio.program.tagvalue.LineSplitParser;
import org.biojava.bio.program.tagvalue.Parser;
import org.biojava.bio.program.tagvalue.ValueChanger;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.FastaFormat;
import org.biojava.bio.seq.io.SeqIOAdapter;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.seq.io.SequenceBuilderFactory;
import org.biojava.bio.seq.io.StreamReader;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.utils.NestedException;
import org.biojava.utils.ParserException;
import org.biojava.utils.io.CountedBufferedReader;
import org.biojava.utils.io.RAF;
import org.biojava.utils.lsid.LifeScienceIdentifier;

/**
 * <code>IndexTools</code> contains static utility methods for
 * creating flatfile indices according to the OBDA standard.
 *
 * @author Keith James
 * @author Matthew Pocock
 */
public class IndexTools
{
    // Cannot be instantiated
    private IndexTools() { }

    public static void indexFastaDNA(File location, File [] seqFiles)
        throws FileNotFoundException, IOException, BioException
    {
        BioStoreFactory bsf = new BioStoreFactory();
        bsf.setPrimaryKey("ID");
        bsf.setStoreLocation(location);
        bsf.setSequenceFormat(LifeScienceIdentifier.valueOf("open-bio.org",
                                                            "fasta",
                                                            "dna" ));
        bsf.addKey("ID", 10);

        BioStore store = bsf.createBioStore();
        SymbolTokenization toke =
            DNATools.getDNA().getTokenization("token");
        indexFasta(store, toke, seqFiles);
    }

    public static void indexFastaRNA(File location, File [] seqFiles)
        throws FileNotFoundException, IOException, BioException
    {
        BioStoreFactory bsf = new BioStoreFactory();
        bsf.setPrimaryKey("ID");
        bsf.setStoreLocation(location);
        bsf.setSequenceFormat(LifeScienceIdentifier.valueOf("open-bio.org",
                                                            "fasta",
                                                            "rna" ));
        bsf.addKey("ID", 10);

        BioStore store = bsf.createBioStore();
        SymbolTokenization toke =
            RNATools.getRNA().getTokenization("token");
        indexFasta(store, toke, seqFiles);
    }

    public static void indexFastaProtein(File location, File [] seqFiles)
        throws FileNotFoundException, IOException, BioException
    {
        BioStoreFactory bsf = new BioStoreFactory();
        bsf.setPrimaryKey("ID");
        bsf.setStoreLocation(location);
        bsf.setSequenceFormat(LifeScienceIdentifier.valueOf("open-bio.org",
                                                            "fasta",
                                                            "protein" ));
        bsf.addKey("ID", 10);

        BioStore store = bsf.createBioStore();
        SymbolTokenization toke =
            ProteinTools.getAlphabet().getTokenization("token");
        indexFasta(store, toke, seqFiles);
    }

    public static void indexEmbl(File location, File [] seqFiles)
    {
        
    }

    public static void indexSwissprot(File location, File [] seqFiles)
        throws FileNotFoundException, IOException, ParserException,
               BioException
    {
        BioStoreFactory bsf = new BioStoreFactory();
        bsf.setPrimaryKey("ID");
        bsf.setStoreLocation(location);
        bsf.addKey("AC", 10);
        bsf.addKey("ID", 10);

        BioStore store = bsf.createBioStore();

        for (int i = 0; i < seqFiles.length; i++)
        {
            Indexer indexer = new Indexer(seqFiles[i], store);
            indexer.setPrimaryKeyName("ID");
            indexer.addSecondaryKey("AC");

            ChangeTable changeTable = new ChangeTable();

            changeTable.setChanger("ID", new ChangeTable.Changer()
                {
                    public Object change(Object value)
                    {
                        String s = (String) value;
                        int i = s.indexOf(" ");
                        return s.substring(0, i);
                    }
                });

            changeTable.setChanger("AC", new ChangeTable.Changer()
                {
                    public Object change(Object value)
                    {
                        String s = (String) value;
                        int i = s.indexOf(";");
                        return s.substring(0, i);
                    }
                });

            ValueChanger changer = new ValueChanger(indexer, changeTable);
            Parser parser = new Parser();

            while(parser.read(indexer.getReader(),
                              LineSplitParser.EMBL, changer));
        }

        try
        {
            store.commit();
        }
        catch (NestedException ne)
        {
            throw new BioException(ne, "Failed to commit new index to file");
        }
    }

    private static void indexFasta(BioStore store, SymbolTokenization toke,
                                   File [] seqFiles)
        throws FileNotFoundException, IOException, BioException
    {
         FastaFormat format = new FastaFormat();

         for (int i = 0; i < seqFiles.length; i++)
         {
             RAF raf = new RAF(seqFiles[i], "r");

             FastaIndexer indexer = new FastaIndexer(raf, store);
             StreamReader reader =
                 new StreamReader(indexer.getReader(), format, toke, indexer);

             while (reader.hasNext())
             {
                 reader.nextSequence();
             }
        }

         try
         {
             store.commit();
         }
         catch (NestedException ne)
         {
             throw new BioException(ne, "Failed to commit new index to file");
         }
    }

    private static class FastaIndexer implements SequenceBuilderFactory
    {
        private final Map map = new HashMap();
        private final RAF raf;
        private final IndexStore store;
        private final CountedBufferedReader reader;
    
        public FastaIndexer(RAF raf, IndexStore store) throws IOException
        {
            this.raf = raf;
            this.store = store;
            reader = new CountedBufferedReader(new FileReader(raf.getFile()));
        }

        public CountedBufferedReader getReader()
        {
            return reader;
        }

        public SequenceBuilder makeSequenceBuilder()
        {
            return new SeqIOIndexer();
        }

        class SeqIOIndexer extends SeqIOAdapter implements SequenceBuilder
        {
            long offset = 0L;
            String id;

            public void startSequence()
            {
                id = null;
                offset = reader.getFilePointer();
            }

            public void addSequenceProperty(Object key, Object value)
            {
                if (key.equals(FastaFormat.PROPERTY_DESCRIPTIONLINE))
                {
                    String line = (String) value;
                    int a = line.indexOf(" ");

                    if (a != -1)
                        id = line.substring(0, a);
                    else
                        id = line;
                }
            }

            public void endSequence()
            {
                long nof = reader.getFilePointer();
                store.writeRecord(raf, offset, (int) (nof - offset), id, map);
                offset = nof;
            }

            public Sequence makeSequence()
            {
                return null;
            }
        }
    }
}
