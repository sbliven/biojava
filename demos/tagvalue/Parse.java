package tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.formats.*;

public class Parse {
  public static void main(String[] args)
  throws Exception {
    if(args.length < 3) {
      useageAndExit();
    }

    boolean build = false;

    if(args[0].startsWith("-b")) {
      build = true;
    } else if(args[0].startsWith("-e")) {
      build = false;
    } else {
      useageAndExit();
    }

    String formatName = args[1];
    Format format = createFormat(formatName);
    System.err.println("Using format: " + format.getLSID());

    TagValueListener listener;
    if(build) {
      listener = new AnnotationBuilder(format.getType());
    } else {
      listener = new Echo();
    }
    ParserListener pl = format.getParserListener(listener);

    Parser parser = new Parser();

    for(int i = 2; i < args.length; i++) {
      BufferedReader reader = new BufferedReader(
        new FileReader(
          new File(args[i]) ));
      while(parser.read(reader, pl.getParser(), pl.getListener())) {
        if(build) {
          System.out.println(((AnnotationBuilder) listener).getLast());
        }
      }
    }
  }

  private static void useageAndExit() {
    System.err.println("Use: tagvalue.Parse [-b | -e] format <file list>");

    System.err.println("\t-b\t: build and print out full anntoation bundles");
    System.err.println("\t-e\t: echo parser events directly");

    System.err.println("\tformat\t: format name. One of");
    System.err.println("\t\tthe name of a class in org.biojava.bio.program.format");
    System.err.println("\t\tfully qualified class name");
    System.err.println("\t\tEither way, the class must be castable to");
    System.err.println("\t\torg.biojava.bio.program.formats.Format.");

    System.exit(1);
  }

  private static Format createFormat(String formatName)
  throws Exception {
    Class formatClass;

    try {
      formatClass = Parser.class.getClassLoader().loadClass(formatName);
    } catch (ClassNotFoundException cnfe1) {
      System.err.println("Could not find class named: " + formatName);
      formatName = "org.biojava.bio.program.formats." + formatName;
      try {
        formatClass = Parser.class.getClassLoader().loadClass(formatName);
      } catch (ClassNotFoundException cnfe2) {
        System.err.println("Could not find class named: " + formatName);
        throw cnfe2; // we could do something more sane here
      }
    }

    return (Format) formatClass.newInstance();
  }
}
