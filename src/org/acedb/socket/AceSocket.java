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

/**
 * @author Thomas Down
 */

public class AceSocket implements Connection {
    private final static int OK_MAGIC = 0x12345678;

    private Socket sock;
    private DataInputStream dis;
    private DataOutputStream dos;

    private boolean pendingConfig = true;
    private int serverVersion = 0;
    private int clientId = 0;
    private int aceMagic = 0;
    private int encore = 0;
    private int maxBytes = 0;

    private static int byteSwap(int i) {
	/*

	return ((i & 0x000000ff) << 24) |
	       ((i & 0x0000ff00) << 8) |
	       ((i & 0x00ff0000) >> 8) |
	       ((i & 0xff000000) >> 24);

	*/

	return i;
    }        

    public AceSocket(String host, int port) throws AceException {
	try {
	    sock = new Socket(host, port);
	    dis = new DataInputStream(sock.getInputStream());
	    dos = new DataOutputStream(sock.getOutputStream());
	
	    // System.out.println(transact("hello"));
	    transact("hello");
	} catch (IOException ex) {
	    throw new AceException(ex);
	}
    }

    public String transact(String s) throws AceException {
	try {
	    writeMessage(s);
	    return readMessage();
	} catch (IOException ex) {
	    throw new AceException(ex);
	}
    }

    private void writeMessage(String s) throws IOException {
	// System.out.println("writeMessage: " + s);

	dos.writeInt(byteSwap(OK_MAGIC));
	dos.writeInt(byteSwap(s.length() + 1));

	dos.writeInt(byteSwap(serverVersion)); // ???Server version???
	dos.writeInt(byteSwap(clientId)); // clientId
	dos.writeInt(byteSwap(aceMagic)); // aceMagic
	dos.writeInt(byteSwap(encore)); // encore
	dos.writeInt(byteSwap(maxBytes)); // maxBytes

	byte[] padding = new byte[80 - 28];
	dos.write(padding, 0, padding.length);

	dos.writeBytes(s);
	dos.write(0);
	dos.flush();

	// System.out.println("writeMessageDone "+dos.size());
    }

    private String readMessage() throws IOException {
	int magic = dis.readInt();
	// System.out.println("readmagic :" + magic);
	int length = byteSwap(dis.readInt());
	// System.out.println("length = " + length);

	int rServerVersion = dis.readInt(); // ???Server version???
	// System.out.println("rServerVersion = " + rServerVersion);
	int rClientId = byteSwap(dis.readInt()); // clientId
	// System.out.println("rClientId = " + rClientId);
	int rAceMagic = byteSwap(dis.readInt()); // aceMagic
	// System.out.println("rAceMagic = " + rAceMagic);
	int rEncore = byteSwap(dis.readInt()); // encore
	// System.out.println("rEncore = " + rEncore);
	int rMaxBytes = byteSwap(dis.readInt()); // maxBytes
	// System.out.println("rMaxBytes = " + rMaxBytes);

	if (pendingConfig) {
	    serverVersion = rServerVersion;
	    clientId = rClientId;
	    
	    if (rAceMagic < 0)
		rAceMagic = -rAceMagic;
	    aceMagic = rAceMagic;

	    maxBytes = rMaxBytes;

	    pendingConfig = false;
	}

	dis.skipBytes(80 - 28);
	byte[] message = new byte[length];
	dis.readFully(message);
	return new String(message);
    }

    public void dispose() throws AceException {
	try {
	    sock.close();
	} catch (IOException ex) {
	    throw new AceException(ex);
	}
    }
}
