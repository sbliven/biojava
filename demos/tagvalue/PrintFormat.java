package tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.formats.*;

public class PrintFormat {
  public static void main(String[] args)
  throws Exception {
    if(args.length != 1) {
      System.err.println("Use: tagvalue.PrintFormat formatName");
    }

    Format format = FormatTools.getFormat(args[0]);
    System.out.println(format.getLSID());
    pretyPrint(format.getType(), "  ");
  }

  private static void pretyPrint(AnnotationType at, String indent) {
    List properties = new ArrayList(at.getProperties());
    Collections.sort(properties);
    for(Iterator i = properties.iterator(); i.hasNext(); ) {
      Object prop = i.next();
      System.out.print(indent);
      System.out.print(prop);

      CollectionConstraint cc = at.getConstraint(prop);
      if(cc instanceof CollectionConstraint.AllValuesIn) {
        CollectionConstraint.AllValuesIn avi = (CollectionConstraint.AllValuesIn) cc;
        PropertyConstraint pc = avi.getPropertyConstraint();
        if(pc instanceof PropertyConstraint.ByAnnotationType) {
          AnnotationType annoT = ((PropertyConstraint.ByAnnotationType) pc).getAnnotationType();
          System.out.println(" {");
          pretyPrint(annoT, indent + "  ");
          System.out.print(indent);
          System.out.print("}");
        }
      }
      System.out.println();
    }
  }
}
