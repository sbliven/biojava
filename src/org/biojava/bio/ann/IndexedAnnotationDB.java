package org.biojava.bio.ann;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.beans.*;

import org.biojava.utils.*;
import org.biojava.utils.io.*;
import org.biojava.utils.xml.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.program.indexdb.*;
import org.biojava.bio.seq.io.filterxml.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.biojava.utils.stax.*;

import org.biojava.bio.program.tagvalue.Parser;

/**
 * <p>A database of Annotation instances backed by an indexed file set.</p>
 *
 * @author Matthew Pocock
 */
public class IndexedAnnotationDB
implements AnnotationDB {
  private final BioStore store;
  private final AnnotationType schema;
  private final ParserListenerFactory plFactory;
  private final ParserListener parserListener;
  private final AnnotationBuilder annBuilder;
  private final Parser recordParser;
  
  public IndexedAnnotationDB(
    BioStore store,
    AnnotationType schema,
    ParserListenerFactory plFactory
  ) throws IOException {
    // state
    this.store = store;
    this.schema = schema;
    this.plFactory = plFactory;
    this.annBuilder = new AnnotationBuilder(schema);
    this.parserListener = plFactory.getParserListener(annBuilder);
    this.recordParser = new Parser();
    
    // persistance
    File factoryFile = new File(store.getLocation(), "ParserListenerFactory.xml");
    XMLEncoder xmlEnc = new XMLEncoder(
      new BufferedOutputStream(
        new FileOutputStream(
          factoryFile
        )
      )
    );
    xmlEnc.writeObject(plFactory);
    xmlEnc.close();
    
    File schemaFile = new File(store.getLocation(), "schema.xml");
    PrintWriter schemaPW = new PrintWriter(
      new FileWriter(
        schemaFile
      )
    );
    XMLWriter schemaWriter = new PrettyXMLWriter(schemaPW);
    XMLAnnotationTypeWriter schemaTW = new XMLAnnotationTypeWriter();
    schemaTW.writeAnnotationType(schema, schemaWriter);
    schemaPW.flush();
    schemaPW.close();
  }
  
  public IndexedAnnotationDB(BioStore store) throws IOException, SAXException {
    this.store = store;
    
    File factoryFile = new File(store.getLocation(), "ParserListenerFactory.xml");
    XMLDecoder xmlDec = new XMLDecoder(
      new BufferedInputStream(
        new FileInputStream(
          factoryFile
        )
      )
    );
    this.plFactory = (ParserListenerFactory) xmlDec.readObject();
    xmlDec.close();
    
    File schemaFile = new File(store.getLocation(), "schema.xml");
    InputSource is = new InputSource(
      new BufferedReader(
        new FileReader(
          schemaFile
        )
      )
    );
    XMLReader parser = XMLReaderFactory.createXMLReader();
    XMLAnnotationTypeHandler annTypeH = new XMLAnnotationTypeHandler();
    parser.setContentHandler(
      new SAX2StAXAdaptor(
        annTypeH
      )
    );
    this.schema = annTypeH.getAnnotationType();
    
    this.annBuilder = new AnnotationBuilder(schema);
    this.parserListener = plFactory.getParserListener(annBuilder);
    this.recordParser = new Parser();
  }
  
  public String getName() {
    return store.getName();
  }

  public AnnotationType getSchema() {
    return schema;
  }
  
  public Iterator iterator() {
    return new Iterator() {
      Iterator rli = store.getRecordList().iterator();
      
      public boolean hasNext() {
        return rli.hasNext();
      }
      
      public Object next() {
        try {
          return process((Record) rli.next());
        } catch (Exception e) {
          throw new NestedRuntimeException(e);
        }
      }
      
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public int size() {
    return store.getRecordList().size();
  }
  
  public AnnotationDB filter(AnnotationType at) {
    AnnotationType schema = AnnotationTools.intersection(at, this.schema);
    
    if(schema != AnnotationType.NONE) {
      return new LazyFilteredAnnotationDB("", this, schema);
    } else {
      return AnnotationDB.EMPTY;
    }
  }
  
  public AnnotationDB search(AnnotationType at) {
    return new LazySearchedAnnotationDB("", this, at);
  }
  
  public ParserListenerFactory getParserListenerFactory() {
    return plFactory;
  }
  
  private Annotation process(Record rec)
  throws IOException, ParserException {
    RandomAccessReader rar = new RandomAccessReader(rec.getFile());
    rar.seek(rec.getOffset());
    BufferedReader reader = new BufferedReader(rar);
    recordParser.read(reader, parserListener.getParser(), parserListener.getListener());
    return annBuilder.getLast();
  }
  
  public interface ParserListenerFactory
  extends Serializable {
    public ParserListener getParserListener(TagValueListener listener);
  }
  
  public class StaticMethodRPFactory
  implements ParserListenerFactory {
    private final  Method method;
    
    public StaticMethodRPFactory(Method method)
    throws IllegalArgumentException {
      if(method.getModifiers() != Modifier.STATIC) {
        throw new IllegalArgumentException("Method must be static");
      }
      
      if(method.getReturnType() != ParserListener.class) {
        throw new IllegalArgumentException("Method must return a ParserListener instance");
      }
      
      if(
        method.getParameterTypes().length != 1 ||
        method.getParameterTypes()[0] != TagValueListener.class
      ) {
        throw new IllegalArgumentException("Method must accept a single TagValueListener as it's sole parameter");
      }
      
      this.method = method;
    }
    
    public Method getMethod() {
      return method;
    }
    
    public ParserListener getParserListener(TagValueListener tvl) {
      try {
        return (ParserListener) method.invoke(null, new Object[] { tvl });
      } catch (Exception e) {
        throw new NestedRuntimeException(e);
      }
    }
  }
}

