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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.db.IllegalIDException;
import org.biojava.bio.seq.db.SequenceDBLite;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.seq.io.SequenceFormat;
import org.biojava.directory.Registry;
import org.biojava.directory.SystemRegistry;

/**
 * <p><code>BioGetSeq</code> is a user application for retrieving
 * sequences from flat file databanks which have been indexed
 * according to the Open Bioinformatics Database Access (OBDA)
 * indexing standard. See the BIODATABASE-ACCESS-HOWTO for more
 * information.</p>
 *
 * @biojava.use -d dataSource [-n namespace] [-f format]
 * @biojava.use -h
 * @biojava.option -d --dbname
 *                  Specifies a symbolic data source
 * @biojava.option -f --format
 *                  The output format {@biojava.default fasta}
 * @biojava.option -h --help
 *                  Command-line help
 * @biojava.option -n --namespace
 *                  Specifies the namespace within which to search
 *                  {@biojava.default ID}
 *
 * @author Keith James
 * @author Matthew Pocock
 */
public class BioGetSeq
{
    public static void main(String [] argv) throws Exception
    {
        Options opts = createOptions();

        try
        {
            CommandLine cmd = new GnuParser().parse(opts, argv);

            if (cmd.hasOption('h'))
            {
                exitHelp(opts, 0);
            }

            String namespace = cmd.getOptionValue('n', "id");

            // Currently BioJava SequenceDBs only understand the ID
            // namespace
            if (! namespace.equalsIgnoreCase("ID"))
            {
                exitHelp(opts, 1, "Only the 'ID' namespace is supported");
            }

            String dbname = cmd.getOptionValue('d', "embl");
            String format = cmd.getOptionValue('f', "fasta");

            Registry registry = SystemRegistry.instance();
            SequenceDBLite db = registry.getDatabase(dbname);

            String [] ids = cmd.getArgs();

            for (int i = 0; i < ids.length; i++)
            {
                try
                {
                    Sequence seq = db.getSequence(ids[i]);
                    String alphabet = seq.getAlphabet().getName();

                    int seqType = SeqIOTools.identifyFormat(format, alphabet);
                    SequenceFormat sf = SeqIOTools.getSequenceFormat(seqType);
                    sf.writeSequence(seq, System.out);
                }
                catch (IllegalIDException iie)
                {
                    // Skip over IDs which are not found
                    System.err.println(iie.getMessage());
                }
            }
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

    private static Options createOptions()
    {
        Options opts = new Options();
        boolean hasArg = true;

        Option dbname = new Option("d", "dbname", hasArg,
                                   "Specifies a symbolic data source");
        dbname.setRequired(true);

        Option format = new Option("f", "format", hasArg,
                                   "Specifies the output format "
                                   + "(optional, defaults to 'fasta')");
        format.setRequired(false);

        Option help = new Option("h", "help", ! hasArg,
                                 "Command line help");
        help.setRequired(false);

        Option namespace = new Option("n", "namespace", hasArg,
                                      "Specifies the namespace within which to search "
                                      + "(optional, defaults to 'ID')");
        namespace.setRequired(false);

        opts.addOption(dbname);
        opts.addOption(format);
        opts.addOption(help);
        opts.addOption(namespace);

        return opts;
    }

    private static void exitHelp(Options opts, int exitValue)
    {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("java org.biojava.app.BioGetSeq", opts);
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
