package annodb;

import java.util.*;
import java.io.*;

import org.biojava.bio.annodb.*;
import org.biojava.bio.program.tagvalue.*;

/**
 * Creates an index DB.
 *
 * @author Matthew Pocock
 * @since 1.3
 */ 
public class CreateIndexDB {
  public static void main(String[] args)
  throws Exception {
//    if(args.length != 1) {
//      System.err.println("use: annodb.CreateIndexedDB indexConfig.xml");
//    }

    File indexDir = new File(args[0]);
    List fileList = new ArrayList();
    for(int i = 1; i < args.length; i++) {
      fileList.add(new File(args[i]));
    } 

    Index2Model model = new Index2Model();
    model.setPrimaryKeyName("ID");
    model.addKeyPath("ID", new String[] { "ID" });
    model.addKeyPath("AC", new String[] { "AC" });
    model.addKeyPath("SV", new String[] { "SV" });
    model.addKeyPath("db_xref", new String[] { "FT", "source", "db_xref" });

    AnnotationDB annoDB = new IndexedAnnotationDB(
      "Embl",
      indexDir,
      model,
      fileList,
      15,
      Formats.EMBL_TYPE,
      new IndexedAnnotationDB.StaticMethodRPFactory(Formats.class.getMethod(
        "createEmblParserListener", new Class[] { TagValueListener.class }
      ))
    );
  }
} 
