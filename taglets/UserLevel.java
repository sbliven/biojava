import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Taglet providing the for.developer, for.powerUser and for.User tags.
 *
 * @author Matthew Pocock
 */
public class UserLevel implements Taglet {
  public static void register(Map tagletMap) {
    register(tagletMap, "for.user", "general use");  
    register(tagletMap, "for.powerUser", "advanced users");
    register(tagletMap, "for.developer", "developers");
  }
  
  private static void register(Map tagletMap, String name, String message) {
    Taglet t = (Taglet) tagletMap.get(name);
    if(t != null) {
      tagletMap.remove(name);
    }
    tagletMap.put(name, new UserLevel(name, message));
  }
  
  private final String name;
  private final String message;
  
  public UserLevel(String name, String message) {
    this.name = name;
    this.message = message;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean inField() {
    return true;
  }
  
  public boolean inConstructor() {
    return true;
  }
  
  public boolean inMethod() {
    return true;
  }
  
  public boolean inOverview() {
    return true;
  }
  
  public boolean inPackage() {
    return true;
  }
  
  public boolean inType() {
    return true;
  }
  
  public boolean isInlineTag() {
    return false;
  }
  
  public String toString(Tag tag) {
    return toString(new Tag[] { tag });
  }
  
  public String toString(Tag[] tags) {
    if(tags.length == 0) {
      return null;
    } else {
      StringBuffer sb = new StringBuffer();
      
      sb.append("<dt><b>For " + message + ":</b></dt><dd>");
      for (int i = 0; i < tags.length; ++i) {
          sb.append(tags[i].text());
          if (i < tags.length - 1) {
              sb.append("<br /><br />");
          }
      }
      sb.append("</dd>");
      return sb.toString();
    }
  }
}
