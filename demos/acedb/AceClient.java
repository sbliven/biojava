/*
 *                    BioJava development code
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
 */
package acedb;

import java.io.*;
import java.net.*;
import java.util.*;

import org.acedb.*;
import org.acedb.socket.*;

/**
 * A simple command-line ACeDB client.
 * <P>
 * The client connects to an ACeDB socket server on a user-defined socket. The
 * client currently has no command-line history or auto-complete functionality.
 * These would be good things to add.
 * <P>
 * Use:<br>
 * <code>java AceClient host port user password</code>
 *
 * @author Matthew Pocock
 */
public class AceClient {
  public static void main(String [] args) throws Exception {
    if(args.length > 5) {
      throw new Exception("Use: AceClient host port user password [log]");
    }
    
    try {
      String host = args[0];
      String port = args[1];
      String user = args[2];
      String passwd = args[3];
      Writer log = null;
      if(args.length == 5) {
        log = new FileWriter(new File(args[4]));
      }
      Ace.registerDriver(new SocketDriver());
      URL _dbURL = new URL("acedb://" + host + ":" + port + "/");
      AceURL dbURL = new AceURL(_dbURL, user, passwd, null);
      System.out.println("Connecting to " + dbURL);
      Connection aceCon = Ace.getConnection(dbURL);
      System.out.println("Connected:");
      BufferedReader in = new BufferedReader(
        new InputStreamReader(System.in)
      );
    
      for(
        String command = in.readLine();
        command != null;
        command = in.readLine()
      ) {
        if(log != null) {
          log.write(command);
        }
        if(command.trim().equals("bye")) {
          aceCon.dispose();
          break;
        } else {
          String response = aceCon.transact(command);
          if(log != null) {
            log.write(response);
          }
          System.out.println(response);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
