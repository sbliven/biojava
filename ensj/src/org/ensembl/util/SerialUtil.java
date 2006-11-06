/*
Copyright (C) 2004 EBI, GRL

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerialUtil {

    /**
     * Write an object to a file.
     * 
     * @param o The object to be written.
     * @param s The name of the file to write.
     */
  public static void writeObject(Object o, String s) {
    writeObject(o, new File(s));
  }
  
  public static void writeObject(Object o, File f) {

        try {

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));

            oos.writeObject(o);

            oos.close();

        } catch (FileNotFoundException e) {

            System.err.println("Cannot find " + f);
            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    // -------------------------------------------------------------------------
    /**
     * Read an object from a previously serialised file.
     * 
     * @param s The file name to read from. 
     */
    public static Object readObject(String s) {
      
      return readObject(new File(s));
    }
    
    public static Object readObject(File f) {

        Object o = null;
        
        try {

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));

             o = ois.readObject();

            ois.close();

        } catch (FileNotFoundException e) {

            System.err.println("Cannot find " + f);
            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (ClassNotFoundException e) {

            e.printStackTrace();

        }
        
        return o;
        
    }
    
    // -------------------------------------------------------------------------
    
}