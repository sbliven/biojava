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

public class ProcessBlastReport {
  public static void main(String[] args)
  throws Exception {
    File blastFile = new File(args[0]);

    Class handlerClass = ProcessBlastReport.class.getClassLoader().loadClass(args[1]);
    SearchContentHandler handler = (SearchContentHandler) handlerClass.newInstance();

    BeanInfo info = Introspector.getBeanInfo(handlerClass);
    PropertyDescriptor[] props = info.getPropertyDescriptors();
    for(int i = 2; i < args.length; i+=2) {
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
          } else {
            Constructor cons = propClass.getConstructor(new Class[] { String.class });
            consVal = cons.newInstance(new Object[] { propVal});
           }
           write.invoke(handler, new Object[] { consVal });
           handled = true;
        }

        if(handled == false) {
          throw new IllegalArgumentException(
            "Couldn't process : " + propName + " -> " + propVal +
            " for bean of class " + handlerClass
          );
        }
      }
    }

    SeqSimilarityAdapter adapter = new SeqSimilarityAdapter();
    adapter.setSearchContentHandler(handler);
    BlastLikeSAXParser reader = new BlastLikeSAXParser();
    reader.setModeLazy();
    InputSource is = new InputSource(new FileReader(blastFile));
    reader.setContentHandler(adapter);
            
    reader.parse(is);
  }
}
