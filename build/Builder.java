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

package build;

import java.util.*;
import java.io.*;

/**
 * Simple pure java build tool, intended for use with BioJava.
 *
 * @author Thomas Down <td2@sanger.ac.uk>
 */

public class Builder {
    private static FilenameFilter javaFiles = 
             new EndsWithFilenameFilter(".java");

    public static void main(String[] args) throws Exception {
	if (args.length == 0) {
	    printUsage();
	    return;
	}

	String command = args[0].toLowerCase();
	if (command.equals("all"))
	    buildAll();
	else if (command.equals("package")) {
	    List packList = new ArrayList();
	    for (int i = 1; i < args.length; ++i)
		packList.add(args[i]);
	    buildPackages(packList);
	} else if (command.equals("docs")) {
	    buildDocs();
	} else {
	    printUsage();
	}
    }

    public static void printUsage() {
	System.out.println("BioJava integrated build tool 0.01");
	System.out.println("java build.Builder [all | package <name> | docs]");
    }

    public static void buildAll() throws IOException {
	System.out.println("Running complete build...");
	File classDir = new File("class");
	if (classDir.exists()) {
	    System.out.println("Deleting old build tree...");
	    rm_rf(classDir);
	}
	classDir.mkdir();
	List packs = getAllPackages(new File("src"), true);
	System.out.println("Compiling...");
	for (Iterator i = packs.iterator(); i.hasNext(); ) {
	    File pd = (File) i.next();
	    List javaFiles = getJavaFiles(pd);
	    if (compile(javaFiles) != 0)
		throw new IOException("Couldn't build!");
	}
	System.out.println("Packaging...");
	runcmd("jar -cf biojava.jar -C class .");
	runcmd("jar -uf biojava.jar -C resources .");
	runcmd("jar -ufm biojava.jar manifest/defaultmanifest.txt");
	System.out.println("New biojava.jar built.  Share and enjoy!");
    }

    public static void buildPackages(List packs) throws IOException {
	System.out.println("Running partial build...");
	List newPacks = new ArrayList();
	List newPackNames = new ArrayList();
	for (Iterator i = packs.iterator(); i.hasNext(); ) {
	    String pn = (String) i.next();
	    StringBuffer npn = new StringBuffer("src");
	    for (StringTokenizer toke = new StringTokenizer(pn, ".");
		 toke.hasMoreTokens(); ) 
	    {
		npn.append(File.separatorChar);
		npn.append(toke.nextToken());
	    }
	    File pf = new File(npn.toString());
	    if (pf.isDirectory()) {
		newPacks.add(pf);
		newPackNames.add(npn.toString().substring(4)); // CAVE
	    } else
		System.err.println("Warning: package " + pn + " not found");
	}

	if (newPacks.size() == 0) {
	    System.err.println("No packages to build.");
	    return;
	}

	System.out.println("Compiling...");
	for (Iterator i = newPacks.iterator(); i.hasNext(); ) {
	    File pd = (File) i.next();
	    List javaFiles = getJavaFiles(pd);
	    if (compile(javaFiles) != 0)
		throw new IOException("Couldn't build!");
	}
	System.out.println("Packaging...");
	String[] command = new String[6];
	command[0] = "jar";
	command[1] = "-uf";
	command[2] = "biojava.jar";
	command[3] = "-C";
	command[4] = "class";
	for (int i = 0; i < newPackNames.size(); ++i) {
	    command[5] = (String) newPackNames.get(i);

	    Process p = Runtime.getRuntime().exec(command);
	    OutputSpinner os = new OutputSpinner(p.getErrorStream(), System.out);
	    os.start();
	    Thread.yield();
	    try {
		p.waitFor();
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	    Thread.yield();
	    if (p.exitValue() != 0)
		throw new IOException("Packaging error!");
	}

	System.out.println("" + newPacks.size() + " package(s) successfully rebuilt");
    }

    public static void buildDocs() throws IOException {
	System.out.println("Building API documentation...");
	File docDir = new File("docs");
	if (docDir.exists()) {
	    System.out.println("Deleting existing documentation tree...");
	    rm_rf(docDir);
	}
	docDir.mkdir();
	File apiDocDir = new File(docDir, "api");
	apiDocDir.mkdir();
	File srcDir = new File("src");
	List packs = getAllPackages(srcDir, true);
	List pkgNames = new ArrayList();
	Map pkgGroups = new HashMap();
	for (Iterator i = packs.iterator(); i.hasNext(); ) {
	    File pack = (File) i.next();
	    String path = pack.getPath();
	    List temp = new LinkedList();
	    for (File gopher = pack; !(gopher.equals(srcDir)); gopher = gopher.getParentFile()) {
		temp.add(0, gopher.getName());
	    }
	    StringBuffer pn = new StringBuffer();
	    for (int item = 0; item < temp.size(); ++item) {
		if (item != 0)
		    pn.append('.');
		pn.append((String) temp.get(item));
	    }

	    pkgNames.add(pn.toString());

	    String groupName = getGroupName(pack);
	    if (groupName != null) {
		List grp = (List) pkgGroups.get(groupName);
		if (grp == null) {
		    grp = new ArrayList();
		    pkgGroups.put(groupName, grp);
		}
		grp.add(pn.toString());
	    }
	}

	System.out.println("Running Javadoc...");
	String[] command = new String[11 + pkgGroups.size() * 3 + pkgNames.size()];
	command[0] = "javadoc";
	command[1] = "-sourcepath";
	command[2] = "src";
	command[3] = "-classpath";
	command[4] = makePath(new String[] {"class", "xml.jar"});
	command[5] = "-d";
	command[6] = apiDocDir.getPath();
	command[7] = "-version";
	command[8] = "-author";
	command[9] = "-windowtitle";
	command[10] = "Biojava Public API documentation";
	int indx = 11;
	for (Iterator i = pkgGroups.entrySet().iterator(); i.hasNext(); ) {
	    Map.Entry me = (Map.Entry) i.next();
	    command[indx++] = "-group";
	    command[indx++] = (String) me.getKey();
	    StringBuffer sb = new StringBuffer();
	    List grp = (List) me.getValue();
	    for (Iterator gi = grp.iterator(); gi.hasNext(); ) {
		if (sb.length() != 0)
		    sb.append(':');
		sb.append((String) gi.next());
	    }
	    command[indx++] = sb.toString();
	}
	for (Iterator i = pkgNames.iterator(); i.hasNext(); ) {
	    command[indx++] = (String) i.next();
	}
	Process p = Runtime.getRuntime().exec(command);
	OutputSpinner os = new OutputSpinner(p.getErrorStream(), System.out);
	os.start();
	Thread.yield();
	try {
	    p.waitFor();
	} catch (InterruptedException ex) {
	    ex.printStackTrace();
	}
	Thread.yield();

	if (p.exitValue() != 0)
	    System.out.println("JavaDoc errors...");
	else
	    System.out.println("Documentation built okay...");
    }

    public static String getGroupName(File pack) throws IOException {
	File packInfo = new File(pack, "package.inf");
	if (!packInfo.exists())
	    return null;
	BufferedReader br = new BufferedReader(new FileReader(packInfo));
	String tag = br.readLine();
	if (! (tag.equals("!!BioJavaBuildTool1")))
	    System.out.println("Warning: package " + pack.getPath() + " has invalid package.inf file");
	return br.readLine();
    }
    
    public static int compile(List filenames) throws IOException {
	String[] command = new String[7 + filenames.size()];
	command[0] = "/usr/opt/java122/bin/javac";
	command[1] = "-classpath";
	command[2] = makePath(new String[] {"class", "xml.jar"});
	command[3] = "-sourcepath";
	command[4] = "src";
	command[5] = "-d";
	command[6] = "class";
  	for (int i = 0; i < filenames.size(); ++i) {
  	    command[i + 7] = ((File) filenames.get(i)).getCanonicalPath();
        }
	
	Process p = Runtime.getRuntime().exec(command);
	OutputSpinner os = new OutputSpinner(p.getErrorStream(), System.out);
	os.start();
	Thread.yield();
	try {
	    p.waitFor();
	} catch (InterruptedException ex) {
	    ex.printStackTrace();
	}
	Thread.yield();

	return p.exitValue();
    }

    public static List getAllPackages(File root, boolean dont) throws IOException {
	List l = new ArrayList();
	boolean includeSelf = false;
	File[] all = root.listFiles();
	for (int i = 0; i < all.length; ++i) {
	    if (!dont && !includeSelf && javaFiles.accept(root, all[i].getName()))
		includeSelf = true;
            if (all[i].isDirectory())
		l.addAll(getAllPackages(all[i], false));
	}

	if (includeSelf)
	    l.add(root);

	return l;
    }

    public static List getJavaFiles(File pd) throws IOException {
	List jf = new ArrayList();
	File[] javas = pd.listFiles(javaFiles);
	for (int n=0; n < javas.length; ++n)
	    jf.add(javas[n]);
	return jf;
    }

    public static int runcmd(String cmd) throws IOException {
	Process p = Runtime.getRuntime().exec(cmd);
	OutputSpinner os = new OutputSpinner(p.getErrorStream(), System.out);
	os.start();
	try {
	    p.waitFor();
	} catch (InterruptedException ex) {
	}
	return p.exitValue();
    }

    public static String makePath(String[] parts) {
	StringBuffer path = new StringBuffer();
	for (int i = 0; i < parts.length; ++i) {
	    if (i > 0)
		path.append(System.getProperty("path.separator"));
	    path.append(parts[i]);
	}
	return path.toString();
    }

    public static void rm_rf(File dir) throws IOException {
	File[] f = dir.listFiles();
        for (int i = 0; i < f.length; ++i) {
	    if (f[i].isDirectory())
		rm_rf(f[i]);
	    else
		f[i].delete();
	}
	dir.delete();
    }
}


class OutputSpinner extends Thread {
    private InputStream in;
    private OutputStream out;

    public OutputSpinner(InputStream in, OutputStream out) {
	super();
	this.in = in;
	this.out = out;
    }

    public void run() {
	try {
	    int b;
	    while ((b = in.read()) != -1)
		out.write(b);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }
}

class EndsWithFilenameFilter implements FilenameFilter {
    private String fs;

    EndsWithFilenameFilter(String fs) {
	this.fs = fs.toLowerCase();
    }

    public boolean accept(File dir, String name) {
	return (name.toLowerCase().endsWith(fs) && !name.startsWith("."));
    }
}
