package ssbind;

import java.util.*;
import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.program.ssbind.*;
import java.beans.*;
import java.lang.reflect.*;

import org.biojava.bio.program.sax.*;
import org.biojava.bio.search.*;

/**
 * <p>
 * Driver script that parses a blast report and pumps the result through a
 * configurable chain of filters and handlers.
 * </p>
 *
 * <p>
 * This command expects to be given a blast report as the first argument, a
 * SearchContentHandler with a no-args constructor as the last argument and
 * any number of SearchContentFilters inbetween. It assumes that all classes
 * can be configured using beany accessors (get/setFoo methods). If you supply
 * the class name as-is, the class will be instantiated. If you give the class
 * name followed by brackets, you can put any number of property names and their
 * values within the brackets.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * # echo parsing events to stdout
 * java ProcessBlastReport blast.out ssbind.Echoer
 *
 * # echo events that are left after all hits with expectedValue greater
 * # than 0.1 have been removed
 * java ProcessBlastReport \
 *    "ssbind.FilterByValue(maxVal=0.1 keyName=expectedValue)" \
 *    ssbind.Echoer
 *
 * # echo the percentageIdentity of all alignments between 100 and 200 in length
 * java ProcessBlastReport \
 *   "ssbind.FilterByValue(minVal=100 maxVal=200 keyName=alignmentSize)" \
 *   "ssbind.PropertyEchoer(keyName=percentageIdentity)"
 * </pre>
 *
 * @author Matthew Pocock
 */
public class ProcessBlastReport {
  public static void main(String[] args)
  throws Exception {
    File blastFile = new File(args[0]);

    SearchContentHandler handler = null;
    for(int i = args.length - 1; i > 0; i--) {
      String arg = args[i];
      int ob = arg.indexOf("(");
      String className;
      String[] argList;
      
      if(ob != -1 && arg.endsWith(")")) {
        className = arg.substring(0, ob);
        String argString = arg.substring(ob+1, arg.length() - 1);
        argList = argString.split("\\s+|=");
      } else {
        className = arg;
        argList = new String[] {};
      }
      handler = createHandler(className, argList, handler);
    }

    SeqSimilarityAdapter adapter = new SeqSimilarityAdapter();
    adapter.setSearchContentHandler(handler);
    BlastLikeSAXParser reader = new BlastLikeSAXParser();
    reader.setModeLazy();
    InputSource is = new InputSource(new FileReader(blastFile));
    reader.setContentHandler(adapter);
            
    reader.parse(is);
  }
  
  /**
   * Introspection magic to make a handler and configure its properties.
   */
  private static SearchContentHandler createHandler(
    String handlerClassName,
    String[] args,
    SearchContentHandler delegate
  ) throws Exception {
    // instantiate our handler class
    Class handlerClass = ProcessBlastReport.class.getClassLoader().loadClass(handlerClassName);
    SearchContentHandler handler;
    
    if(delegate == null) {
      handler = (SearchContentHandler) handlerClass.newInstance();
    } else {
      handler = (SearchContentHandler) handlerClass.getConstructor(
        new Class[] { SearchContentHandler.class }
      ).newInstance(new Object[] { delegate });
    }

    // set any beany properties
    BeanInfo info = Introspector.getBeanInfo(handlerClass);
    PropertyDescriptor[] props = info.getPropertyDescriptors();
    for(int i = 0; i < args.length; i+=2) {
      String propName = args[i];
      String propVal = args[i+1];
      boolean handled = false;
      
      for(int p = 0; p < props.length; p++) {
        PropertyDescriptor pd = props[p];
        
        if(pd.getName().equals(propName)) {
          // set the value
          Method write = pd.getWriteMethod();
          Object consVal = null;
          Class propClass = write.getParameterTypes()[0];
          if(propClass == String.class) {
            consVal = propVal;
          } else if(propClass.isPrimitive()) {
            // fixme: other primitives?
            if(propClass == Double.TYPE) {
              consVal = new Double(propVal);
            }
          } else {
            Constructor cons = propClass.getConstructor(new Class[] { String.class });
            consVal = cons.newInstance(new Object[] { propVal});
           }
           write.invoke(handler, new Object[] { consVal });
           handled = true;
        }
      }
      if(handled == false) {
        throw new IllegalArgumentException(
          "Couldn't process : " + propName + " -> " + propVal +
          " for bean of class " + handlerClass
        );
      }
    }
    
    return handler;
  }
}
