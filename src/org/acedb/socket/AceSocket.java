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

package org.acedb.socket;

import org.acedb.*;

import java.util.*;
import java.io.*;
import java.net.*;
import java.security.*;

/**
 * Low level interface to the ACeDB sockets server.
 *
 * @author Thomas Down
 */

class AceSocket implements Connection {
    private final static int OK_MAGIC = 0x12345678;
    
    private final static String MSGREQ = "ACESERV_MSGREQ";
    private final static String MSGDATA = "ACESERV_MSGDATA";
    private final static String MSGOK = "ACESERV_MSGOK";
    private final static String MSGENCORE = "ACESERV_MSGENCORE";
    private final static String MSGFAIL = "ACESERV_MSGFAIL";
    private final static String MSGKILL = "ACESERV_MSGKILL";

    private Socket sock;
    private DataInputStream dis;
    private DataOutputStream dos;

    private boolean pendingConfig = true;
    private int serverVersion = 0;
    private int clientId = 0;
    private int encore = 0;
    private int maxBytes = 0;

    public AceSocket(String host, int port, String user, String passwd) 
	throws AceException 
    {
	try {
	    sock = new Socket(host, port);
	    dis = new DataInputStream(sock.getInputStream());
	    dos = new DataOutputStream(sock.getOutputStream());
	
	    // System.out.println(transact("hello"));
	    String pad = transact("bonjour");
	    String userPasswd = md5Sum(user, passwd);
	    String token = md5Sum(userPasswd, pad);
	    transact(user + " " + token);
	    
	} catch (IOException ex) {
	    throw new AceException(ex);
	}
    }

    private int byteSwap(int i) {
	// return ((i & 0xff) << 24) | ((i & 0xff00) << 8) | ((i & 0xff0000) >>> 8) | ((i & 0xff000000) >>> 24);
	return i;
    }

    public String transact(String s) throws AceException {
	// System.err.println(">>> " + s);
	try {
	    writeMessage(MSGREQ, s);
	    String reply = readMessage();
	    // System.err.println("<<< " + reply);
	    return reply;
	} catch (IOException ex) {
	    throw new AceException(ex);
	}
    }

    private void writeMessage(String type, String s) throws IOException {
	// System.out.println("writeMessage: " + s);

	dos.writeInt(OK_MAGIC);
	dos.writeInt(s.length() + 1);
	dos.writeInt(serverVersion); // ???Server version???
	dos.writeInt(clientId); // clientId
	dos.writeInt(maxBytes); // maxBytes

	dos.writeBytes(type);

	byte[] padding = new byte[30 - type.length()];
	dos.write(padding, 0, padding.length);

	dos.writeBytes(s);
	dos.write(0);
	dos.flush();

	// System.out.println("writeMessageDone "+dos.size());
    }

    private String readMessage() throws IOException {
	int magic = dis.readInt();
	// System.out.println("readmagic = " + magic);
	int length = dis.readInt();
	// System.out.println("length = " + length);

	int rServerVersion = dis.readInt(); 
	// System.out.println("rServerVersion = " + rServerVersion);
	int rClientId = dis.readInt();
	// System.out.println("rClientId = " + rClientId);
	int rMaxBytes = dis.readInt();
	// System.out.println("rMaxBytes = " + rMaxBytes);
	byte[] typeb = new byte[30];
	dis.readFully(typeb);
	String type = new String(typeb);
	
	if (pendingConfig) {
	    serverVersion = rServerVersion;
	    clientId = rClientId;
	    maxBytes = rMaxBytes;
	    pendingConfig = false;
	}

	byte[] message = new byte[length-1];
	dis.readFully(message);
	dis.skipBytes(1);

	if (type.startsWith(MSGENCORE)) {
	    writeMessage(MSGREQ, "encore");
	    return (new String(message)) + readMessage();
	}
	return new String(message);
    }

    public void dispose() throws AceException {
	try {
	    sock.close();
	} catch (IOException ex) {
	    throw new AceException(ex);
	}
    }

    private static String md5Sum(String a, String b) {
	try {
	    MessageDigest md = MessageDigest.getInstance("MD5");

	    md.update(a.getBytes());
	    byte[] digest = md.digest(b.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < digest.length; ++i) {
		int bt = digest[i];
		sb.append(hexChar((bt >>> 4) & 0xf));
		sb.append(hexChar(bt & 0xf));
	    }
	    return sb.toString();
	} catch (NoSuchAlgorithmException ex) {
	    throw new AceError("BioJava access to ACeDB sockets require the MD5 hash algorithm.  Consult your Java Vendor.");
	}
    }

    private static char hexChar(int i) {
	if (i <= 9)
	    return (char) ('0' + i);
	else
	    return (char) ('a' + i-10);
    }

    public static void main(String[] args) {
	System.out.println(args[0] + " " + md5Sum(args[0], args[1]));
    }
}
