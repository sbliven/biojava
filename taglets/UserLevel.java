import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

public class UserLevel implements Taglet {
  public static void register(Map tagletMap) {
    register(tagletMap, "for.developer", "Developers");
    register(tagletMap, "for.powerUser", "Power users");
    register(tagletMap, "for.user", "Users");
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
      
      sb.append("<dt></dt><dd><table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n");
      sb.append("<tr BGCOLOR=\"#EEEEFF\" CLASS=\"TableSubHeadingColor\">\n");
      sb.append("<td><b>Intended for:</b> " + message + "</td>\n");
      sb.append("</tr>\n");
      for(int i = 0; i < tags.length; i++) {
        String text = tags[i].text();
        if(text.length() > 0) {
          sb.append("<tr BGCOLOR=\"white\" CLASS=\"TableRowColor\"><td>" + text + "</td></tr>\n");
        }
      }
      sb.append("</table></dd>\n");
      
      return sb.toString();
    }
  }
}
