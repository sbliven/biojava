package org.ensembl.compara.driver;

import java.util.List;
import java.util.Properties;

import org.ensembl.datamodel.Location;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.DriverManager;

public class ComparaReader{
  public static void main(String[] args){
    String classPath = System.getProperty("java.class.path");
    System.err.println("Running ComparaReader with classpath: "+classPath);

    CoreDriver comparaDriver = null;
    DnaDnaAlignFeatureAdaptor dnaDnaAlignFeatureAdaptor = null;
    MemberAdaptor memberAdaptor = null;
    Location location = null;
    Properties props = new Properties();
    List featureList = null;
    org.ensembl.datamodel.FeaturePair ensjFeaturePair;
    Location queryLocation = null;
    Location hitLocation = null;

    String chromosome = "1";
    int start = 100000;
    int end = 105000;
    
    System.out.println("Start");

    int strand = 0;

    String querySpecies = "Homo sapiens";
    String hitSpecies = "Mus musculus";
    
    props.setProperty("jdbc_driver","org.gjt.mm.mysql.Driver");
    props.setProperty("host","ensembldb.sanger.ac.uk");
    props.setProperty("port","3306");
    props.setProperty("user","anonymous");
    props.setProperty("ensembl_driver","org.ensembl.compara.driver.impl.ComparaMySQLDriver");
    props.setProperty("database","ensembl_compara_27_1");

    try{
      comparaDriver = DriverManager.load(props);
      System.out.println("Loaded driver");

      location = new Location("chromosome:1:100000-105000:0");

      memberAdaptor = (MemberAdaptor)comparaDriver.getAdaptor(MemberAdaptor.TYPE);

      dnaDnaAlignFeatureAdaptor = 
        (DnaDnaAlignFeatureAdaptor)comparaDriver.getAdaptor(DnaDnaAlignFeatureAdaptor.TYPE);

      System.out.println("created adaptors");

      featureList = 
        dnaDnaAlignFeatureAdaptor.fetch(
          querySpecies, 
          location,
          hitSpecies,
          "BLASTZ_NET"
        );

      System.out.println("dna aligns :");

      for(int i=0; i<featureList.size(); i++){
        queryLocation = (Location)((org.ensembl.datamodel.FeaturePair)featureList.get(i)).getLocation();
        hitLocation = (Location)((org.ensembl.datamodel.FeaturePair)featureList.get(i)).getHitLocation();
        System.out.println(
          queryLocation.getSeqRegionName()+":"+queryLocation.getStart()+"-"+queryLocation.getEnd()+"/"+hitLocation.getSeqRegionName()+":"+hitLocation.getStart()+"-"+hitLocation.getEnd()
        );
      }//end for
      
      location = new Location("chromosome:10:105000000-106000000:0");
    
      //fetch protein aligns
      featureList = 
        memberAdaptor.fetch(
          querySpecies, 
          location,
          hitSpecies
        );
      System.out.println("protein aligns :");
      //
      //print them out
      System.out.println(
        querySpecies+": query Chr Start - End / "+ hitSpecies + ": hitChr Start - End"
      );
      
      for(int i=0; i<featureList.size(); i++){
        queryLocation = ((org.ensembl.datamodel.FeaturePair)featureList.get(i)).getLocation();
        hitLocation = ((org.ensembl.datamodel.FeaturePair)featureList.get(i)).getHitLocation();
        System.out.println(
          queryLocation.getSeqRegionName()+":"+queryLocation.getStart()+"-"+queryLocation.getEnd()+"   /   "+hitLocation.getSeqRegionName()+":"+hitLocation.getStart()+"-"+hitLocation.getEnd()
        );
      }//end for
      
      System.out.println("Finished");
    }catch(Exception exception){
      exception.printStackTrace();
    }//end try
  }//end main
}//end ComparaReader
