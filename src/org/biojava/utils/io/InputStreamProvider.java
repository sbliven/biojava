/*
 *                  BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 * 
 * Created on Dec 28, 2005
 *
 */
package org.biojava.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/** A class that provides an InputStream from a File. The file can be compressed or uncompressed. 
 * The file extension is checked to determine which compression is being used.
 *  
 * Currently supported
 * compressions:
 * <ul>
 * <li>Gzip (extension .gz)</li>
 * <li>Zip (extension .zip) in this case a stream to the first entry in the zip file is returned </li> 
 * <li>Jar (extension .jar) same as .Zip; only stream to first entry is returned </li>
 * <li>for any other extension, no compression is assumed </li>
 * </ul>
 * 
 * 
 * @author Andreas Prlic
 * @since 1.5
 * @version %I% %G%
 *
 */
public class InputStreamProvider {
    
    public InputStreamProvider() {
        super();
        
    }
    
    /** get an InputStream for this file 
     * 
     * @param pathToFile the path of the file.
     * @return
     */
    public InputStream getInputStream(String pathToFile)
    throws IOException
    {
        File f = new File(pathToFile);
        return getInputStream(f);
    }
    
    /** get an InputStream for the file
     * 
     * @param f
     * @return
     */
    public InputStream getInputStream(File f) 
    throws IOException
    {
        
        String fileName = f.getName();
        InputStream inputStream = null;
        
        if ( fileName.endsWith(".gz")) {
      
            FileInputStream is = new FileInputStream(f);
            inputStream = new GZIPInputStream(is);
            
        } 
        
        else if ( fileName.endsWith(".zip")){
            
          
            ZipFile zipfile = new ZipFile(f);
            
            // stream to first entry is returned ...
            ZipEntry entry;
            Enumeration e = zipfile.entries();
            if ( e.hasMoreElements()){
                entry = (ZipEntry) e.nextElement();
                inputStream = zipfile.getInputStream(entry);
                
            } else {
                throw new IOException ("Zip file has no entries");
            }
            
        } 
        
        else if ( fileName.endsWith(".jar")) {

            JarFile jarFile = new JarFile(f);
           
            // stream to first entry is returned
            JarEntry entry;
            Enumeration e = jarFile.entries();
            if ( e.hasMoreElements()){
                entry = (JarEntry) e.nextElement();
               
                inputStream = jarFile.getInputStream(entry);
            } else {
                throw new IOException ("Jar file has no entries");
            }
        }
        
        else {
           
            // no particular extension found, assume that it is an uncompressed file
            inputStream = new FileInputStream(f);
        }
        
        return inputStream;
    }
    
   
}
