/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.idmapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ensembl.util.SerialUtil;

/**
 * Run exonerate on LSF.
 */
public class ExonerateRunner {

    // config variables - will come from config object later

    // The exonerate executable to use - obtained from config file
    private static String exoneratePath;

    // score threshold (0-1) - only report results above this
    private static final double EXONERATE_THRESHOLD = 0.5;

    // Number of exonerate jobs to create; if 0, number is dynamically calculated based on file size
    private static final int EXONERATE_JOBS = 0;

    // If dynamically calculating number of jobs, number of bytes to "equal" one job
    private static final long BYTES_PER_JOB = 250000;

    boolean debug = true;

    // -------------------------------------------------------------------------

    public ExonerateRunner() {

        exoneratePath = System.getProperty("idmapping.exonerate.path");

    }

    // -------------------------------------------------------------------------
    /**
     * Run exonerate via LSF on old and new data files, parse the results and build a score matrix.
     * 
     * @param query The "old" exon sequence file, in FASTA format.
     * @param target The "new" exon sequence file, in FASTA format.
     * @param checkpoint If true, write the score matrix to a serialised file.
     * @return The scored mapping matrix derived from the files, or an empty matrix if there is a problem. Matrix made up of Long
     *         oldID, Long newID, float score.
     */
    public ScoredMappingMatrix run(String query, String target, boolean checkpoint, String rootDir) {

        File q = new File(rootDir + File.separator + query);
        File t = new File(rootDir + File.separator + target);
        String serializedMatrixFileName = rootDir + File.separator + "exons_exonerate.ser";
	File f = new File( serializedMatrixFileName );
	if( f.exists() ) {
	    System.out.println("Using existing exonerate score matrix in "
			       + serializedMatrixFileName);
	    ScoredMappingMatrix exonScoringMatrix = (ScoredMappingMatrix) SerialUtil
		.readObject(serializedMatrixFileName);
	    return exonScoringMatrix;
	}

        if (!(q.exists() && q.canRead() && t.exists() && t.canRead())) {
            System.err.println("Problem reading " + query + " or " + target + " - returning empty matrix");
            return new ScoredMappingMatrix();
        }

        // get the number of jobs
        int jobs = (EXONERATE_JOBS > 0) ? EXONERATE_JOBS : guessNumJobs(rootDir, query);

        // build the job script
        String job = buildJobScript(query, target, jobs, rootDir);

        // submit with bsub
        String baseName = query.replaceAll(".fasta", "");
        String prefix = submitJob(job.toString(), jobs, rootDir + File.separator + baseName, rootDir);

        checkExonerateResults(jobs, baseName, rootDir);

        // parse results
        ScoredMappingMatrix matrix = parseResults(rootDir, prefix, ".exonmap");

        // checkpoint the matrix
        if (checkpoint) {

            checkpointMatrix(matrix, query, target, rootDir);

        }

        return matrix;

    }


  /**
   * Checks that exonerate ran correctly and throws a RuntimeException if
   * a problem occured.
   *
   * Checks that all output files exist and all error files are empty.
   *
   */
  private void checkExonerateResults(int jobs, String baseName, String rootDir) {

    StringBuffer outputFiles = new StringBuffer();
    StringBuffer errFiles = new StringBuffer();

    // 1...N
    for(int i=1; i<=jobs;++i) {

      File mappingFile = new File(rootDir+File.separator+i+".exonmap");
      if (!mappingFile.exists())
        outputFiles.append("MISSING exonerate output file: ").append(mappingFile).append("\n");

      File errFile =  new File(rootDir+File.separator+baseName+"."+i+".err");
      if (!errFile.exists())
        errFiles.append("MISSING exonerate error file:").append(errFile).append("\n");
      if (errFile.length()>0)
        errFiles.append("ERROR running exonerate: ").append(errFile).append("\n");

    } 

    if (outputFiles.length()>0 || errFiles.length()>0) {
      System.err.println(outputFiles);
      System.err.println(errFiles);
      throw new RuntimeException("Problem running exonerate. See previous output for more information.");
    }
  }

    /**
     * Run exonerate using settings from System properties. See documentation for other run method.
     */
    public ScoredMappingMatrix run(String rootDir) {

        return run(System.getProperty("idmapping.source.database") + "_exons.fasta", System
                .getProperty("idmapping.target.database")
                + "_exons.fasta", true, rootDir);

    }

    // -------------------------------------------------------------------------

    private String buildJobScript(String query, String target, int jobs, String rootDir) {

        String output = "$LSB_JOBINDEX.exonmap";
        int percent = (int) (EXONERATE_THRESHOLD * 100);

        List j = new ArrayList();

        j.add(". /usr/local/lsf/conf/profile.lsf");
        j.add("");
        j.add(exoneratePath + " " + rootDir + "/" + query + " " + rootDir + "/" + target + " --querychunkid $LSB_JOBINDEX --querychunktotal " + jobs
                + " --model affine:local -M 900 --showalignment FALSE --subopt no --percent " + percent
                + " --ryo \"myinfo: %qi %ti %et %ql %tl\\n\" | grep '^myinfo:' > " + rootDir + "/" + output);

        StringBuffer job = new StringBuffer();
        
        //if (debug) {
        //    System.out.println("\nJob script:\n");
        //}
        Iterator it = j.iterator();
        while (it.hasNext()) {
            String line = (String) it.next();
            // do NOT comment out the following line!
	    job.append(line + "\n");
	    // if (debug) {
            //    System.out.println(line);
            //}
        }
        
        
        return job.toString();

    }

    // -------------------------------------------------------------------------

    private String submitJob(String job, int jobs, String prefix, String rootDir) {

        String uniqueName = "mapexons" + System.currentTimeMillis();

        // note use of *any* quoting here seems to break things
        String[] cmd = {"bsub", "-J" + uniqueName + "[1-" + jobs + "]", "-o", prefix + ".%I.out", "-e", prefix + ".%I.err", "-q", "normal", "-m", "bc_hosts"};

        String[] depend = {"bsub", "-K", "-wended(" + uniqueName + ")", "-q", "small", "-o",
                rootDir + File.separator + "depend.out", "-e", rootDir + File.separator + "depend.err", "/bin/true"};

        
	/*
        if (debug) {
            System.out.println("\nMain job:\n");
            for (int i = 0; i < cmd.length; i++) {
                System.out.print(cmd[i] + " ");
            }
            System.out.println();
            System.out.println("\n\nDepend job:");
            for (int i = 0; i < depend.length; i++) {
                System.out.print(depend[i] + " ");
            }
            System.out.println();
        }
        */
        
        Process jobProc = null;
        Process dependProc = null;

        StandardStreamReader reader = null;
        StandardStreamReader error = null;
        StandardStreamReader dependReader = null;
        StandardStreamReader dependError = null;

        System.out.println("Submitting " + jobs + " exonerate jobs to LSF");

        try {

            // start the command running
            jobProc = Runtime.getRuntime().exec(cmd);
            System.out.println("Submitted main job");

            // thread to read the output
            reader = new StandardStreamReader(jobProc.getInputStream(), System.out, true);
            reader.start();

            // thread to read stderr
            error = new StandardStreamReader(jobProc.getErrorStream(), System.out, true);
            error.start();

            // thread to write stuff
            StandardInputWriter writer = new StandardInputWriter(jobProc);
            writer.start();

            writer.writeln(job);

            writer.close();

            // we now submit ANOTHER job which depends on the first, but does not exit until
            // the first one has completed
            jobProc.waitFor();
            
            //System.exit( 0 );
            
            dependProc = Runtime.getRuntime().exec(depend);
            dependReader = new StandardStreamReader(dependProc.getInputStream(), System.out, true);
            dependReader.start();
            dependError = new StandardStreamReader(dependProc.getErrorStream(), System.out, true);
            dependError.start();
            System.out.println("Submitted dependent job");

            // get job ID just in case
            String jobID = parseJobID(reader);

            System.out.println("Waiting for exonerate jobs to run on the farm - this will take about 20 minutes");
            dependProc.waitFor();
            System.out.println("All jobs finished");

            // close readers and stop threads
	    /*
            reader.close();
            reader.stop();
            reader = null;
            error.close();
            error.stop();
            error = null;
            dependReader.close();
            dependReader.stop();
            dependReader = null;
            dependError.close();
            dependError.stop();
            dependError = null;
            writer = null;
	    */
        } catch (IOException ioe) {

            ioe.printStackTrace(System.err);

        } catch (InterruptedException ine) {
            System.err.println("Error waiting for main job to be submitted");
            ine.printStackTrace();
        }

        // check for failure
        if (jobProc != null && jobProc.exitValue() != 0) {
            System.err.println("Main job exit value = " + jobProc.exitValue());
            System.err.println("Main job stderr:");
            dumpBuffer(error.getBuffer());
            System.err.println("Main job stdout:");
            dumpBuffer(reader.getBuffer());
        }
        if (dependProc != null && dependProc.exitValue() != 0) {
            System.err.println("Depend job exit value = " + dependProc.exitValue());
            System.err.println("Depend job stderr:");
            dumpBuffer(dependError.getBuffer());
            System.err.println("Depend job stdout:");
            dumpBuffer(dependReader.getBuffer());
        }

        return prefix;

    }

    // -------------------------------------------------------------------------

    private ScoredMappingMatrix parseResults(String rootDir, String prefix, String postfix) {

        debug("Parsing exonerate results");
        ScoredMappingMatrix matrix = new ScoredMappingMatrix();

        ExonerateResultsFilenameFilter filter = new ExonerateResultsFilenameFilter(prefix, postfix);

        File dir = new File(rootDir);
        String[] resultFiles = dir.list(filter);
        int lines = 0;

        for (int i = 0; i < resultFiles.length; i++) {

            File f = new File(rootDir + File.separator + resultFiles[i]);

            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {

                    // line format: old ID, new ID, match length, old length, new length
                    // myinfo: 147835 8822 32 1479 32
                    String[] bits = line.split(" ");
                    long oldID = Long.parseLong(bits[1]);
                    long newID = Long.parseLong(bits[2]);
                    int matchLen = Integer.parseInt(bits[3]);
                    int oldLen = Integer.parseInt(bits[4]);
                    int newLen = Integer.parseInt(bits[5]);
                    float score;
                    if (oldLen == 0 || newLen == 0 ) {
                        System.err
                                .println("Error: total length of 0 for ID pair " + oldID + " " + newID + " - setting score to 0.");
                        score = 0.0f;
                    } else {
                    		// create a symmetric exon score between 0.0 and 1.0 for 1.0 being complete
                    		// identity
                        score = (float) ((double) 2*matchLen / (double) ( oldLen + newLen ));
                    }

                    matrix.addScore(oldID, newID, score);
                    lines++;
                }

                br.close();

            } catch (FileNotFoundException e) {
                System.err.println("Can't read " + resultFiles[i]);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //if (debug) {
        debug("Read a total of " + lines + " lines from " + resultFiles.length + " result files");
        //}

        return matrix;

    }

    // -------------------------------------------------------------------------
    /**
     * Estimate the number of exonerate jobs based on the size of the query file.
     */
    private int guessNumJobs(String rootDir, String query) {

        File f = new File(rootDir + File.separator + query);

        return (int) (f.length() / BYTES_PER_JOB) + 1;

    }

    // -------------------------------------------------------------------------

    private void checkpointMatrix(ScoredMappingMatrix matrix, String query, String target, String rootDir) {

        String name = rootDir + File.separator + "exons_exonerate.ser";

        SerialUtil.writeObject(matrix, name);

        debug("Wrote exonerate score matrix to " + name);

    }

    // -------------------------------------------------------------------------

    public static void main(String[] args) {

        ExonerateRunner er = new ExonerateRunner();

        if (args.length != 2) {
            System.err.println("Usage: ExonerateRunner old.fasta new.fasta");
            System.exit(1);
        }

        ScoredMappingMatrix matrix = er.run(args[0], args[1], true, "");

    }

    // -------------------------------------------------------------------------
    /**
     * Get the LSF job ID from the output of a StandardOutputReader.
     */
    private String parseJobID(StandardStreamReader reader) {

        //while (!reader.ready() || reader.getBuffer().size() == 0) {
        //    System.out.println("##waiting for reader");
        //    try { Thread.sleep(1000); } catch (InterruptedException ie) { ie.printStackTrace(); }
        //}
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        String jobID = "";

        Iterator it = reader.getBuffer().iterator();
        while (it.hasNext()) {
            String line = (String) it.next();
            int firstOpen = line.indexOf("<");
            int firstClose = line.indexOf(">");
            if (firstOpen > -1 && firstClose > -1) {
                jobID = line.substring(firstOpen + 1, firstClose);
            }
        }

        if (jobID.equals("")) {
            System.err.println("Warning: can't parse job ID from output");
        } else {
            System.out.println("Main job ID: " + jobID);
        }

        return jobID;

    }

    // -------------------------------------------------------------------------

    private void dumpBuffer(List buffer) {

        Iterator it = buffer.iterator();
        while (it.hasNext()) {
            System.out.println((String) it.next());
        }

    }

    // -------------------------------------------------------------------------

    private void debug(String s) {

        if (debug) {
            System.out.println(s);
        }

    }

    // -------------------------------------------------------------------------

} // ExonerateRunner

// -------------------------------------------------------------------------

class ExonerateResultsFilenameFilter implements FilenameFilter {

    private String prefix, postfix;

    public ExonerateResultsFilenameFilter(String prefix, String postfix) {

        this.prefix = prefix;
        this.postfix = postfix;

    }

    public boolean accept(File arg0, String arg1) {

        //String regexp = prefix + ".*" + postfix;
        String regexp = "[0-9]+\\.exonmap";

        return arg1.matches(regexp);

    }
}

// -------------------------------------------------------------------------
