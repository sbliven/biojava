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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ActivityListener;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A collection of DAS datasources.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class DAS extends AbstractChangeable {
    private static List activityListeners;
    private Map dataSources;
    private static boolean threadFetches = false;

    {
        dataSources = new HashMap();
    }

    static {
        activityListeners = new ArrayList();
        threadFetches = Boolean.getBoolean("org.biojava.bio.program.das.thread_fetches");
    }


    public static Set getTypes(URL dasURL) throws BioException {
        final Set types = new HashSet();
        try {
            TypesFetcher tf = new TypesFetcher(dasURL, null, null);
            tf.setNullSegmentHandler(new TypesListener() {
                    public void startSegment() {}
                    public void endSegment() {}
                    public void registerType(String type) { types.add(type); }
                    public void registerType(String type, int count) { types.add(type); }
                } );
            tf.runFetch();
        } catch (ParseException ex) {
            throw new BioException(ex);
        }
        return Collections.unmodifiableSet(types);
    }

    public static void setThreadFetches(boolean b) {
        threadFetches = b;
    }

    public static boolean getThreadFetches() {
        return threadFetches;
    }

    public static final ChangeType SERVERS = new ChangeType(
                 "Das Servers changed", DAS.class, "SERVERS"
                                                           );

    public static synchronized void addActivityListener(ActivityListener al) {
        activityListeners.add(al);
    }

    public static synchronized void removeActivityListener(ActivityListener al) {
        activityListeners.remove(al);
    }

    public static synchronized void startedActivity(Object source) {
        for (Iterator i = activityListeners.iterator(); i.hasNext(); ) {
            ActivityListener al = (ActivityListener) i.next();
            al.startedActivity(source);
        }
    }

    public static synchronized void completedActivity(Object source) {
        for (Iterator i = activityListeners.iterator(); i.hasNext(); ) {
            ActivityListener al = (ActivityListener) i.next();
            al.completedActivity(source);
        }
    }

    public static synchronized void activityProgress(Object source, int current, int target) {
        for (Iterator i = activityListeners.iterator(); i.hasNext(); ) {
            ActivityListener al = (ActivityListener) i.next();
            al.activityProgress(source, current, target);
        }
    }

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
  // an atomic operation
  private void addDasURLImpl(URL dasURL)
  throws BioException, ChangeVetoException {
    try {
      URL dsnURL = new URL(dasURL, "dsn");
      DAS.startedActivity(this);
      HttpURLConnection huc = (HttpURLConnection) dsnURL.openConnection();
      huc.connect();
      int status = DASSequenceDB.tolerantIntHeader(huc, "X-DAS-Status");
      if(status == 0) {
        throw new BioException("Not a DAS server: " + dsnURL);
      } else if(status != 200) {
        throw new BioException("DAS error (status code = " + status + ")");
      }

      InputSource is = new InputSource(huc.getInputStream());
      is.setSystemId(dsnURL.toString());
      DocumentBuilder parser = DASSequence.nonvalidatingParser();
      NodeList nl = parser
        .parse(is)
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
          String sourceText = ((Text) source.getFirstChild()).getData().trim();
          String mapURLString = ((Text) mapmaster.getFirstChild()).getData().trim();
          if (! (mapURLString.charAt(mapURLString.length() - 1) == '/')) {
              mapURLString = mapURLString + '/';
          }
          URL mapURL = new URL(mapURLString);
          String descrText = ((Text) description.getFirstChild()).getData().trim();

          URL dsURL = new URL(dasURL, sourceID + "/");
          ReferenceServer master = (ReferenceServer) dataSources.get(mapURL);

          if(dsURL.equals(mapURL)) { // reference server URL
            if(master != null) { // merge this entry with old stub entry
                master.setName(sourceText);
                master.setDescription(descrText);
            } else {
                ReferenceServer ds = new ReferenceServer(dsURL, sourceText, descrText);
                dataSources.put(dsURL, ds);
            }
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
      throw new BioException("Can't build DAS url",me);
    } catch (IOException ioe) {
      throw new BioException("Can't process URL connection",ioe);
    } catch (SAXException se) {
      throw new BioException("Can't parse XML document",se);
    } finally {
        DAS.completedActivity(this);
    }
  }

  // Set of all ReferenceServer objects known by this das
  public Set getReferenceServers() {
    return Collections.unmodifiableSet(new HashSet(dataSources.values()));
  }
}
