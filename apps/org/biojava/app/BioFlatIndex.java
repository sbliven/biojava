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

package org.biojava.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.cli.*;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.program.indexdb.BioStore;
import org.biojava.bio.program.indexdb.IndexTools;
import org.biojava.bio.seq.io.SeqIOConstants;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.utils.lsid.LifeScienceIdentifier;
import org.biojava.utils.lsid.LifeScienceIdentifierParseException;

/**
 * <p><code>BioFlatIndex</code> is a user application for performing
 * sequence flat file indexing according to the Open Bioinformatics
 * Database Access (OBDA) indexing standard. It may be used to create
 * a new index or to update an existing index with new files. When
 * creating a new index the sequence file format (currently one of
 * 'fasta', 'swissprot', 'embl' or 'genbank', case-insensitive) and
 * alphabet ('dna', 'rna', 'aa', case-insensitive) must be
 * indicated. This information is stored in the index and is used by
 * the sequence retrieval code to determine how to treat the indexed
 * file(s). See the FLAT-DATABASES-HOWTO for more information.</p>
 *
 * @biojava.use -c -a alphabet -d dbName -f format [-i indexScheme -l indexRoot] seqFileList
 * @biojava.use -d [-l] seqFileList
 * @biojava.use -h
 * @biojava.option -a --alphabet
 *                  Source file alphabet {@biojava.values dna rna aa}
 * @biojava.option -c --create
 *                  Create a new index
 * @biojava.option -d --dbname
 *                  Specify the symbolic database name (used as the index root
 *                  directory name)
 * @biojava.option -f --format
 *                  Specifies the source file format
 * @biojava.option -h --help
 *                  Command line help
 * @biojava.option -i --index
 *                  Specify the indexing scheme {@biojava.default flat}
 * @biojava.option -l --location
 *                  Path to the index root directory {@biojava.default `cwd`}
 * @biojava.argument seqFileList
 *                  Any number of sequence file names of the apropreate format
 * @author Keith James
 * @author Matthew Pocock
 */
public class BioFlatIndex
{
    public static void main(String [] argv) throws Exception
    {
        Options opts = createOptions();

        try
        {
            CommandLine cmd = new GnuParser().parse(opts, argv);

            String alphabet = "";
            String   format = "";
            String   dbName = "";
            String location = "";
            boolean create = cmd.hasOption('c');

            if (cmd.hasOption('h'))
                exitHelp(opts, 0);

            if (cmd.hasOption('l'))
                location = cmd.getOptionValue('l');
            else
                location = System.getProperty("user.dir");

            if (cmd.hasOption('d'))
                dbName = cmd.getOptionValue('d');
            else
                exitHelp(opts, 2, "No index name was specified");

            String [] fileNames = cmd.getArgs();
            Set seqFiles = new HashSet();

            for (int i = 0; i < fileNames.length; i++)
            {
                seqFiles.add(new File(fileNames[i]));
            }

            File index = new File (location, dbName);

            if (create)
            {
                // If creating, we need alphabet and format
                if (! cmd.hasOption('a'))
                    exitHelp(opts, 2, "No sequence alphabet was specified");

                if (! cmd.hasOption('f'))
                    exitHelp(opts, 2, "No sequence format was specified");

                if (fileNames.length == 0)
                    exitHelp(opts, 2, "No sequence files were specified");

                alphabet = cmd.getOptionValue('a');
                format   = cmd.getOptionValue('f');
            }
            else
            {
                // If updating, we get alphabet and format from the
                // existing index
                BioStore store = new BioStore(index, false);
                Annotation metaData = store.getMetaData();

                LifeScienceIdentifier formatSpec = getFormatSpec(metaData);
                alphabet = formatSpec.getObjectId();
                format   = formatSpec.getNamespaceId();

                List indexedFiles = getIndexedFiles(metaData);
                seqFiles.addAll(indexedFiles);
            }

            int seqType = SeqIOTools.identifyFormat(format, alphabet);
            if (seqType == SeqIOConstants.UNKNOWN)
            {
                exitHelp(opts, 2, "Unknown format/alphabet combination: "
                         + format
                         + "/"
                         + alphabet);
            }

            doIndex(dbName, index, seqFiles, seqType);
        }
        catch (MissingOptionException moe)
        {
            exitHelp(opts, 1, moe.getMessage());
        }
        catch (MissingArgumentException mae)
        {
            exitHelp(opts, 1, mae.getMessage());
        }
        catch (UnrecognizedOptionException uoe)
        {
            exitHelp(opts, 1, uoe.getMessage());
        }
        catch (Exception e)
        {
            exitHelp(opts, 1, e.getMessage());
        }
    }

    private static LifeScienceIdentifier getFormatSpec(Annotation config)
        throws BioException
    {
        LifeScienceIdentifier format;

        try
        {
            String lsid = (String) config.getProperty("format");
            format = LifeScienceIdentifier.valueOf(lsid);
        }
        catch (NoSuchElementException nsee)
        {
            throw new BioException("Malformed OBDA index "
                                   + "does not indicate sequence format",
                                   nsee);
        }
        catch (LifeScienceIdentifierParseException lse)
        {
            throw new BioException("Malformed OBDA index "
                                   + "has a format identifier which is not a valid LSID",
                                   lse);
        }

        return format;
    }

    private static List getIndexedFiles(Annotation config)
    {
        List files = new ArrayList();

        for (Iterator ci = config.keys().iterator(); ci.hasNext();)
        {
            String key = (String) ci.next();
            if (key.startsWith("fileid_"))
            {
                String val = (String) config.getProperty(key);
                int tab = val.indexOf("\t");
                files.add(new File(val.substring(0, tab)));
            }
        }

        return files;
    }

    private static void doIndex(String dbName, File index, Set seqFiles,  int seqType)
        throws Exception
    {
        if (index.exists())
        {
            File [] indexedFiles = index.listFiles();
            for (int i = 0; i < indexedFiles.length; i++)
            {
                if (indexedFiles[i].canWrite())
                    throw new BioException("Unable to modify existing index. "
                                           + indexedFiles[i]
                                           + " is not writable");

                indexedFiles[i].delete();
            }

            if (! index.canWrite())
                throw new BioException("Unable to modify existing index. "
                                       + index
                                       + " is not writable");

            index.delete();
        }

        File [] files = (File []) seqFiles.toArray(new File [0]);

        int formatType = seqType & (~ 0xffff0000);
        int alphaType = seqType & (~ 0xffff);

        switch (formatType)
        {
            case (SeqIOConstants.FASTA):
                IndexTools.indexFasta(dbName, index, files, alphaType);
                break;

            case (SeqIOConstants.EMBL):
                IndexTools.indexEmbl(dbName, index, files, alphaType);
                break;

            case (SeqIOConstants.GENBANK):
                IndexTools.indexGenbank(dbName, index, files, alphaType);
                break;

            case (SeqIOConstants.SWISSPROT):
                IndexTools.indexSwissprot(dbName, index, files);
                break;

            default:
                throw new IllegalArgumentException("Sequence format not supported");
        }
    }

    private static Options createOptions()
    {
        Options opts = new Options();
        boolean hasArg = true;

        Option alphabet = new Option("a", "alphabet", hasArg,
                                     "Specifies the source file alphabet "
                                     + "(required if creating an index)");
        alphabet.setRequired(false);

        Option create = new Option("c", "create", ! hasArg,
                                   "Create a new index (optional)");
        create.setRequired(false);

        Option dbname = new Option("d", "dbname", hasArg,
                                   "Specifies the symbolic database name "
                                   + "(used as the index directory name)");
        dbname.setRequired(false);

        Option format = new Option("f", "format", hasArg,
                                   "Specifies the source file format "
                                   + "(required if creating an index)");
        format.setRequired(false);

        Option help = new Option("h", "help", ! hasArg,
                                 "Command line help");
        help.setRequired(false);

        Option index = new Option("i", "index", hasArg,
                                  "Specifies the indexing scheme "
                                  + "(optional, defaults to 'flat')");
        index.setRequired(false);

        Option location = new Option("l", "location", hasArg,
                                     "Path to the index root directory "
                                     + "(optional, defaults to cwd)");
        location.setRequired(false);

        opts.addOption(alphabet);
        opts.addOption(create);
        opts.addOption(dbname);
        opts.addOption(format);
        opts.addOption(help);
        opts.addOption(index);
        opts.addOption(location);

        return opts;
    }

    private static void exitHelp(Options opts, int exitValue)
    {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("java org.biojava.app.BioFlatIndex", opts);
        System.exit(exitValue);
    }

    private static void exitHelp(Options opts, int exitValue, String message)
    {
        if (exitValue == 0)
            System.out.println(message);
        else
            System.err.println(message);

        exitHelp(opts, exitValue);
    }
}
