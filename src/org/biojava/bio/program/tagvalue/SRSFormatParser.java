package org.biojava.bio.program.tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.utils.ParserException;

/**
 * Format processor for the family of files
 */
public class SRSFormatParser
  implements
    TagValueParser
{
  public boolean read(BufferedReader reader, TagValueListener tvListener)
    throws
      ParserException,
      IOException
  {
    String line;
    
    tvListener.startRecord();
    
    for(
      line = reader.readLine();
      line != null;
      line = reader.readLine()
    ) {
      if(line.startsWith("//")) {
        boolean eof = false;
        
        while(true) {
          reader.mark(1);
          int c = reader.read();
          if(c == -1) {
            eof = true;
            break;
          }
          
          if(Character.isWhitespace((char) c)) {
            continue;
          }
          
          reader.reset();
          break;
        }
        
        tvListener.endRecord();
        
        return eof;
      } else {
        if(line.length() >= 2) {
          String tag = line.substring(0, 2);
          String value = null;
          if(line.length() > 5) {
            value = line.substring(5);
          }
          tvListener.tagValue(tag, value);
        }
      }
    }
    
    throw new IOException("Premature end of stream or missing end tag '//'");
  }
}
