import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

public class UserLevel implements Taglet {
  public static void register(Map tagletMap) {
    register(tagletMap, "developer", "Developers");
    register(tagletMap, "powerUser", "Power users");
    register(tagletMap, "user", "Users");
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
    return "<DT><B>Intended for:</B>" + message + "</DT><DD>" + tag.text() + "</DD>\n";
  }
  
  public String toString(Tag[] tags) {
    if(tags.length == 0) {
      return null;
    } else {
      StringBuffer sb = new StringBuffer();
      
      sb.append("<DT><B>Intended for:</B> " + message + "</DT>");
      for(int i = 0; i < tags.length; i++) {
        sb.append("<DD>" + tags[i].text() + "</DD>");
      }
      sb.append("\n");
      
      return sb.toString();
    }
  }
}
