
/**
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

package org.biojava.bio.seq.ragbag;

import java.lang.String;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.NoSuchElementException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;

import org.xml.sax.*;
import org.apache.xerces.parsers.*; 

import org.biojava.utils.stax.*;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.seq.StrandedFeature;

/**
 * This class is an object that encapulates all information 
 * present in a Map file in a Ragbag directory.
 */
public class RagbagMap extends StAXContentHandlerBase
{
  // main storage structures
/**
 * a list of mappings for components onto virtual sequence
 */
  private List map;

/**
 * Associates filenames with their references
 */
  private Map refMap;

  private File mapFile;
  private boolean locked = false;
  private int dstLength=0;

  // stuff to maintain state machine state
  // making a special class for these seems overkill
  private static int INIT      = 0;
  private static int RAGBAGMAP = 1;
  private static int COMPONENT = 2;
  private static int MAPPING   = 3;
  private int level=0;

  private int previousState;

  // variables used to pass outer nesting info to inner nested element handler
  private String componentFilename;
  private String componentRef;

  private String EMPTYSTRING = "";

  {
    map = new Vector();
    refMap = new HashMap();
    previousState = INIT;
  }

/**
 * create a RagbagMap from the specified File
 * @param mapFile XML file containing map data.
 */
  public RagbagMap(File mapFile)
  {
    this.mapFile = mapFile;
  }

/**
 * Parse the specified mapping file for this object now.
 */
  public void parse()
    throws FileNotFoundException, IOException, SAXException
  {
    // check that we are not changing an initialised object
    if (locked) throw new SAXException("Attempt to change an locked RagbagMap!");

    // create parser
    SAXParser parser = new SAXParser();

    // assign self as content handler
    parser.setContentHandler(new SAX2StAXAdaptor(this));

    // parse the map file
    InputSource is = new InputSource(new FileReader(mapFile));
    parser.parse(is);
  }

/**
 * gets reference string for a given filename
 * @return String value of reference. Returns empty string if it doesn't exist.
 */
  public String getRef(String filename)
  {
    String refString = (String) refMap.get(filename);
    if (refString == null) {
      System.err.println("RagbagMap.getRef lookup failed for " + filename);
      return "";
    }
    else {
//      System.err.println("RagbagMap.getRef lookup succeeded for " + filename + " response is " + refString);
      return refString;
    }
  }

/**
 * Enumeration object for RagbagMap. An Iterator was not used because a RagbagMap
 * is immutable after it has completed initialisation.
 */
  public class MapEnum implements Enumeration
  {
    MapElement mapElem=null;
    String filename;
    Iterator mapI;

    private MapEnum(String filename)
    {
      // create iterator to map
      this.filename = filename;
      mapI = map.iterator();
    }

    public boolean hasMoreElements()
    {
      if (mapElem == null) {
        while (mapI.hasNext()) {
          MapElement thisMapElem = (MapElement) mapI.next();
          if (thisMapElem.filename.equals(filename)) {
            // we have an object that can be returned with next()
            // cache it for the next() call.
            mapElem = thisMapElem;
            return true;
          }
        }

      // can't find any further entries
      mapElem = null;
      return false;
      }
      else {
        // already have a chached object ready for return
        return true;
      }      
    }

    public Object nextElement()
    {
      if (mapElem == null) {
        // we don't have an object yet
        // get one or crash in the attempt.
        if (!hasMoreElements())
          throw new NoSuchElementException();
      }

      // return the cached element
      MapElement returnElem = mapElem;
      mapElem = null;
      return (Object) returnElem;
    } 
  }

/**
 * class that represents a single mapping in mapFile
 */
  public class MapElement
  {
    private String ref;
    private String filename;
    private RangeLocation srcLoc;
    private RangeLocation dstLoc;
    private StrandedFeature.Strand strand;

    private MapElement(String ref,
                       String filename, 
                       RangeLocation srcLoc, 
                       RangeLocation dstLoc, 
                       StrandedFeature.Strand strand)
    {
      // initialise object
      this.filename = filename;
      this.srcLoc = srcLoc;
      this.dstLoc = dstLoc;
      this.strand = strand;
    }
    // to enforce immutability
    public String getRef() {return ref;}
    public String getFilename() {return filename;}
    public RangeLocation getSrcLocation() {return srcLoc;}
    public RangeLocation getDstLocation() {return dstLoc;}
    public StrandedFeature.Strand getStrand() {return strand;}
  }

/**
 * @return an enumeration object for mappings for a file
 * of given filename.
 */
  public Enumeration getEnumeration(String filename)
  {
     // return an enumerator
     return new MapEnum(filename);
  }

/**
 * @return the minimum destination sequence length needed to
 * represent the mapping.
 */
  public int getDstLength()
  {
    return dstLength;
  }

  public void endTree()
  {
    // implement lock to prevent further changes in object after
    // reading mapping list.  Can be seriously bad juju.
    locked = true;
  }



  public void startElement(
                String nsURI,
                String localName,
                String qName,
                Attributes attrs,
                DelegationManager dm)
    throws SAXException
  {
    // update stack level
    level++;

    switch (previousState) {
      // initial condition
      case 0 /*INIT*/:
        // transition to RAGBAGMAP permitted only
        if ((level == 1) && localName.equals("ragbag_map"))
          previousState = RAGBAGMAP;
        else
          throw new SAXException("Ragbag map file does not start with a <ragbag_map> element");
      break;

      case 1 /*RAGBAGMAP*/:
        // transition to component possible
        if ((level == 2) && localName.equals("component")) {
          // move to COMPONENT state
          previousState  = COMPONENT;

          // ***************
          // * <component> *
          // ***************

          // process attributes
          // it MUST have a source file!
          componentFilename = attrs.getValue("source");

          if (componentFilename == null)
            throw new SAXException("source attribute is missing in <component>");

          // pick up the ref and label attributes
          componentRef = attrs.getValue("ref");
          if (componentRef == null)
            componentRef = EMPTYSTRING;

          // enter filename into refmap
          if (!refMap.containsKey(componentFilename)) {
            // add the key value pair
            refMap.put(componentFilename, componentRef);
          }
        }
        else
          throw new SAXException("Illegal element " + localName + " encountered when expecting a <component>");
      break;

      case 2/*COMPONENT*/:
        // transition to mapping possible
        if ((level == 3) && localName.equals("mapping")) {
          // move to COMPONENT state
          previousState = MAPPING;

          // *************
          // * <mapping> *
          // *************

          // get mapping details
          String refStg = attrs.getValue("ref");
          if (refStg == null) refStg = EMPTYSTRING;

          // get source start and end coordinates
          String srcStartStg = attrs.getValue("src_start");
          String srcEndStg = attrs.getValue("src_end");
          String dstStartStg = attrs.getValue("dst_start");
          String dstEndStg = attrs.getValue("dst_end");
          String direction = attrs.getValue("sense");

          // validation
          if (srcStartStg == null || srcEndStg == null
             || dstStartStg == null || dstEndStg == null || direction == null)
             throw new SAXException("one or more attributes missing from <mapping>");          

          int srcStart = (new Integer(srcStartStg)).intValue();
          int srcEnd = (new Integer(srcEndStg)).intValue();
          int dstStart = (new Integer(dstStartStg)).intValue();
          int dstEnd = (new Integer(dstEndStg)).intValue();
          if ((srcStart > srcEnd) || (dstStart > dstEnd)) throw new SAXException("illegal sequence coordinates!");
 
          // create the location objects
          RangeLocation srcLoc = new RangeLocation(srcStart, srcEnd);
          RangeLocation dstLoc = new RangeLocation(dstStart, dstEnd);
 
          // update destination length
          dstLength = Math.max(dstLength, dstEnd);
 
          StrandedFeature.Strand strand;
          if (direction.equals("SAME"))
            strand = StrandedFeature.POSITIVE;
          else if (direction.equals("REVERSED"))
            strand = StrandedFeature.NEGATIVE;
          else
            throw new SAXException("illegal value for direction attribute");
 
          // create the mapping object and add to list
//        System.out.println("adding " + srcFilename + srcLoc + dstLoc + strand);
          map.add(new MapElement(refStg.trim(), componentFilename, srcLoc, dstLoc, strand));
        }
        else
          throw new SAXException("Illegal element " + localName + " encountered when expecting a <mapping>"); 
      break;

      case 3 /*MAPPING*/:
        // you can't go deeper than mapping
        throw  new SAXException("Illegal attempt to nest element in <mapping>");

      default:
        throw new SAXException("Catastrophic parse failure!");
    } 
  }  

  public void endElement(String nsURI,
                         String localName,
                         String qName,
                         StAXContentHandler delegate)
    throws SAXException
  {
    // check exits
    switch (previousState) {
      case 0 /*INIT*/:
        if (level != 0) throw new SAXException("Parse error in endElement " + level + " " + localName);
      break;

      case 1 /*RAGBAGMAP*/:
        if (level != 1) 
          throw new SAXException("Parse error in endElement " + level + " " + localName);
        else
          previousState = INIT;
      break;

      case 2 /*COMPONENT*/:
        if (level != 2) 
          throw new SAXException("Parse error in endElement " + level + " " + localName);
        else
          previousState = RAGBAGMAP;
      break;

      case 3 /*MAPPING*/:
        if (level != 3) 
          throw new SAXException("Parse error in endElement " + level + " " + localName);
        else
          previousState = COMPONENT;
      break;

      default:
        throw new SAXException("Parse error in endElement");
    }

    // record level exit
    level--;    
  }
}









/*
  public void startElement(
                String nsURI, 
                String localName, 
                String qName, 
                Attributes attrs, 
                DelegationManager dm) 
    throws SAXException
  {
    // the outer element MUST be a ragbag_map
    if (level == 0) {
       if (localName.equals("ragbag_map")) {
         level++;
         return;
       }
       else
          throw new SAXException("Ragbag map file does not start with a <ragbag_map> element");
    }

    // check nesting
    if (level != 1)
      throw new SAXException("This level of nesting is impossible!");
    level++;

    // the element has one or more <mapping> elements
    if (!(localName.equals("mapping")))
      throw new SAXException("Only mapping elements are permitted within a <ragbag_map> element");

    // all seems kosher, process attributes. 
    // get source: do I need full pathnames?
    String srcFilename = attrs.getValue("source");
    if (srcFilename == null)
      throw new SAXException("source missing in <mapping> in Map.");       

    // pick up reference
    String refStg = attrs.getValue("ref");
    if (refStg == null)
      throw new SAXException("ref missing in <mapping> in Map.");       

    // get source start and end coordinates
    String srcStartStg = attrs.getValue("src_start");
    String srcEndStg = attrs.getValue("src_end");
    String dstStartStg = attrs.getValue("dst_start");
    String dstEndStg = attrs.getValue("dst_end");
    String direction = attrs.getValue("sense");

    // validation
    if (srcStartStg == null || srcEndStg == null
        || dstStartStg == null || dstEndStg == null || direction == null)
      throw new SAXException("one or more attributes missing from <mapping>");

    int srcStart = (new Integer(srcStartStg)).intValue();
    int srcEnd = (new Integer(srcEndStg)).intValue();   
    int dstStart = (new Integer(dstStartStg)).intValue();
    int dstEnd = (new Integer(dstEndStg)).intValue();
    if ((srcStart > srcEnd) || (dstStart > dstEnd)) throw new SAXException("illegal sequence coordinates!");

    // create the location objects
    RangeLocation srcLoc = new RangeLocation(srcStart, srcEnd);
    RangeLocation dstLoc = new RangeLocation(dstStart, dstEnd);

    // update destination length
    dstLength = Math.max(dstLength, dstEnd);

    StrandedFeature.Strand strand;
    if (direction.equals("SAME")) 
       strand = StrandedFeature.POSITIVE;
    else if (direction.equals("REVERSED")) 
       strand = StrandedFeature.NEGATIVE;
    else 
       throw new SAXException("illegal value for direction attribute");
    

    // create the mapping object and add to list
//    System.out.println("adding " + srcFilename + srcLoc + dstLoc + strand);
    map.add(new MapElement(refStg.trim(), srcFilename, srcLoc, dstLoc, strand));     
  }  


  public void endElement(String nsURI, 
                         String localName, 
                         String qName, 
                         StAXContentHandler delegate)
  {
    // record level exit
    level--;
  }
}
*/
