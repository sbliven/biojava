
import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.Tag;

import java.util.StringTokenizer;
import java.util.Map;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Values implements Taglet {
  public static void register(Map tagletMap)
  {
    Values instance = new Values();
    String name = instance.getName();

    Taglet t = (Taglet) tagletMap.get(name);
    if(t != null) {
      tagletMap.remove(name);
    }
    tagletMap.put(name, new com.sun.tools.doclets.internal.toolkit.taglets.LegacyTaglet(instance));
  }

  public boolean inConstructor()
  {
    return false;
  }

  public boolean inField()
  {
    return false;
  }

  public boolean inMethod()
  {
    return false;
  }

  public boolean inOverview()
  {
    return false;
  }

  public boolean inPackage()
  {
    return false;
  }

  public boolean inType()
  {
    return true;
  }

  public boolean isInlineTag()
  {
    return true;
  }

  public String getName()
  {
    return "biojava.values";
  }

  public String toString(Tag tag)
  {
    StringBuffer sb = new StringBuffer();

    StringTokenizer sTok = new StringTokenizer(tag.text());
    if(sTok.hasMoreTokens()) {
      sb.append(sTok.nextToken());
    }

    while(sTok.hasMoreTokens()) {
      sb.append("|");
      sb.append(sTok.nextToken());
    }

    return sb.toString();
  }

  public String toString(Tag[] tags)
  {
    return null;
  }
}
