package filter;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.stax.*;
import org.biojava.bio.program.xff.*;
import org.biojava.servlets.dazzle.XMLPrettyPrinter;

public class FilterXML {
  public static void main(String [] args)
  throws Exception {
    FilterXML fXML = new FilterXML();
    
    FeatureFilter f1 = new FeatureFilter.And(
      new FeatureFilter.Not(
        new FeatureFilter.And(
          new FeatureFilter.BySource("foo"),
          new FeatureFilter.ByAnnotation("mat", "BAZ")
        )
      ),
      new FeatureFilter.Or(
        new FeatureFilter.OverlapsLocation(
          new RangeLocation(100, 200)
        ),
        new FeatureFilter.AndNot(
          new FeatureFilter.ByType("exon"),
          new FeatureFilter.HasAnnotation("bar")
        )
      )
    );
    
    StringWriter writer = new StringWriter();
    fXML.writeFilter(writer, f1);
    System.out.println(writer.toString());
    
    StringReader reader = new StringReader(writer.toString());
    FeatureFilter f2 = fXML.readFilter(reader);
    
    System.out.println("old: " + f1);
    System.out.println("new: " + f2);
    
    System.out.println("same features: " + f1.equals(f2));
  }
  
  public void writeFilter(Writer out, FeatureFilter f)
  throws SAXException {
    PrintWriter pw = new PrintWriter(out);
    XMLPrettyPrinter printer = new XMLPrettyPrinter(pw, 2);
    _writeFilter(printer, f);
    pw.flush();
  }
  
  private void _writeFilter(XMLPrettyPrinter printer, FeatureFilter f)
  throws SAXException {
    AttributeListImpl ali = new AttributeListImpl();
    if(f instanceof FeatureFilter.And) {
      FeatureFilter.And and = (FeatureFilter.And) f;
      printer.startElement("and", ali);
      _writeFilter(printer, and.getChild1());
      _writeFilter(printer, and.getChild2());
      printer.endElement("and");
    } else if(f instanceof FeatureFilter.AndNot) {
      FeatureFilter.AndNot andNot = (FeatureFilter.AndNot) f;
      printer.startElement("and_not", ali);
      _writeFilter(printer, andNot.getChild1());
      _writeFilter(printer, andNot.getChild2());
      printer.endElement("and_not");
    } else if(f instanceof FeatureFilter.Or) {
      FeatureFilter.Or or = (FeatureFilter.Or) f;
      printer.startElement("or", ali);
      _writeFilter(printer, or.getChild1());
      _writeFilter(printer, or.getChild2());
      printer.endElement("or");
    } else if(f instanceof FeatureFilter.Not) {
      FeatureFilter.Not not = (FeatureFilter.Not) f;
      printer.startElement("not", ali);
      _writeFilter(printer, not.getChild());
      printer.endElement("not");
    } else if(f instanceof FeatureFilter.ByType) {
      FeatureFilter.ByType type = (FeatureFilter.ByType) f;
      printer.startElement("type", ali);
      printer.characters(type.getType());
      printer.endElement("type");
    } else if(f instanceof FeatureFilter.BySource) {
      FeatureFilter.BySource source = (FeatureFilter.BySource) f;
      printer.startElement("source", ali);
      printer.characters(source.getSource());
      printer.endElement("source");
    } else if(f instanceof FeatureFilter.OverlapsLocation) {
      FeatureFilter.OverlapsLocation overlap =
        (FeatureFilter.OverlapsLocation) f;
      printer.startElement("overlaps", ali);
      _writeLocation(printer, overlap.getLocation());
      printer.endElement("overlaps");
    } else if(f instanceof FeatureFilter.ByAnnotation) {
      FeatureFilter.ByAnnotation ann = (FeatureFilter.ByAnnotation) f;
      ali.addAttribute("key", "CDATA", ann.getKey().toString());
      ali.addAttribute("value", "CDATA", ann.getValue().toString());
      printer.startElement("annotation", ali);
      printer.endElement("annotation");
    } else if(f instanceof FeatureFilter.HasAnnotation) {
      FeatureFilter.HasAnnotation ann = (FeatureFilter.HasAnnotation) f;
      ali.addAttribute("key", "CDATA", ann.getKey().toString());
      printer.startElement("annotation", ali);
      printer.endElement("annotation");
    } else {
      throw new SAXException("Can't serialize " + f);
    }
  }
  
  public void _writeLocation(XMLPrettyPrinter printer, Location loc)
  throws SAXException {
    AttributeListImpl ali = new AttributeListImpl();
    for(Iterator i = loc.blockIterator(); i.hasNext(); ) {
      Location block = (Location) i.next();
      ali.clear();
      ali.addAttribute("start", "CDATA", String.valueOf(block.getMin()));
      ali.addAttribute("stop", "CDATA", String.valueOf(block.getMax()));
      printer.startElement("span", ali);
      printer.endElement("span");
    }
  }
  
  public FeatureFilter readFilter(Reader in)
  throws SAXException, IOException {
    FilterHandler handler = new FilterHandler();
    InputSource is = new InputSource(in);
    SAXParser parser = new SAXParser();
    parser.setContentHandler(new SAX2StAXAdaptor(handler));
    parser.parse(is);
    return handler.getFilter();
  }
  
  public class FilterHandler
  extends StAXContentHandlerBase
  implements FilterParser
  {
    private FeatureFilter filter;
    private FilterParser parser;
    
    public FeatureFilter getFilter() {
      if(this.parser != null) {
        return parser.getFilter();
      } else {
        return this.filter;
      }
    }
    
    public void startElement(
      String nsURI,
      String localName,
      String qName,
      Attributes attrs,
      DelegationManager dm
    ) throws SAXException {
      if(localName.equals("and")) {
        parser = new MultiHandler(new FilterHandler()) {
          public FeatureFilter getFilter() {
            return new FeatureFilter.And(f1, f2);
          }
        };
        dm.delegate(parser);
      } else if(localName.equals("and_not")) {
        parser = new MultiHandler(new FilterHandler()) {
          public FeatureFilter getFilter() {
            return new FeatureFilter.AndNot(f1, f2);
          }
        };
        dm.delegate(parser);
      } else if(localName.equals("or")) {
        parser = new MultiHandler(new FilterHandler()) {
          public FeatureFilter getFilter() {
            return new FeatureFilter.Or(f1, f2);
          }
        };
        dm.delegate(parser);
      } else if(localName.equals("not")) {
        parser = new MultiHandler(new FilterHandler()) {
          public FeatureFilter getFilter() {
            return new FeatureFilter.Not(f1);
          }
        };
        dm.delegate(parser);
      } else if(localName.equals("source")) {
        dm.delegate(new StringElementHandlerBase() {
          protected void setStringValue(String s)
          throws SAXException {
            filter = new FeatureFilter.BySource(s);
          }
        });
      } else if(localName.equals("type")) {
        dm.delegate(new StringElementHandlerBase() {
          protected void setStringValue(String s)
          throws SAXException {
            filter = new FeatureFilter.ByType(s);
          }
        });
      } else if(localName.equals("overlaps")) {
        dm.delegate(new LocationHandlerBase() {
          public void setLocationValue(Location loc)
          throws SAXException {
            filter = new FeatureFilter.OverlapsLocation(loc);
          }
        });
      } else if(localName.equals("annotation")) {
        String key = attrs.getValue("key");
        String value = attrs.getValue("value");
        
        if(value != null && value.length() > 0) {
          filter = new FeatureFilter.ByAnnotation(key, value);
        } else {
          filter = new FeatureFilter.HasAnnotation(key);
        }
      } else {
        throw new SAXException("Can't parse element: " + nsURI + " " + qName + " " + localName);
      }
    }
  }
  
  private abstract class ChildHandlerBase extends StAXContentHandlerBase {
    int depth = 0;
    StAXContentHandler delegate;
    
    public ChildHandlerBase(StAXContentHandler delegate) {
      this.delegate = delegate;
    }
    
    public void startElement(
      String nsURI,
      String localName,
      String qName,
      Attributes attrs,
      DelegationManager dm
    ) throws SAXException {
      if(!atRoot()) {
        dm.delegate(delegate);
      }
      depth++;
    }
    
    public void endElement(String nsURI, String localName, String qName)
        throws SAXException
    {
      depth--;
    }
    
    public boolean atRoot() {
      return depth <= 0;
    }
    
    public StAXContentHandler getHandler() {
      return delegate;
    }
  }
  
  public interface FilterParser extends StAXContentHandler {
    FeatureFilter getFilter();
  }
  
  abstract private class MultiHandler extends ChildHandlerBase implements FilterParser {
    protected FeatureFilter f1;
    protected FeatureFilter f2;
    
    public MultiHandler(FilterParser delegate) {
      super(delegate);
    }
    
    public void endElement(String nsURI, String localName, String qName)
    throws SAXException {
      super.endElement(nsURI, localName, qName);
      if(!atRoot()) {
        FeatureFilter ff = ((FilterParser) getHandler()).getFilter();
        if(f1 == null) {
          f1 = ff;
        } else {
          f2 = ff;
        }
      }
    }
  }
}
