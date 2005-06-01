
import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.Tag;

import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Option implements Taglet {
  public static void register(Map tagletMap)
  {
    registerOption(tagletMap, new Option("biojava.option", "Option"));
    registerOption(tagletMap, new Option("biojava.argument", "Argument"));
  }

  private static void registerOption(Map tagletMap, Option instance)
  {
    Taglet t = (Taglet) tagletMap.get(instance.getName());
    if(t != null) {
      tagletMap.remove(instance.getName());
    }
    tagletMap.put(instance.getName(), new com.sun.tools.doclets.internal.toolkit.taglets.LegacyTaglet(instance));
  }

  private String name;
  private String headline;

  public Option(String name, String headline) {
    this.name = name;
    this.headline = headline;
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
    return false;
  }

  public String getName()
  {
    return name;
  }

  public String getHeadline() {
    return headline;
  }

  public String toString(Tag tag)
  {
    return toString(new Tag[] { tag });
  }

  public String toString(Tag[] tags)
  {
    if(tags.length == 0) {
      return null;
    } else {
      StringBuffer sb = new StringBuffer();

      sb.append("<dt><b>");
      sb.append(getHeadline());
      sb.append(":</b></dt><dd>");
      sb.append("<table>");
      for(int i = 0; i < tags.length; ++i) {
        sb.append("<tr><td><tt>");
        String text = tags[i].text();
        int lineBreak = text.indexOf('\n');
        String first = text.substring(0, lineBreak).trim();
        String rest = text.substring(lineBreak + 1);

        StringTokenizer sTok = new StringTokenizer(first);
        if(sTok.hasMoreTokens()) {
          sb.append(sTok.nextToken());
        }
        while(sTok.hasMoreTokens()) {
          sb.append(", ");
          sb.append(sTok.nextToken());
        }

        sb.append("</tt></td><td>");
        sb.append(rest);
        sb.append("</td></tr>");
      }
      sb.append("</table>");
      sb.append("</dd>");
      return sb.toString();
    }
  }
}
