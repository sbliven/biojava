/*
 * TestDigest2.java
 *
 * Created on July 10, 2001, 1:10 PM
 */

package prot;




import org.biojava.bio.proteomics.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.BioException;
import org.biojava.utils.*;
import java.io.*;
import java.util.*;
import java.text.NumberFormat;

/**
 *
 * @author  mjones
 * @version 
 */
public class TestDigestIO extends Object {
    
    private MassCalc massCalc;
    /** Creates new TestDigest2 */
    public TestDigestIO(String fileName) throws BioException, 
                                                ChangeVetoException, 
                                                IOException {
        //Initiate Digest
        Digest bioJavaDigest = new Digest();
        bioJavaDigest.setMaxMissedCleavages(2);

        massCalc = new MassCalc(SymbolPropertyTable.AVG_MASS);
	try{
	   massCalc.setSymbolModification('W', 100000.00); 
        }catch(Exception e){}
        
       // Protease protease = new Protease();
       // String[] list = Protease.getProteaseList();
       // for(int i=0; i<list.length ;i++)
       //         System.out.println(list[i]);
        
	//String proteaseName = Protease.ASP_N;
	String proteaseName = Protease.TRYPSIN;
       bioJavaDigest.setProtese(Protease.getProteaseByName(proteaseName));
        
        //Get the Sequence Iterator
        Alphabet alpha = ProteinTools.getAlphabet();
        SymbolParser protParser = alpha.getParser("token");
        BufferedReader br = new BufferedReader(
                               new FileReader(fileName));

                                        
      SequenceBuilderFactory sFact = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
      SequenceFormat sFormat = new FastaFormat();
      
      SequenceIterator sourceI = new StreamReader(
        br,
        sFormat,
        protParser,
        sFact
      );
      
      ArrayList list  = new ArrayList();
      while(sourceI.hasNext()) {
         Sequence sourceSeq = sourceI.nextSequence();
         bioJavaDigest.setSequence(sourceSeq);
         bioJavaDigest.addDigestFeatures();
         list.add(sourceSeq);
         printFeatures(sourceSeq.features(), sourceSeq.getName() + " " );
      }
      
      
        
    }
    
    private  void printFeatures(Iterator i, String prefix)
    {
       NumberFormat nf = NumberFormat.getInstance();

       nf.setMaximumFractionDigits(2);
       nf.setMinimumFractionDigits(2);
		
       
                              
       for (; i.hasNext(); ) {
        Feature f = (Feature) i.next();
        System.out.print(prefix);
        System.out.print(f.getType());
        System.out.print(f.getLocation().toString()+ " ");
                   
        //Use this for static
	try{
	    double mass = MassCalc.getMass(
                                       f.getSymbols(), 
                                       SymbolPropertyTable.AVG_MASS, 
                                       true);
	    System.out.print(nf.format(mass) + " ");
                    
	    //Use this for instances of MassCalc
	    mass = massCalc.getMass(f.getSymbols(),true);
                    
	    System.out.print(" PTM " + nf.format(mass) + " ");
                    
                    
	    System.out.print(" " + f.getSymbols().seqString() + "    " );
                   
                   
                   
	    System.out.println();
	    //  printFeatures(f, pw, prefix + "    ");
                
	}
	catch(IllegalSymbolException ise){
	    System.out.println(ise.getMessage());
	}
    }
  }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        if(args.length < 1){
            System.out.println("Usage: java prot.TestDigest <fastaFile>");
            System.exit(-1);
        }
        try{
           new TestDigestIO(args[0]); 
        }catch (BioException bioe){
            bioe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (ChangeVetoException cve){
            cve.printStackTrace();
        }
    }

}
