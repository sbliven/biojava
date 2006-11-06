/*
 Copyright (C) 2003 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Convenience class offering methods for easily accessing property files stored as files available via file system or URL. Makes it
 * easy to extract single property values or all of them as a property object.
 */
public class PropertiesUtil {

    /**
     * Creates property object from propertyFiles. Firstly attempts to open each propertyFile as a file, secondly attempts to open
     * it as a resource (available via CLASSPATH).
     * 
     * @return property object corresponding to contents of files (empty if can't open files or files are empty).
     */
    public static Properties createProperties(String[] propertyFiles) {

        Properties p = new Properties();
        for (int i = 0; i < propertyFiles.length; ++i) {
            String fileName = propertyFiles[i];
            if (fileName != null) {
                Properties p1 = createProperties(fileName);
                if (p1 != null) p.putAll(p1);
            } else
                System.err.println("WARNING: PropertiesUtil.createProperties(String[]): ignoring null filename");
        }
        return p;

    }

    /**
     * Creates property object from propertyFile(s). Firstly attempts to open property as a file, secondly attempts to open it as a
     * resource (available via CLASSPATH).
     * 
     * @param propertyFile list of 1 or more files separated by commas.
     * @return property object corresponding to file, or null if can't open file.
     */
    public static Properties createProperties(String propertyFile) {

        if (propertyFile == null) return null;

        if (propertyFile.indexOf(",") > 1) {

            StringTokenizer tokens = new StringTokenizer(propertyFile, ",");
            final int nTokens = tokens.countTokens();
            String[] files = new String[nTokens];
            for (int t = 0; t < nTokens; ++t)
                files[t] = tokens.nextToken();

            return createProperties(files);
        }

        URL url = stringToURL(propertyFile);

        if (url == null) {
            System.err.println("WARNING: PropertiesUtil.createProperties(String): Failed to load properties from file:"
                    + propertyFile);
            return null;
        } else {
            return createProperties(url);
        }
    }

    
    /**
     * Attempts to convert file location into a URL corresponding to a file in the
     * local filesystem or in the classpath.
     * 
     * Simply returns new URL(file) if file is a valid URL already.
     * 
     * First looks for a File with that filepath, then looks in the classpath.
     * 
     * @param file file location. e.g. resources/data/file.txt, file:/home/bob/file.txt
     * @return URL corresponding to file if it exists, otherwise null.
     */
    public static URL stringToURL(String file) {
      
      URL url = null;
      try {
        // file might be specified as a URL already
        URL tmp = new URL(file);
        tmp.openStream().read();
        return tmp;
      } catch (MalformedURLException e1) {
        // do nothing
      } catch (IOException e1) {
        // do nothing
      }
      
      File f = new File(file);
      if (f.exists()) {
          try {
              url = f.toURL();
          } catch (MalformedURLException e) {
              e.printStackTrace();
              url = null;
          }
      }

      if (url == null) 
          url = PropertiesUtil.class.getClassLoader().getResource(file);
      
      return url;
    }
    
    /**
     * Creates property object from url.
     * 
     * @return property object corresponding to file, or null if can't open resource.
     */
    public static Properties createProperties(URL url) {

        Properties p = new Properties();
        try {
        		InputStream is = url.openStream(); 
            p.load(is);
            is.close();
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return value corresponding to key from file, null if can't open file or key not present.
     */
    public static String getProperty(String propertyFile, String key) {

        Properties p = createProperties(propertyFile);
        if (p == null) return null;
        return p.getProperty(key);
    }

    /**
     * @return value corresponding to key from url, null if can't open file or key not present.
     */
    public static String getProperty(URL url, String key) {

        Properties p = createProperties(url);
        if (p == null) return null;
        return p.getProperty(key);
    }

    /**
     * @return true if property value is is "true" (case insensitive), false if value is not "true", _default_ if proerty not set.
     */
    public static boolean booleanValue(Properties properties, String propertyName, boolean defaultValue) {

        String v = properties.getProperty(propertyName);
        if (v == null) return defaultValue;
        if (v.equalsIgnoreCase("true"))
            return true;
        else
            return false;
    }

    /**
     * Sorts pairs by key name and prints one name = value pair per line.
     */
    public static String toString(Properties p) {

        StringBuffer buf = new StringBuffer();
        ArrayList keys = new ArrayList(p.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); ++i) {
            String key = (String) keys.get(i);
            buf.append(key).append(" = ").append(p.getProperty(key)).append("\n");
        }

        return buf.toString();
    }

    /**
     * Filters the input set of properties only allowing those with keys that begin with _prefix_.
     * 
     * @param prefix key prefix to filter on.
     * @param in input properties
     * @return new properties object containing a subset of _in_ matching the condition.
     */
    public static Properties filterOnPrefix(String prefix, Properties in) {

        Properties out = new Properties();

        Enumeration keys = in.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith(prefix)) out.put(key, in.getProperty(key));
        }

        return out;
    }

    /**
     * Removes "prefix\\." from the beginning of all keys in _src_.
     * 
     * @param prefix prefix to be removed from keys. Should be a regular
     * expression as defined in java.util.regexp.Pattern. 
     * @param src input properties
     * @return new properties object that contains all the pairs from _in_
     * but with the prefix removed from any keys that start with it.
     * @see #removePrefixFromKeys(Properties, String, String)
     */
    public static Properties removePrefixFromKeys(Properties src, String prefix) {
    return removePrefixFromKeys(src, prefix, "\\.");
    }
    
    /**
     * Removes _prefix_+separator from the beginning of all keys in _src_.
     *  
     * @param src input properties
     * @param prefix prefix to be removed from keys. Should be a regular
     * expression as defined in java.util.regexp.Pattern. 
     * @param separator separator between prefix and key. Should be a regular
     * expression.
     *  * @return new properties object that contains all the pairs from _in_
     * but with the prefix removed from any keys that start with it.
     */
    public static Properties removePrefixFromKeys(Properties src, String prefix, String separator) {
      
      Properties p = new Properties();
      p.putAll(src);
      
      Pattern pn = Pattern.compile("^"+prefix+separator+"(.+)");
      for(Iterator iter=src.entrySet().iterator(); iter.hasNext();) {
        Map.Entry e = (Entry) iter.next();
        String key = (String) e.getKey();
        Matcher m = pn.matcher(key);
        if (m.matches()) {
          String newKey = m.group(1);
          p.put(newKey, e.getValue());
          p.remove(key);
        }
      }
      return p;
      
      }

    
    /**
     * Copy property from src to tgt if not null.
     * @param src source properties.
     * @param key key of property to be copied.
     * @param tgt target properties.
     */
    public static void copyProperty(Properties src, String key, Properties tgt) {
      copyProperty(src, key, tgt, null);
    }
    
    /**
     * Copy property from src to tgt if not null.
     * @param src source properties.
     * @param key key of property to be copied.
     * @param tgt target properties.
     * @param defaultValue defaultValue for property, used if src.getProperty(key)==null
     * and ignored if null.
     */
    public static void copyProperty(Properties src, String key, Properties tgt, String defaultValue) {
      String value = src.getProperty(key);
      if (value!=null)
        tgt.put(key, value);
      else if (defaultValue!=null)
        tgt.put(key, defaultValue);
    }
    
    /**
     * Write some properties to file. Optionally filter on property name.
     * 
     * @param props The properties to write.
     * @param fileName The file to write to.
     * @param pat If not null, only properties whose names match this string will be written.
     */
    public static void writeToFile(Properties props, String fileName, String pat) {

        try {

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName));
            Enumeration en = props.propertyNames();
            
            while (en.hasMoreElements()) {
                String name = (String) en.nextElement();
                String value = (String) props.get(name);
                if (pat == null || name.indexOf(pat) >= 0) {
                    writer.write(name + "=" + value + "\n");
                }
            }

            writer.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    /**
     * Convenience method for creating a properties object from a string.
     * 
     * @param string string to be converted into a properties object. 
     * Format of string is name=value pairs separated by _separatorRegexp_.
     * Can be null. 
     * e.g. "a=1:b=2".
     * @param separatorRegexp separator regular expression, e.g. ":", ";"
     * @return properties object representation of the string or null if _string_ is null.
     */
    public static Properties stringToProperties(String string, String separatorRegexp) {
      
      if (string==null) 
        return null;
      
      Properties p = new Properties();
      String[] properties = string.split(separatorRegexp);
      for (int i = 0; i < properties.length; i++) {
        String[] name2Value = properties[i].split("=");
        p.put(name2Value[0],name2Value[1]);
      }
      return p;
    }

}// PropertyUtil
