import java.io.*;
import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.io.agave.*;
import org.biojava.directory.*;
import org.biojava.bio.program.xff.*;
import org.biojava.utils.xml.*;

public class RFetch {
    private static void printUsage() {
	System.err.println("usage: rfetch [-fasta | -gff | -embl | -genbank | -agave | -xff] namespace id1 id2 ...");
    }

    public static void main(String[] args)
        throws Exception
    {
	String format = "fasta";
	boolean all = false;

	int startArgs = 0;
	while (startArgs < args.length && args[startArgs].startsWith("-")) {
	    String sw = args[startArgs];
	    if ("-fasta".equals(sw)) {
		format = "fasta";
	    } else if ("-gff".equals(sw)) {
		format = "gff";
	    } else if ("-embl".equals(sw)) {
		format = "embl";
	    } else if ("-genbank".equals(sw)) {
		format = "genbank";
	    } else if ("-agave".equals(sw)) {
		format = "agave";
	    } else if ("-xff".equals(sw)) {
            format = "xff";
        }else if ("-list".equals(sw)) {
		format = "list";
	    } else if ("-all".equals(sw)) {
		all = true;
	    } else {
		System.err.println("Unknown switch: " + sw);
		printUsage();
		return;
	    }

	    ++startArgs;
	}
	
	if (args.length - startArgs< 1) {
	    printUsage();
	    return;
	}

	SequenceDBLite seqDB;
	String namespace = args[startArgs++];
	try {
	    seqDB = SystemRegistry.instance().getDatabase(namespace);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.err.println("Can't access namespace " + namespace);
	    return;
	}

	if (all) {
	    if (seqDB instanceof SequenceDB) {
		SequenceDB lseqDB = (SequenceDB) seqDB;
		for (SequenceIterator si = lseqDB.sequenceIterator(); si.hasNext(); ) {
		    writeSequence(si.nextSequence(), format);
		}
	    } else {
		System.err.println("Can't list namespace " + namespace);
	    }
	} else if ("list".equals(format)) {
	    if (seqDB instanceof SequenceDB) {
		SequenceDB lseqDB = (SequenceDB) seqDB;
		for (Iterator i = lseqDB.ids().iterator(); i.hasNext(); ) {
		    System.out.println(i.next());
		}
	    } else {
		System.err.println("Can't list namespace " + namespace);
	    }
	} else {
	    while (startArgs < args.length) {
		String name = args[startArgs++];
		Sequence seq = null;
		try {
		    seq = seqDB.getSequence(name);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		if (seq != null) {
		    writeSequence(seq, format);
		}
	    }
	}
    }

    private static void writeSequence(Sequence seq, String format)
        throws Exception
    {
	if ("embl".equals(format)) {
	    new EmblLikeFormat().writeSequence(seq, System.out);
	} else if ("genbank".equals(format)) {
	    new GenbankFormat().writeSequence(seq, System.out);
	} else if ("fasta".equals(format)) {
	    new FastaFormat().writeSequence(seq, System.out);
	} else if ("gff".equals(format)) {
	    SequencesAsGFF seqgff = new SequencesAsGFF();
	    seqgff.setRecurse(true);
	    PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
	    GFFWriter gffw = new GFFWriter(pw);
	    seqgff.processSequence(seq, gffw);
	    pw.flush();
	} else if ("xff".equals(format)) {
        XFFWriter xffw = new XFFWriter();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out));
        XMLWriter xw = new PrettyXMLWriter(pw);
        xffw.writeFeatureSet(seq, xw);
        pw.flush();
    } else if ("agave".equals(format)) {
	    new AgaveWriter().writeSequence(seq, System.out);
	}
    }
}
