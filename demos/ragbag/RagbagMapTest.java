
import java.io.*;
import java.util.Enumeration;

import org.xml.sax.*;
import org.biojava.utils.stax.*;
import org.apache.xerces.parsers.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.ragbag.*;

public class RagbagMapTest {
    public static void main(String[] args) 
        throws Exception
    {
      // create File referring to Map
      File mapFile = new File("Map");

      // create map object
      RagbagMap mapObj = new RagbagMap(mapFile);

      // parse the file
      mapObj.parse();

      // get iterator for testfile fake2.game
      Enumeration mapObjEnum = mapObj.getEnumeration("fake2.game");

      // print all entries related to fake2.game
      while (mapObjEnum.hasMoreElements()) {
        // get the object
        RagbagMap.MapElement thisElem = (RagbagMap.MapElement) mapObjEnum.nextElement();
        System.out.println("Dumping " + thisElem.getFilename() + thisElem.getSrcLocation()
                           + thisElem.getDstLocation() + thisElem.getStrand());
      }
    }
}
