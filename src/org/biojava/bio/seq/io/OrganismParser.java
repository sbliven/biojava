package org.biojava.bio.seq.io;

import java.io.Serializable;
import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.taxa.*;
import org.biojava.utils.*;

/**
 * A parser that is able to generate Taxa entries for sequence builder event
 * streams.
 *
 * @author Matthew Pocock
 */
public class OrganismParser
  extends
    SequenceBuilderFilter
//  implements
//    ParseErrorSource
{
  public static final String PROPERTY_ORGANISM = OrganismParser.class + ":organism";
  
  /**
   * Factory which wraps SequenceBuilders in an OrganismParser.
   *
   * @author Matthew Pocock
   */
  public static class Factory
    implements
      SequenceBuilderFactory,
      Serializable
  {
    private SequenceBuilderFactory delegateFactory;
    private String sciNameKey;
    private String commonNameKey;
    private String ncbiTaxaKey;
    private TaxaFactory taxaFactory;
    private TaxaParser taxaParser;
    
    public Factory(
      SequenceBuilderFactory delegateFactory,
      TaxaFactory taxaFactory,
      TaxaParser taxaParser,
      String sciNameKey,
      String commonNameKey,
      String ncbiTaxaKey
    ) {
      this.delegateFactory = delegateFactory;
      this.taxaFactory = taxaFactory;
      this.taxaParser = taxaParser;
      this.sciNameKey = sciNameKey;
      this.commonNameKey = commonNameKey;
      this.ncbiTaxaKey = ncbiTaxaKey;
    }
    
    public SequenceBuilder makeSequenceBuilder() {
      return new OrganismParser(
        delegateFactory.makeSequenceBuilder(),
        taxaFactory,
        taxaParser,
        sciNameKey,
        commonNameKey,
        ncbiTaxaKey
      );
    }
  }
  
  private final TaxaFactory taxaFactory;
  private final TaxaParser taxaParser;
  private final String sciNameKey;
  private final String commonNameKey;
  private final String ncbiTaxaKey;
  private String fullName;
  private String commonName;
  private String ncbiTaxa;
  
  public OrganismParser(
    SequenceBuilder delegate,
    TaxaFactory taxaFactory,
    TaxaParser taxaParser,
    String sciNameKey,
    String commonNameKey,
    String ncbiTaxaKey
  ) {
    super(delegate);
    this.taxaFactory = taxaFactory;
    this.taxaParser = taxaParser;
    this.sciNameKey = sciNameKey;
    this.commonNameKey = commonNameKey;
    this.ncbiTaxaKey = ncbiTaxaKey;
  }
  
  public void addSequenceProperty(Object sciNameKey, Object value)
    throws
      ParseException
  {
    if(this.sciNameKey.equals(sciNameKey)) {
      if(fullName == null) {
        fullName = value.toString();
      } else {
        fullName = fullName + " " + value;
      }
    } else if(this.commonNameKey.equals(sciNameKey)) {
      commonName = value.toString();
    } else if(this.ncbiTaxaKey.equals(sciNameKey)) {
      String tid = value.toString();
      int eq = tid.indexOf("=");
      if(eq >= 0) {
        tid = tid.substring(eq + 1);
      }
      int sc = tid.indexOf(";");
      if(sc >= 0) {
        tid = tid.substring(0, sc);
      }
      if(this.ncbiTaxa == null) {
        this.ncbiTaxa = tid;
      } else {
        this.ncbiTaxa = this.ncbiTaxa + tid;
      }
    } else {
      getDelegate().addSequenceProperty(sciNameKey, value);
    }
  }
  
  public void endSequence()
    throws
      ParseException
  {
    try {
      Taxa taxa = taxaParser.parse(taxaFactory, fullName);
      if(commonName != null && taxa.getCommonName() == null) {
        try {
          taxa.setCommonName(commonName);
        } catch (ChangeVetoException cve) {
          throw new ParseException(cve, "Failed to build Taxa");
        }
      }
      StringTokenizer stok = new StringTokenizer(ncbiTaxa, ",");
      if(stok.countTokens() == 1) {
        taxa.getAnnotation().setProperty(EbiFormat.PROPERTY_NCBI_TAXA, ncbiTaxa);
      } else {
        List tl = new ArrayList();
        while(stok.hasMoreTokens()) {
          tl.add(stok.nextToken());
        }
        taxa.getAnnotation().setProperty(EbiFormat.PROPERTY_NCBI_TAXA, tl);
      }
      getDelegate().addSequenceProperty(PROPERTY_ORGANISM, taxa);
    } catch (ChangeVetoException cve) {
      throw new ParseException(cve, "Could not parse organism: " + fullName);
    } catch (CircularReferenceException cre) {
      throw new ParseException(cre);
    }
  }
}


