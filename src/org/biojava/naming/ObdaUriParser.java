package org.biojava.naming;

import javax.naming.Name;
import javax.naming.CompoundName;
import javax.naming.NamingException;
import javax.naming.NameParser;
import java.util.Properties;

/**
 * Singleton class for parsers that understand OBDA URIs.
 *
 * @author Matthew Pocock
 */
public class ObdaUriParser
        implements NameParser
{
  private static final Properties syntax;
  private static final ObdaUriParser INSTANCE;

  static
  {
    syntax = new Properties();
    syntax.put("jndi.syntax.direction", "left_to_right");
    syntax.put("jndi.syntax.separator", ":");
    syntax.put("jndi.syntax.ignorecase", "true");

    INSTANCE = new ObdaUriParser();
  }

  public static ObdaUriParser getInstance()
  {
    return INSTANCE;
  }

  private ObdaUriParser()
  {
    // only we should make one
  }

  public Name parse(String name)
          throws NamingException
  {
    return new CompoundName(name, syntax);
  }
}
