package process;

import java.util.*;
import java.io.*;
import org.biojava.utils.*;

public class ProcessToolsTest {
    public static void main(String[] args)
        throws Exception
    {
        testEcho();
        testWC();
        testTimebombDefused();
        testTimebombKilled();
    }
    
    private static void testEcho()
        throws Exception
    {
        StringBuffer out = new StringBuffer();
        ProcessTools.exec(
            new String[] {"/bin/echo", "Hello, world"},
            null,
            out,
            null
        );
        System.out.println("Output from echo test");
        System.out.print(out);
        System.out.println();
    }
    
    private static void testWC()
        throws Exception
    {
        StringBuffer out = new StringBuffer();
        ProcessTools.exec(
            new String[] {"/usr/bin/wc", "-w"},
            "The quick brown fox jumps over the lazy dog",
            out,
            null
        );
        int numWords = Integer.parseInt(out.toString().trim());
        System.out.println("Counted " + numWords + " words");
    }
    
    private static void testTimebombDefused()
        throws Exception
    {
        System.err.println("Test a process running normally under a timebomb");
        boolean killed = false;
        try {
            ProcessTools.exec(
                new String[] {"/bin/sleep", "5"},
                null,
                null,
                null,
                10000L
            );
        } catch (ProcessTimeoutException ex) {
            killed = true;
        }
        if (killed) {
            System.err.println("Got a timeout: BAD");
        } else {
            System.err.println("No timeout: GOOD");
        }
    }
    
    private static void testTimebombKilled()
        throws Exception
    {
        System.err.println("Test a process which should be killed by a timebomb");
        boolean killed = false;
        try {
            ProcessTools.exec(
                new String[] {"/bin/sleep", "15"},
                null,
                null,
                null,
                10000L
            );
        } catch (ProcessTimeoutException ex) {
            killed = true;
        }
        if (killed) {
            System.err.println("Got a timeout: GOOD");
        } else {
            System.err.println("No timeout: BAD");
        }
    }
}