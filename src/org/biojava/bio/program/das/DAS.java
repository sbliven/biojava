/*
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
 
package org.biojava.bio.program.das;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

public class DAS extends AbstractChangeable {
  private Map dataSources;
  
  {
    dataSources = new HashMap();
  }
  
  public static final ChangeType SERVERS = new ChangeType(
    "Das Servers changed", DAS.class, "SERVERS"
  );
  
  public void addDasURL(URL dasURL)
  throws BioException, ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(SERVERS);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(this, SERVERS);
        cs.firePreChangeEvent(ce);
        addDasURLImpl(dasURL);
	cs.firePostChangeEvent(ce);
      }
    } else {
      addDasURLImpl(dasURL);
    }
  }
  
  // fixme: can leave the object in an inconsistent state. Should be made into
  // an attomic operation
  private void addDasURLImpl(URL dasURL)
  throws BioException, ChangeVetoException {
    try {
      URL dsnURL = new URL(dasURL, "dsn");
      HttpURLConnection huc = (HttpURLConnection) dsnURL.openConnection();
      huc.connect();
      int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
      if(status == 0) {
        throw new BioException("Not a DAS server");
      } else if(status != 200) {
        throw new BioException("DAS error (status code = " + status + ")"); 
      }
      
      InputSource is = new InputSource(huc.getInputStream());
      DOMParser parser = DASSequence.nonvalidatingParser();
      parser.parse(is);
      NodeList nl = parser
        .getDocument()
        .getDocumentElement()
        .getElementsByTagName("DSN");
      
      for(int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        if(n instanceof Element) {
          Element dnsE = (Element) n;
          Element source = (Element) dnsE.getElementsByTagName("SOURCE").item(0);
          Element mapmaster = (Element) dnsE.getElementsByTagName("MAPMASTER").item(0);
          Element description = (Element) dnsE.getElementsByTagName("DESCRIPTION").item(0);
          
          String sourceID = source.getAttribute("id");
          String sourceText = ((Text) source.getFirstChild()).getData();
          URL mapURL = new URL(((Text) mapmaster.getFirstChild()).getData());
          String descrText = ((Text) description.getFirstChild()).getData();
          
          URL dsURL = new URL(dasURL, sourceID + "/");
          ReferenceServer master = (ReferenceServer) dataSources.get(mapURL);
          
          if(dsURL.equals(mapURL)) { // reference server URL
            ReferenceServer ds = new ReferenceServer(dsURL, sourceText, descrText);
            if(master != null) { // merge this entry with old stub entry
              for(Iterator ai = master.getAnnotaters().iterator(); ai.hasNext(); ) {
                ds.addAnnotator( (DataSource) ai.next() );
              }
            }
            dataSources.put(dsURL, ds);
          } else { // annotation server
            if(master == null) {
              master = new ReferenceServer(mapURL, null, null);
              dataSources.put(mapURL, master);
            }
            DataSource ds = new DataSource(dsURL, mapURL, sourceText, descrText);
            master.addAnnotator(ds);
          }
        }
      }
    } catch (MalformedURLException me) {
      throw new BioException(me, "Can't build DAS url");
    } catch (IOException ioe) {
      throw new BioException(ioe, "Can't process URL connection");
    } catch (SAXException se) {
      throw new BioException(se, "Can't parse XML document");
    }
  }
  
  // Set of all ReferenceServer objects known by this das
  public Set getReferenceServers() {
    return Collections.unmodifiableSet(new HashSet(dataSources.values()));
  }
}
