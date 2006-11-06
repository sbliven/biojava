package org.ensembl.compara.test;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.FeaturePair;
import org.ensembl.datamodel.Location;

public class MemberAdaptorTest extends ComparaBase {

  private static Logger logger = Logger.getLogger(MemberAdaptorTest.class
      .getName());

  public MemberAdaptorTest(String name) throws Exception {
    super(name);
  }

  public void testRetrieveSpecificHomologyFeaturePairs() throws Exception {

    Location queryLocation = new Location("chromosome:10:105000000-1050100000:0");

    Location hitLocation = new Location("chromosome:19:47000000-47010000:0");

    List features = comparaDriver.getMemberAdaptor().fetch("Homo sapiens",
        queryLocation, "Mus musculus");

    Iterator theIterator = features.iterator();
    FeaturePair feature;
    while (theIterator.hasNext()) {
      feature = (FeaturePair) theIterator.next();
      logger.info(feature.getDescription() + " "
          + ((Location) feature.getLocation()).getSeqRegionName() + ":"
          + ((Location) feature.getLocation()).getStart() + ","
          + ((Location) feature.getLocation()).getEnd() + "---"
          + feature.getHitDescription() + " "
          + ((Location) feature.getHitLocation()).getSeqRegionName() + ":"
          + ((Location) feature.getHitLocation()).getStart() + ","
          + ((Location) feature.getHitLocation()).getEnd());
    }

    logger.info("Number of Feature pairs found:" + features.size());

    features = comparaDriver.getMemberAdaptor().fetch("Homo sapiens",
        queryLocation, "Mus musculus", hitLocation);

    theIterator = features.iterator();
    while (theIterator.hasNext()) {
      feature = (FeaturePair) theIterator.next();
      logger.info(feature.getDescription() + " "
          + ((Location) feature.getLocation()).getSeqRegionName() + ":"
          + ((Location) feature.getLocation()).getStart() + ","
          + ((Location) feature.getLocation()).getEnd() + "---"
          + feature.getHitDescription() + " "
          + ((Location) feature.getHitLocation()).getSeqRegionName() + ":"
          + ((Location) feature.getHitLocation()).getStart() + ","
          + ((Location) feature.getHitLocation()).getEnd());
    }

    logger.info("Number of Feature pairs found:" + features.size());

    //      features =
    //        theAdaptor.fetch(
    //          "Homo sapiens",
    //          "Mus musculus",
    //          "1"
    //        );
    //      
    //      theIterator = features.iterator();
    //      while(theIterator.hasNext()){
    //        feature = (FeaturePair)theIterator.next();
    //        logger.info(
    //          feature.getHitDescription()+" "+
    //          ((Location)feature.getLocation()).getSeqRegionName()+":"+
    //          ((Location)feature.getLocation()).getStart()+","+
    //          ((Location)feature.getLocation()).getEnd()+"---"+
    //          feature.getHitDescription()+" "+
    //          ((Location)feature.getHitLocation()).getSeqRegionName()+":"+
    //          ((Location)feature.getHitLocation()).getStart()+","+
    //          ((Location)feature.getHitLocation()).getEnd()
    //        );
    //      }
    //      
    //      logger.info("Number of Feature pairs found:" + features.size());

    String[] names = new String[] { "ENSG00000065613", "ENSG00000148836",
        "ENSG00000120051" };

    features = comparaDriver.getMemberAdaptor().fetch("Homo sapiens", names,
        "Mus musculus");

    theIterator = features.iterator();
    while (theIterator.hasNext()) {
      feature = (FeaturePair) theIterator.next();
      logger.info(feature.getDescription() + " "
          + ((Location) feature.getLocation()).getSeqRegionName() + ":"
          + ((Location) feature.getLocation()).getStart() + ","
          + ((Location) feature.getLocation()).getEnd() + "---"
          + feature.getHitDescription() + " "
          + ((Location) feature.getHitLocation()).getSeqRegionName() + ":"
          + ((Location) feature.getHitLocation()).getStart() + ","
          + ((Location) feature.getHitLocation()).getEnd());
    }

    logger.info("Number of Feature pairs found:" + features.size());

  }//end testRetrieveSpecificRepeatFeature

}// RepeatFeatureTest

