package org.biojava.bio.program.tagValue;

import java.io.*;
import java.util.*;

import org.biojava.utils.ParserException;

/**
 * Format processor for the family of files that resemble Genbank.
 *
 * @author Matthew Pocock
 */
public class GenbankPirFormatParser
  implements
    TagValueParser
{
  public boolean read(BufferedReader reader, TagValueListener tvListener)
    throws
      ParserException,
      IOException
  {
    String line;
    String tag = null;
    tvListener.startRecord();
    
    for(
      line = reader.readLine();
      line != null;
      line = reader.readLine()
    ) {
      if(line.startsWith("///")) {
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
        
        return !eof;
      } else {
        if(line.length() >= 12) {
          String newTag = line.substring(0, 12).trim();
          String value = null;
          if(line.length() > 12) {
            value = line.substring(12);
          }
          if(newTag.length() > 0) {
            tag = newTag;
          }
          tvListener.tagValue(tag, value);
        }
      }
    }
    
    throw new IOException("Premature end of stream or missing end tag '//'");
  }
}
