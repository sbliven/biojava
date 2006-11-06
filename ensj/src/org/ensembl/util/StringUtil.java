/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * String Utility methods.
 * 
 * @version $Revision$
 */

public class StringUtil {

  /**
   * Comparator which orders strings by length.
   */
  public final static Comparator LENGTH_ORDER =
    new AscendingStringLengthComparator();

  /**
   * Converts string to int. In addition to handling 'normal' integers such 23452 it understands:
   * <ul>
   * <li>postfixes "m", "M", "mb", "MB" and "k", "kb", "K", "KB" e.g. 11m, 11.5mb, 11k.
   * <li>commas e.g. 3,000 and 11,000,000
   * </ul>
   */
  public static int parseInt(String value) throws ParseException {

    String v = value.toLowerCase();
    int y = 1;

    boolean m = v.endsWith("m");
    boolean mb = v.endsWith("mb");
    boolean k = v.endsWith("k");
    boolean kb = v.endsWith("kb");

    if (m || mb)
      y = 1000000;
    else if (k || kb)
      y = 1000;

    if (m || k)
      v = v.substring(0, v.length() - 1);
    if (mb || kb)
      v = v.substring(0, v.length() - 2);

    // double handles numbers like 2.5m
    final double x = NumberFormat.getNumberInstance().parse(v).doubleValue();
    final int z = (int) (x * y);

    return z;
  }

  public static String formatForPrinting(String string) {

    int offset = 2;
    int indent = -offset;

    StringBuffer buf = new StringBuffer();
    buf.append("\n");

    StringTokenizer tokenizer = new StringTokenizer(string, "(){}[]", true);

    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (token.equals("(") || token.equals("{") || token.equals("[")) {
        indent += offset;
        continue;
      }

      if (token.equals(")") || token.equals("}") || token.equals("]")) {
        indent -= offset;
        continue;
      }

      // handle screwed up string values with imbalanced parentheses.
      if (indent < 0)
        indent = 0;

      //buf.append(token);
      StringTokenizer attributeTokens = new StringTokenizer(token, ",");
      while (attributeTokens.hasMoreTokens()) {
        String attributePair = attributeTokens.nextToken();
        for (int i = 0; i < indent; ++i)
          buf.append(' ');
        buf.append(attributePair.trim());
        if (attributeTokens.hasMoreTokens())
          buf.append("\n");
      }

      if (tokenizer.hasMoreTokens())
        buf.append("\n");
    }

    return buf.toString();
  }

  /**
   * Convenience method which automatically calls o.toString() and delegates formatting to sister method.
   */
  public static String formatForPrinting(Object o) {

    return formatForPrinting(o.toString());
  }

  private static class AscendingStringLengthComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      String s1 = (String) o1;
      int l1 = s1.length();

      String s2 = (String) o2;
      int l2 = s2.length();

      if (l1 > l2)
        return 1;
      else if (l1 < l2)
        return -1;
      else
        return 0;
    }

    public boolean equals(Object o1, Object o2) {

      return compare(o1, o2) == 0;
    }
  }

  public static String arrayToString(int[][] matrix) {

    final int nRows = matrix.length;
    final int nCols = matrix[0].length;

    StringBuffer matrixBuf = new StringBuffer();
    for (int n = 0; n < nRows; ++n) {
      for (int o = 0; o < nCols; ++o) {
        matrixBuf.append(n);
        matrixBuf.append(',');
        matrixBuf.append(o);
        matrixBuf.append('\t');
        matrixBuf.append(matrix[n][o]);
        matrixBuf.append('\n');
      }
    }

    return matrixBuf.toString();
  }

  /**
   * Convenience method for converting an array of longs into
   * a string of numbers separated by commas. 
   * @param array longs to be converted to a string
   * @return string of numbers separated by commas or an empty string
   * if the array is null, empty.
   */
  public static String toString(long[] array) {
    return toString(array, false);
  }

  /**
   * Convenience method for converting an array of longs into
   * a string of numbers separated by commas.
   * @param array longs to be converted to a string
   * @param ignoreZeroes whether zeros should be excluded from the string
   * @return string of numbers separated by commas or an empty string
   * if the array is null, empty or full of zeroes and ignoreZeroes=true.
   */
  public static String toString(long[] array, boolean ignoreZeroes) {

    if (array == null)
      return "";

    StringBuffer buf = new StringBuffer();
    final int len = array.length;
    boolean comma = false;
    for (int n = 0; n < len; ++n) {
      long l = array[n];
      if (l != 0) {
        if (comma)
          buf.append(", ");
        buf.append(l);
        comma = true;
      }
    }
    return buf.toString();
  }

  public static String toString(int[] array) {

    if (array == null)
      return "";

    StringBuffer buf = new StringBuffer();
    final int len = array.length;
    for (int n = 0; n < len; ++n) {
      buf.append(array[n]);
      if (n + 1 < len)
        buf.append(", ");
    }
    return buf.toString();
  }

  public static String toString(String[] array) {

    if (array == null)
      return "";

    StringBuffer buf = new StringBuffer();
    final int len = array.length;
    for (int n = 0; n < len; ++n) {
      buf.append(array[n]);
      if (n + 1 < len)
        buf.append(", ");
    }
    return buf.toString();
  }

  public static String toString(Collection collection) {
    return toString(collection, ", ");
  }
  
  public static String toString(Collection collection, String separator) {

    if (collection == null)
      return "";

    Iterator i = collection.iterator();
    StringBuffer buf = new StringBuffer();
    while (i.hasNext()) {
      Object n = i.next();
      if (n==null)
        buf.append("null");
      else
        buf.append(n.toString());
      if (i.hasNext())
        buf.append(separator);
    }
    return buf.toString();
  }

  public static String toString(Object[] array) {
    return toString(array, ", ");
  }
  
  public static String toString(Object[] array, String separator) {

    if (array == null)
      return "";

    StringBuffer buf = new StringBuffer();
    final int len = array.length;
    for (int n = 0; n < len; ++n) {
      Object item = array[n];
      buf.append(item!=null ? item.toString() : null);
      if (n + 1 < len)
        buf.append(separator);
    }
    return buf.toString();
  }

  /**
   * @return index of the first digit fround in the string, -1 if none found.
   */
  public static int indexOfFirstDigit(String s) {

    char[] digits =
      new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    boolean digitFound = false;
    int index = Integer.MAX_VALUE;

    for (int i = 0; i < digits.length; ++i) {

      int tmp = s.indexOf((int) digits[i]);
      if (tmp != -1 && tmp < index) {
        index = tmp;
        digitFound = true;
      }
    }

    if (!digitFound)
      return -1;
    else
      return index;
  }

  /**
   * String comparison which handles null values. Basically the same as String.compare() except that "null" is a valid value which
   * comes before ANY string and two "nulls" are equal.
   */
  public static final int compare(final String str1, final String str2) {

    if (str1 == str2) {
      return 0;
    }
    if (str1 != null && str2 != null) {
      return str1.compareTo(str2);
    } else if (str1 == null && str2 != null) {
      return -1;
    } else {
      //str1!=null && str2==null
      return +1;
    }

  }

  /**
   * @return size as string or "null" if c==null.
   */
  public static String sizeOrUnset(Collection c) {

    if (c == null)
      return "unset";
    else
      return Integer.toString(c.size());
  }

  /**
   * @return s if not null else "unset".
   */
  public static String stringOrUnset(String s) {

    if (s == null)
      return "unset";
    else
      return s;
  }

  /**
   * @return "set" if not null, "unset" if null.
   */
  public static String setOrUnset(Object o) {

    if (o == null)
      return "unset";
    else
      return "set";
  }

  /**
   * Converts s into a string representation of at most 10 characters or "unset" if s is null.
   * 
   * @return unset of s==null, or first few chars followed by "...", e.g. briefOrUnset("aactctgaaattcg") = "aactctgaaa..."
   */
  public static String briefOrUnset(String s) {

    if (s == null)
      return "unset";
    else
      return s.substring(0, Math.min(10, s.length())) + "...";

  }

  /**
   * Capitalise first letter.
   */
  public static String capitaliseFirstLetter(String s) {

    String result = "";
    if (s.length() < 1) {
      return s;
    }
    result = s.substring(0, 1).toUpperCase() + s.substring(1);

    return result;
  }

  /**
   * Read a text file into a String.
   */
  public static String readTextFile(String f) {

    StringBuffer result = new StringBuffer();

    try {

      BufferedReader br = new BufferedReader(new FileReader(f));
      String line;
      while ((line = br.readLine()) != null) {

        result.append(line + "\n");

      }
      br.close();

    } catch (FileNotFoundException e) {
      System.err.println("Can't read " + f);
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("Error reading " + f);
      e.printStackTrace();
    }

    return result.toString();

  }

//-------------------------------------------------------------------------
	/**
	 * Check if a String is in an array of Strings. The whole array is searched
	 * (until a match is found); this is quite slow but does not require the array
	 * to be sorted in any way beforehand.
	 * 
	 * @param str
	 *          The String to search for.
	 * @param a
	 *          The array to search through.
	 * @param caseSensitive
	 *          If true, case sensitive searching is done.
	 * @return true if str is in a.
	 */
	public static boolean stringInArray(String str, String[] a, boolean caseSensitive) {

		boolean result = false;

		for (int i = 0; i < a.length; i++) {

			if (caseSensitive) {
				if (a[i].equals(str)) {
					result = true;
					break;
				}
			} else {
				if (a[i].equalsIgnoreCase(str)) {
					result = true;
					break;
				}
			}

		}

		return result;

	}

} // StringUtil
