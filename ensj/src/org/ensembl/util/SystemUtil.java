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

/**
 * Utility methods for looking at system settings and command line program.
 * 
 * <p>
 * Can be used from the command line to determine where resources, e.g. classes
 * will be loaded from. Delegates parameter to runtimeResourceLocation(String).
 * e.g.
 * <code>java org.ensembl.util.SystemUtil "org.w3c.dom.Node" </code>
 * </p>
 * @see #runtimeResourceLocation(String) for more information on command line parameters
 */

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import com.mysql.jdbc.Driver;


public class SystemUtil {

  public static class MemoryStatus{

    public final long total;
    public final long free;
    public final long used;

    public MemoryStatus(long total, long free, long used) {
        this.total = total;
        this.free = free;
        this.used = used;
    }
    
    
    public String toString() {
    	return "memory: total = " + total + ", free = " + free + ", used = "
				+ used;

    }

    public String toStringMb() {
	NumberFormat df = new DecimalFormat("###.#");
	long m = 1024 * 1024;
    	return "memory: total = " + df.format(((double)total)/m) + "Mb, free = " + df.format(((double)free)/m) + "Mb, used = " + df.format(((double)used)/m) + "Mb";

    }
    
    public MemoryStatus diff(MemoryStatus other) {
      return new MemoryStatus(total-other.total, free-other.free, used-other.used);
    }
  }
    
    
	/**
	 * Converts each element in the array to a string via element.toString() and
	 * creates a formated string containing all the strings.
	 * 
	 * Format:
	 * 
	 * <pre>
	 * 
	 *  0 = item0
	 *  1 = item2
	 *  3 = item3
	 *  ...
	 *  
	 * </pre>
	 */
	public static String toString(Object[] array) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			buf.append(i).append(" = ").append(array[i]).append("\n");
		}
		return buf.toString();
	}

	/**
	 * Converts array into a string representation.
	 * 
	 * String format: <i>index = item </i>
	 * 
	 * <pre>
	 * 
	 *  0 = item0
	 *  1 = item2
	 *  3 = item3
	 *  ...
	 *  
	 * </pre>
	 */
	public static String toString(int[] array) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			buf.append(i).append(" = ").append(array[i]).append("\n");
		}
		return buf.toString();
	}

	public static String getClasspath() {
		StringBuffer buf = new StringBuffer();

		StringTokenizer paths = new StringTokenizer(System.getProperty(
				"java.class.path", "."), ":");
		while (paths.hasMoreTokens())
			buf.append(paths.nextToken()).append("\n");

		return buf.toString();
	}

	/**
	 * Memory usage for JVM, useful for debugging.
	 * 
	 * Does not call garbage collector before determining memory
	 * usage.
	 * 
	 * @return string containing total and free memory usage for JVM.
	 */
	public static final MemoryStatus memoryStatus() {
	  return memoryStatus(false);
	}

	/**
	 * Memory usage for JVM, useful for debugging.
   *
	 * @param gc whether to call System.gc() before determining memory
	 * usage.
	 * 
	 * @return string containing total and free memory usage for JVM.
	 */
	public static final MemoryStatus memoryStatus(boolean gc) {
	  if (gc)
	    System.gc();
		Runtime rt = Runtime.getRuntime();
		long total = rt.totalMemory();
		long free = rt.freeMemory();
		long used = total - free;
		return new MemoryStatus(total, free, used);

	}
	
	public static String environmentDump() {
		return "Ensj classes loaded from (sample class shown): " + runtimeResourceLocation("org.ensembl.Example")
		+ "\nMySQL classes loaded from (sample class shown): "  + runtimeResourceLocation("com.mysql.jdbc.Driver")
		 + "\n\nCLASSPATH = " + SystemUtil.getClasspath().toString();
	}

	
	/**
	 * Attempts to resolve the src to a URL.
	 * 
	 * This is useful for checking where a class or resource
	 * file is being loaded from by the classloadeder. 
	 * <ul>e.g. src = ...
	 * <li>org.ensembl.Example</li>
	 * <li>org/ensembl/Example.class</li>
	 * <li>com.mysql.jdbc.Driver</li>
	 * </ul>
	 * 
	 * Performs some magic to try to resolve the name if necessary,
	 * e.g. for org.ensembl.Example "full names" like org/ensembl/Example.class
	 * and org\ensembl\Example.class will be tried.
	 * @param src src file path or qualified class name.
	 * @return resolved URL or null if none found.
	 */
	public static URL runtimeResourceLocation(String src) {
		
		URL url = null;
		
		// String.replaceAll gets confused if sep == "\\" 
		// (e.g. windows file separator) so we need to 'expand it'
		String[] variants = { src, 
				src + ".class",
				src.replaceAll("\\.", "\\\\"),
				src.replaceAll("\\.", "\\\\") + ".class",
				src.replaceAll("\\.", "/"),
				src.replaceAll("\\.", "/") + ".class",
				};
		for (int i = 0; url==null && i < variants.length; i++) 
			url = Driver.class.getClassLoader().getResource(variants[i]);
		
		
		return url;
	}

	public static void main(String[] args) {
		

		if (args.length > 0) {
			System.out.println(args[0] + " is loaded from path: " + runtimeResourceLocation(args[0]));
		}
		else {
			System.out.println(environmentDump());
		}
	}
}
