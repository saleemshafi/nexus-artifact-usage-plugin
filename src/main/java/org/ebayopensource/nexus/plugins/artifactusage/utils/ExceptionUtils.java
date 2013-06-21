package org.ebayopensource.nexus.plugins.artifactusage.utils;

import java.lang.reflect.InvocationTargetException;

/**
 * Misc. utility methods that work on exceptions.
 *
 * @author Ludovic Orban
 */
public class ExceptionUtils {

  public static Throwable getRootCause(Throwable t) {
    Throwable last = null;
    while (t != null && t != last) {
      last = t;
      t = t.getCause();
    }
    if (last instanceof InvocationTargetException) {
      last = ((InvocationTargetException)last).getTargetException();
    }
    return last;
  }
}
