package org.biojava.bio.program.indexdb;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;

class ConfigFile extends AbstractAnnotation {
  private File file;
  private Map map;
  
  public ConfigFile(File file)
  throws IOException {
    this.file = file;
    map = new HashMap();
    if(file.exists()) {
      parseFile();
    }
  }
  
  public void commit()
  throws BioException {
    try {
      writeFile();
    } catch (IOException e) {
      throw new BioException(e, "Couldn't commit");
    }
  }
  
  public void rollback() {
    try {
      parseFile();
    } catch (IOException e) {
      throw new BioError(e, "Couldn't roll back: your data may be invalid");
    }
  }
  
  private void parseFile()
  throws IOException {
    BufferedReader reader = new BufferedReader(
      new FileReader(
        file
      )
    );
    
    for(
      String line = reader.readLine();
      line != null;
      line = reader.readLine()
    ) {
      int tab = line.indexOf("\t");
      String key = line.substring(0, tab).trim();
      String value = line.substring(tab+1).trim();
      
      map.put(key, value);
    }
  }
  
  private void writeFile()
  throws IOException {
    PrintWriter writer = new PrintWriter(
      new FileWriter(
        file
      )
    );
    
    writer.println("index\t" + map.get("index"));
    
    for(Iterator i = map.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry me = (Map.Entry) i.next();
      if(!me.getKey().equals("index")) {
        writer.println(me.getKey() + "\t" + me.getValue());
      }
    }
    
    writer.flush();
  }

  
  protected Map getProperties() {
    return map;
  }
  
  protected boolean propertiesAllocated() {
    return true;
  }
}
