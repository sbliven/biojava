package org.biojava.bio.program.tagvalue;

/**
 * <p>
 * A simple listener that just echoes events back to the console.
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.3
 */
public class Echo implements TagValueListener {
  private int depth = 0;
  private String indent = "";
  
  public void startRecord() {
    System.out.println(depth + indent + "[");
    indent();
  }
  
  public void endRecord() {
    outdent();
    System.out.println(depth + indent + "]");
  }
  
  public void startTag(Object tag) {
    System.out.println(depth + indent + tag + " {");
    indent();
  }
  
  public void endTag() {
    outdent();
    System.out.println(depth + indent + "}");
  }
  
  private void indent() {
    depth += 1;
    indent += "  ";
  }
  
  private void outdent() {
    indent = ""; 
    depth--;
    for(int i = 0; i < depth; i++) {
      indent += "  ";
    }
  }
  
  public void value(TagValueContext tvc, Object value) {
    System.out.println(depth + indent + value);
  }
}

