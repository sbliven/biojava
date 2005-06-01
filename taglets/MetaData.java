import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.beans.*;
import java.io.StringBufferInputStream;
import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Display meta-data as a tree
 *
 * @author Matthew Pocock
 */
public class MetaData
implements Taglet {
  private static final String NAME = "metaData";

  public static void register(Map tagletMap) {
    register(tagletMap, NAME);
  }

  private static void register(Map tagletMap, String name) {
    Taglet t = (Taglet) tagletMap.get(name);
    if(t != null) {
      tagletMap.remove(name);
    }
    tagletMap.put(name, new com.sun.tools.doclets.internal.toolkit.taglets.LegacyTaglet(new MetaData()));
  }

  private final Set toStringable;
  private final Set excludeMethods;

  private MetaData() {
    toStringable = new HashSet();
    excludeMethods = new HashSet();

    toStringable.add(String.class);
    toStringable.add(Color.class);
    toStringable.add(Character.class);
    toStringable.add(Boolean.class);
    toStringable.add(Byte.class);
    toStringable.add(Short.class);
    toStringable.add(Integer.class);
    toStringable.add(Long.class);
    toStringable.add(Float.class);
    toStringable.add(Double.class);

    excludeMethods.add("getClass");
  }

  public boolean inConstructor() {
    return true;
  }

  public boolean inField() {
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

  public String getName() {
    return NAME;
  }

  public String toString(Tag tag) {
    StringBuffer sb = new StringBuffer();
    Set seen = new HashSet();

    String message = tag.text();
    System.err.println("Got tag: " + message);
    Pattern patt = Pattern.compile("(?s)(?m)\\s*(\\w+)\\s+(.*)");
    Matcher matcher = patt.matcher(message);
    if(!matcher.find()) {
      System.err.println("Could not match pattern");
      return "<dt><b>Could not match pattern: " + patt.pattern() + "</b></dt>";
    }
System.err.println("Name; " + matcher.group(1));
    System.err.println("Text: " + matcher.group(2));

    sb.append("<dt><b>" + matcher.group(1) + "</b></dt><dd>");

    XMLDecoder decoder = new XMLDecoder(new StringBufferInputStream(matcher.group(2)));
    Object val = decoder.readObject();

    try {
      dump(val, sb, seen);
    } catch (IntrospectionException e) {
      throw new Error(e);
    } catch (IllegalAccessException e) {
      throw new Error(e);
    } catch (InvocationTargetException e) {
      throw new Error(e);
    }

    sb.append("</dd>");

    return sb.toString();
  }

  public String toString(Tag[] tags) {
    if(tags.length == 0) {
      return null;
    }

    System.err.println("All tags: " + Arrays.asList(tags));
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < tags.length; i++) {
      sb.append(toString(tags[i]));
    }
    return sb.toString();
  }

  private void dump(Object val, StringBuffer sb, Set seen)
          throws IntrospectionException, IllegalAccessException, InvocationTargetException {
    System.err.println("Dumping: " + val);
    if(seen.contains(val)) {
      sb.append("Seen this before - discarding circular refference");
    } else if(val == null) {
      sb.append("NULL");
    } else if(toStringable.contains(val.getClass())) {
      sb.append(val.toString());
    } else {
      seen.add(val);
      sb.append(val.getClass() + "<table>");

      BeanInfo bInf = Introspector.getBeanInfo(val.getClass());
      PropertyDescriptor[] pds = bInf.getPropertyDescriptors();
      for(int i = 0; i < pds.length; i++) {
        PropertyDescriptor pd = pds[i];
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        if(readMethod == null) {
          continue;
        }

        if(excludeMethods.contains(readMethod.getName())) {
          continue;
        }
System.err.println("Following: " + name);

        sb.append("<tr><td>" + name + "</td>");
        sb.append("<td>");
        dump(readMethod.invoke(val, new Class[] {}), sb, seen);
        sb.append("</td>");
        sb.append("</tr>");
      }

      sb.append("</table>");
    }
  }
}
