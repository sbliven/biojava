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
    if(args.length != 4) {
      throw new Exception("Use: AceClient host port user password");
    }
    
    try {
      String host = args[0];
      String port = args[1];
      String user = args[2];
      String passwd = args[3];
      DatabaseManager.registerDriver(new SocketDriver());
      URL dbURL = new URL("acedb://" + host + ":" + port + "/");
      System.out.println("Connecting to " + dbURL);
      Database sacchDB = DatabaseManager.getDatabase(dbURL, user, passwd);
      Connection aceCon = sacchDB.getConnection();
      System.out.println("Connected:");
      BufferedReader in = new BufferedReader(
        new InputStreamReader(System.in)
      );
    
      for(
        String command = in.readLine();
        command != null;
        command = in.readLine()
      ) {
        if(command.trim().equals("bye")) {
          aceCon.dispose();
          break;
        } else {
          String response = aceCon.transact(command);
          System.out.println(response);
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
