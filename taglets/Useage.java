
import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ClassDoc;

import java.util.Map;

/**
 * Tag for emitting messages like: "Use: Foo -xyz [option] args1".
 *
 * @author Matthew Pocock
 */
public class Useage implements Taglet {
  public static void register(Map tagletMap)
  {
    Useage instance = new Useage();
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
    return false;
  }

  public String getName()
  {
    return "biojava.use";
  }

  public String toString(Tag tag)
  {
    return toString(new Tag[]{tag});
  }

  public String toString(Tag[] tags)
  {
    if(tags.length == 0) {
      return null;
    } else {
      StringBuffer sb = new StringBuffer();

      sb.append("<dt><b>Useage:</b></dt><dd><table>");
      for(int i = 0; i < tags.length; ++i) {
        sb.append("<tr><td><tt>java ");
        sb.append(((ClassDoc) tags[0].holder()).qualifiedTypeName());
        sb.append("</tt></td><td><tt>");
        sb.append(tags[i].text());
        sb.append("</tt></td></tr>");
      }
      sb.append("</table></dd>");
      return sb.toString();
    }
  }
}
