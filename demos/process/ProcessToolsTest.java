package process;

import java.io.*;
import org.biojava.utils.*;

/**
 * @author unknown
 * @author Matthew Pocock
 */
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
        Writer out = new StringWriter();
        ProcessTools.exec(
            new String[] {"/bin/echo", "Hello, world"},
            null,
            out,
            null
        );
        System.out.println("Output from echo test");
        System.out.print(out.toString());
        System.out.println();
    }

    private static void testWC()
        throws Exception
    {
      Writer out = new StringWriter();
        ProcessTools.exec(
            new String[] {"/usr/bin/wc", "-w"},
            new StringReader("The quick brown fox jumps over the lazy dog"),
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
                (String[]) null,
                (File) null,
                (Reader) null,
                (Writer) null,
                (Writer) null,
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