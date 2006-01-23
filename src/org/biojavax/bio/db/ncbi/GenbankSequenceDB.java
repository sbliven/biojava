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
package org.biojavax.bio.db.ncbi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Set;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.db.HashSequenceDB;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;


/**
 * This class contains functions accessing DNA sequences in Genbank format.
 * It adds methods to return RichSequences instead of plain Sequences.
 *
 * @author Lei Lai
 * @author Matthew Pocock
 * @author Laurent Jourdren
 * @author Shuvankar Mukherjee
 * @author Mark Schreiber
 * @author Richard Holland
 */
public class GenbankSequenceDB extends org.biojava.bio.seq.db.GenbankSequenceDB {
    
    /**
     * The default constructor delegates to the parent class. The constructor refers
     * to RichObjectFactory.getDefaultNamespace() so make sure your factory is initialised
     * before calling this constructor.
     * Sets the default factory to THRESHOLD.
     */
    public GenbankSequenceDB() {
        super();
        this.setFactory(RichSequenceBuilderFactory.THRESHOLD); // threshold factory is efficient
        this.setNamespace(RichObjectFactory.getDefaultNamespace()); // default namespace
    }
    
    /**
     * Given the appropriate Genbank ID, return the matching RichSequence object.
     * @param id the Genbank ID to retrieve.
     * @return the matching RichSequence object, or null if not found.
     * @throws Exception if the sequence could not be retrieved for reasons other
     * than the identifier not being found.
     */
    public RichSequence getRichSequence(String id) throws Exception {
        try {
            IOExceptionFound = false;
            ExceptionFound = false;
            URL queryURL = getAddress(id); //get URL based on ID
            
            SymbolTokenization rParser = getAlphabet().getTokenization("token"); //get SymbolTokenization
            RichSequenceBuilderFactory seqFactory = this.getFactory();
            Namespace ns = this.getNamespace();
            
            DataInputStream in = new DataInputStream(queryURL.openStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            RichSequenceIterator seqI = RichSequence.IOTools.readGenbank(reader, rParser, seqFactory, ns);
            
            return seqI.nextRichSequence();
        } catch (Exception e) {
            System.out.println("Exception found in GenbankSequenceDB -- getRichSequence");
            System.out.println(e.toString());
            ExceptionFound = true;
            IOExceptionFound = true;
            return null;
        }
    }
    
    /**
     * Retrieve rich sequences from a Genbank
     *
     * @param list List of NCBI sequence number (GI), accession, accession.version,
     * fasta or seqid.
     * @return The rich database object (HashSequenceDB) with downloaded rich sequences.
     * You will need to cast the sequences you get from this database object into
     * RichSequence objects if you want to access their full features.
     */
    public SequenceDB getRichSequences(Set list) throws BioException {
        
        return getRichSequences(list, null);
    }
    
    /**
     * Retrieve rich sequences from a Genbank
     *
     * @param list List of NCBI sequence number (GI), accession, accession.version,
     * fasta or seqid.
     * @param database Where to store rich sequences. If database is null, use an
     * HashSequenceDB Object.
     * @return The database object with downloaded rich sequences.
     * You will need to cast the sequences you get from this database object into
     * RichSequence objects if you want to access their full features.
     */
    public SequenceDB getRichSequences(Set list, SequenceDB database)
    throws BioException {
        
        if (database == null)
            database = new HashSequenceDB();
        
        try {
            
            URL url = new URL(urlBatchSequences);
            int port = url.getPort();
            String hostname = url.getHost();
            
            //Open the connection and the streams
            Socket s = new Socket(hostname, port);
            
            InputStream sin = s.getInputStream();
            BufferedReader fromServer =
                    new BufferedReader(new InputStreamReader(sin));
            OutputStream sout = s.getOutputStream();
            PrintWriter toServer = new PrintWriter(new OutputStreamWriter(sout));
            
            // Put the Post request to the server
            toServer.print(makeBatchRequest(url, list));
            toServer.flush();
            
            // Delete response headers
            boolean finEntete = false;
            for (String l = null;
            ((l = fromServer.readLine()) != null) && (!finEntete);
            )
                if (l.equals(""))
                    finEntete = true;
                        
            SymbolTokenization rParser = getAlphabet().getTokenization("token"); //get SymbolTokenization
            RichSequenceBuilderFactory seqFactory = this.getFactory();
            Namespace ns = this.getNamespace();
            
            RichSequenceIterator seqI = RichSequence.IOTools.readGenbank(fromServer, rParser, seqFactory, ns);
            
            while (seqI.hasNext())
                database.addSequence(seqI.nextRichSequence());
            
        } catch (MalformedURLException e) {
            throw new BioException(e,"Exception found in GenbankSequenceDB -- getRichSequences");
        } catch (IOException e) {
            throw new BioException(e,"Exception found in GenbankSequenceDB -- getRichSequences");
        } catch (BioException e) {
            throw new BioException(e,"Exception found in GenbankSequenceDB -- getRichSequences");
        } catch (ChangeVetoException e) {
            throw new BioException(e,"Exception found in GenbankSequenceDB -- getRichSequences");
        }
        
        return database;
    }
    
    /**
     * Holds value of property factory.
     */
    private RichSequenceBuilderFactory factory;
    
    /**
     * Getter for property factory.
     * @return Value of property factory.
     */
    public RichSequenceBuilderFactory getFactory() {
        
        return this.factory;
    }
    
    /**
     * Setter for property factory.
     * @param factory New value of property factory.
     */
    public void setFactory(RichSequenceBuilderFactory factory) {
        
        this.factory = factory;
    }
    
    /**
     * Holds value of property namespace.
     */
    private Namespace namespace;
    
    /**
     * Getter for property namespace.
     * @return Value of property namespace.
     */
    public Namespace getNamespace() {
        
        return this.namespace;
    }
    
    /**
     * Setter for property namespace.
     * @param namespace New value of property namespace.
     */
    public void setNamespace(Namespace namespace) {
        
        this.namespace = namespace;
    }
}
