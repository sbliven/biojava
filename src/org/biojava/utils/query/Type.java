package org.biojava.utils.query;

import java.util.*;
import java.lang.reflect.*;

/**
 * An n'tuple.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Type {
  String getName();
  boolean isAssignableFrom(Type type);
  boolean isInstance(Object o);
}
