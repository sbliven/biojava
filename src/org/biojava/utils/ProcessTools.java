/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of either the BSD licence or the GNU Lesser General
 * Public Licence.  These should be distributed with the code. 
 * If you do not have copies see:
 *
 *      http://www.opensource.org/licenses/bsd-license.php
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
 
package org.biojava.utils;

import java.io.*;
import java.util.*;


/**
 * Convenience methods for running external processes.  This class
 * offers wrappers around the <code>java.lang.Process</code> API,
 * but hides away the details of managing threads and process I/O.
 *
 * @author Thomas Down
 * @since 1.4
 */

public class ProcessTools {

        /** Win NT/2K/MEPro require cmd.exe to run programs **/
      private static final String WINDOWS_NT_2000_COMMAND_1 = "cmd.exe";

      /** Win NT/2K/MEPro require the /C to specify what to run **/
      private static final String WINDOWS_NT_2000_COMMAND_2 = "/C";

      /** Win 9X/MEHome require cmd.exe to run programs **/
      private static final String WINDOWS_9X_ME_COMMAND_1 = "command.exe";

      /** Win 9X/MEHome require the /C to specify what to run **/
      private static final String WINDOWS_9X_ME_COMMAND_2 = "/C";

      /** String to send to STDERR if program exceeds max run time **/
      private static final String MAX_RUN_TIME_EXCEEDED_STRING =
	  "MAX_RUN_TIME_EXCEEDED";

      /** Default max run time (in seconds) **/
      private static int maxRunTimeSecs = 0;


  /**
     * Execute the specified command and wait for it to return
     *
     * @param args the command line to execute.
     * @param input data to present to the process' standard input, or <code>null</code> if the process does not require input.
     * @param stdout a <code>StringBuffer</code> which will be filled with data from the process' output stream, or <code>null</code> to ignore output.
     * @param stderr a <code>StringBuffer</code> which will be filled with data from the process' error stream, or <code>null</code> to ignore output.
     * @return the process' return code.
     * @throws IOException if an error occurs while starting or communicating with the process
     */
  public static int exec(
        String[] args,
        String input,
        StringBuffer stdout,
        StringBuffer stderr
    )
        throws IOException
    {
      /** Flag to indicate if we've exceeded max run time **/
      boolean maxRunTimeExceeded = false;

      
      // First get the start time & calculate comparison numbers
      Date startTime = new Date();
      long startTimeMs = startTime.getTime();
      long maxTimeMs = startTimeMs + (maxRunTimeSecs * 1000);
      
        Process proc = Runtime.getRuntime().exec(args);
        
        Pump outPump, inPump, errPump;
        
        if (input == null) {
            input = "";
        }
        outPump = new PumpStreamToStream(new StringBufferInputStream(input), proc.getOutputStream());
        if (stdout == null) {
            inPump = new PumpStreamToNull(proc.getInputStream());
        } else {
            inPump = new PumpStreamToStringBuffer(proc.getInputStream(), stdout);
        }
        if (stderr == null) {
            errPump = new PumpStreamToNull(proc.getErrorStream());
        } else {
            errPump = new PumpStreamToStringBuffer(proc.getErrorStream(), stderr);
        }
        
        outPump.start();
        inPump.start();
        errPump.start();
        
        int rc;
        try {
            rc = proc.waitFor();
            outPump.join();
            inPump.join();
            errPump.join();
        } catch (InterruptedException iex) {
            throw new IOException("Error waiting for process to complete");
        }
        
        checkException(outPump, "Output to child");
        checkException(inPump, "Input from child");
        checkException(errPump, "Errors from child");
        return rc;
    }

  
  /**
   * Execute the specified command and wait for it to return. This is the
   * simplified version that tries to be nice and make your life easier. If
   * you know exactly what you want, you might want to use exec(String[],...)
   * instead.  
   *
   * @param command the command line to execute.
   * @param input data to present to the process' standard input, or
   * <code>null</code> if the process does not require input. 
   * @param stdout a <code>StringBuffer</code> which will be filled with data
   * from the process' output stream, or <code>null</code> to ignore output. 
   * @param stderr a <code>StringBuffer</code> which will be filled with data
   * from the process' error stream, or <code>null</code> to ignore output. 
   * @return the process' return code.
   * @throws IOException if an error occurs while starting or communicating
   * with the process 
   */
  public static int exec(
                         String command,
                         String input,
                         StringBuffer stdout,
                         StringBuffer stderr)
        throws IOException
  {
    String[] cmd = null;
    // First determine the OS to build the right command string
    String osName = System.getProperty("os.name");
    	 if (osName.equals("Windows NT") || osName.equals("Windows 2000") ||
             osName.equals("Windows XP")) {
	     cmd = new String[3];
	     cmd[0] = WINDOWS_NT_2000_COMMAND_1;
	     cmd[1] = WINDOWS_NT_2000_COMMAND_2;
	     cmd[2] = command;
	 }
	 else if (
	     osName.equals("Windows 95")
		 || osName.equals("Windows 98")
		 || osName.equalsIgnoreCase("Windows ME")) {
	     cmd = new String[3];
	     cmd[0] = WINDOWS_9X_ME_COMMAND_1;
	     cmd[1] = WINDOWS_9X_ME_COMMAND_2;
	     cmd[2] = command;
	 }
	 else {
	     // Linux (and probably other *nixes) prefers to be called
	     // with each argument supplied separately, so we first
	     // Tokenize it across spaces as the boundary.
	     StringTokenizer st = new StringTokenizer(command, " ");
	     cmd = new String[st.countTokens()];
	     int token = 0;
	     while (st.hasMoreTokens()) {
		 String tokenString = st.nextToken();
		 //System.out.println(tokenString);
		 cmd[token++] = tokenString;
	     }
	 }
         return exec(cmd,input,stdout,stderr);
  }
  
  

  
    private static void checkException(Pump p, String msg)
        throws IOException
    {
        IOException ioe = p.getException();
        if (ioe != null) {
            throw new IOException("Exception processing " + msg);
        }
    }
    
    private static abstract class Pump extends Thread {
        private IOException err = null;
        
        protected abstract int sourceData(byte[] buf) throws IOException;
        protected abstract void sinkData(byte[] buf, int len) throws IOException;
        protected void shutdownHook() throws IOException {};
        
        public void run() {
            try {
                byte[] buf = new byte[256];
                int cnt;
                do {
                    cnt = sourceData(buf);
                    if (cnt > 0) {
                        sinkData(buf, cnt);
                    }
                } while (cnt >= 0);
                shutdownHook();
            } catch (IOException e) {
                this.err = e;
            }
        }
        
        public IOException getException() {
            return err;
        }
    }
    
    private static final class PumpStreamToStringBuffer extends Pump {
        private final InputStream is;
        private final StringBuffer sb;
        
        public PumpStreamToStringBuffer(InputStream is, StringBuffer sb) {
            super();
            this.is = is;
            this.sb = sb;
        }
        
        protected int sourceData(byte[] buf)
            throws IOException
        {
            return is.read(buf);
        }
        
        protected void sinkData(byte[] buf, int len) {
            sb.append(new String(buf, 0, len));
        }       
    }
    
    private static final class PumpStreamToNull extends Pump {
        private final InputStream is;
        
        public PumpStreamToNull(InputStream is) {
            super();
            this.is = is;
        }
        
        protected int sourceData(byte[] buf)
            throws IOException
        {
            return is.read(buf);
        }
        
        protected void sinkData(byte[] buf, int len) {
        }       
    }

    private static final class PumpStreamToStream extends Pump {
        private final InputStream is;
        private final OutputStream os;
        
        public PumpStreamToStream(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }
        
        protected int sourceData(byte[] buf)
            throws IOException
        {
            return is.read(buf);
        }
        
        protected void sinkData(byte[] buf, int len)
            throws IOException
        {
            os.write(buf, 0, len);
            os.flush();
        }
        
        protected void shutdownHook() 
            throws IOException
        {
            os.close();
        }
    }
}
