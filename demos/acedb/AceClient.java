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
