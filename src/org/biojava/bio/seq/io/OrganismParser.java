package org.biojava.bio.seq.io;

import java.io.Serializable;

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
    private String key;
    private TaxaFactory taxaFactory;
    
    public Factory(
      SequenceBuilderFactory delegateFactory,
      TaxaFactory taxaFactory,
      String key
    ) {
      this.delegateFactory = delegateFactory;
      this.taxaFactory = taxaFactory;
      this.key = key;
    }
    
    public SequenceBuilder makeSequenceBuilder() {
      return new OrganismParser(
        delegateFactory.makeSequenceBuilder(),
        taxaFactory,
        key
      );
    }
  }
  
  private final TaxaFactory taxaFactory;
  private final String key;
  private String fullName;
  
  public OrganismParser(
    SequenceBuilder delegate,
    TaxaFactory taxaFactory,
    String key
  ) {
    super(delegate);
    this.taxaFactory = taxaFactory;
    this.key = key;
  }
  
  public void addSequenceProperty(Object key, Object value)
    throws
      ParseException
  {
    if(this.key.equals(key)) {
      if(fullName == null) {
        fullName = value.toString();
      } else {
        fullName = fullName + " " + value;
      }
    } else {
      getDelegate().addSequenceProperty(key, value);
    }
  }
  
  public void endSequence()
    throws
      ParseException
  {
    try {
      Taxa taxa = taxaFactory.parseTaxa(fullName);
      getDelegate().addSequenceProperty(PROPERTY_ORGANISM, taxa);
    } catch (ChangeVetoException cve) {
      throw new ParseException(cve, "Could not parse organism: " + fullName);
    } catch (ParserException pe) {
      throw new ParseException(pe);
    }
  }
}


