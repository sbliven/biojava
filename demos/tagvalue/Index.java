package tagvalue;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.biojava.bio.*;
import org.biojava.bio.program.indexdb.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.formats.*;

public class Index {
  public static void main(String[] args)
  throws Exception {
    // fixme: we're using Indexer - we should realy be using Indexer2, but
    // i'm not sure if it's debugged right now

    String storeName = args[0];
    File storeFile = new File(args[1]);
    String formatName = args[2];
    String idAndLen = args[3];
    String[] scndrysAndLen = args[4].split(":");

    Format format = FormatTools.getFormat(formatName);

    BioStoreFactory bsf = new BioStoreFactory();
    bsf.setStoreLocation(storeFile);
    bsf.setSequenceFormat(format.getLSID());

    Pattern idLenPat = Pattern.compile("(\\S+),(\\d+))");

    Matcher idMatcher = idLenPat.matcher(idAndLen);
    if(!idMatcher.matches()) {
      doIdLenError(idAndLen);
    }

    bsf.setPrimaryKey(idMatcher.group(1));
    bsf.addKey(idMatcher.group(1), Integer.parseInt(idMatcher.group(2)));

    for(int i = 0; i < scndrysAndLen.length; i++) {
      Matcher m = idLenPat.matcher(scndrysAndLen[i]);
      if(!m.matches()) {
        doIdLenError(scndrysAndLen[i]);
      }
      bsf.addKey(m.group(1), Integer.parseInt(m.group(2)));
    }

    BioStore store = bsf.createBioStore();
    Indexer indexer = new Indexer(storeFile, store);

    String pk = bsf.getPrimaryKey();
    indexer.setPrimaryKeyName(bsf.getPrimaryKey());
    for(Iterator i = bsf.getKeys().iterator(); i.hasNext(); ) {
      String key = (String) i.next();
      if(!key.equals(pk)) {
        indexer.addSecondaryKey(key);
      }
    }

    Parser parser = new Parser();
    ParserListener pl = format.getParserListener(indexer);

    while(parser.read(indexer.getReader(), pl.getParser(), pl.getListener())) {
      ;
    }

    store.commit();
  }

  private static void doIdLenError(String idLen) {
    System.err.println("Could not process " + idLen);
    System.err.println("Expecting <fieldName>,<length>");
    System.exit(1);
  }
}
