package org.biojava.bio.program.unigene;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;

public class UnigeneTools {
  public static final AnnotationType UNIGENE_ANNOTATION;
  public static final AnnotationType LIBRARY_ANNOTATION;
  
  public static final UnigeneFactory FLAT_FILE_FACTORY =
    new FlatFileUnigeneFactory();
  

  private static final Map shortName2SpeciesName;
  
  static {
    shortName2SpeciesName = new HashMap();
    
    shortName2SpeciesName.put("Aga", "Anophelese gambiae");
    shortName2SpeciesName.put("Hs", "Homo sapiens");
    shortName2SpeciesName.put("Aga", "Anopheles gambiae");
    shortName2SpeciesName.put("Bt", "Bos taurus");
    shortName2SpeciesName.put("Dm", "Drosophila melanogaster");
    shortName2SpeciesName.put("Dr", "Danio rario");
    shortName2SpeciesName.put("Mm", "Mus musculus");
    shortName2SpeciesName.put("Rn", "Rattus norvegicus");
    shortName2SpeciesName.put("Xl", "Xenopus laevis");
    shortName2SpeciesName.put("At", "Arabidopsis thaliana");
    shortName2SpeciesName.put("Gma", "Glycine max");
    shortName2SpeciesName.put("Hv", "Hordeum vulgare");
    shortName2SpeciesName.put("Les", "Lycopersicon esculentum");
    shortName2SpeciesName.put("Mtr", "Medicago truncatula");
    shortName2SpeciesName.put("Os", "Oryza sativa");
    shortName2SpeciesName.put("Ta", "Triticum aestivum");
    shortName2SpeciesName.put("Zm", "Zea mays");
    
    // start to build this annotation type for .data files & UnigeneCluster
    // annotation bundles
    PropertyConstraint pc_string = new PropertyConstraint.ByClass(String.class);
    PropertyConstraint pc_int = new PropertyConstraint.ByClass(Integer.class);
    
    AnnotationType.Impl at_sts = new AnnotationType.Impl();
    at_sts.setConstraints("NAME",   pc_string, CardinalityConstraint.ONE);
    at_sts.setConstraints("ACC",    pc_string, CardinalityConstraint.ZERO_OR_ONE);
    at_sts.setConstraints("DSEG",   pc_string, CardinalityConstraint.ZERO_OR_ONE);
    at_sts.setConstraints("UNISTS", pc_string, CardinalityConstraint.ONE);
    PropertyConstraint pc_sts = new PropertyConstraint.ByAnnotationType(at_sts);
    
    AnnotationType.Impl at_txmap = new AnnotationType.Impl();
    at_txmap.setConstraints("MARKER", pc_string, CardinalityConstraint.ONE);
    at_txmap.setConstraints("RHPANEL", pc_string, CardinalityConstraint.ONE);
    PropertyConstraint pc_txmap = new PropertyConstraint.ByAnnotationType(at_txmap);
    
    AnnotationType.Impl at_protsim = new AnnotationType.Impl();
    at_protsim.setConstraints("ORG", pc_string, CardinalityConstraint.ONE);
    at_protsim.setConstraints("PROTGI", pc_string, CardinalityConstraint.ONE);
    at_protsim.setConstraints("PROTID", pc_string, CardinalityConstraint.ONE);
    at_protsim.setConstraints("PCT", pc_string, CardinalityConstraint.ONE);
    at_protsim.setConstraints("ALN", pc_int, CardinalityConstraint.ONE);
    PropertyConstraint pc_prosim = new PropertyConstraint.ByAnnotationType(at_protsim);
    
    AnnotationType.Impl at_sequence = new AnnotationType.Impl();
    at_sequence.setConstraints("ACC", pc_string, CardinalityConstraint.ONE);
    at_sequence.setConstraints("NID", pc_string, CardinalityConstraint.ONE);
    at_sequence.setConstraints("PID", pc_string, CardinalityConstraint.ZERO_OR_ONE);
    at_sequence.setConstraints("CLONE", pc_string, CardinalityConstraint.ZERO_OR_ONE);
    at_sequence.setConstraints("END", pc_string, CardinalityConstraint.ZERO_OR_ONE);
    at_sequence.setConstraints("LID", pc_string, CardinalityConstraint.ZERO_OR_ONE);
    at_sequence.setConstraints("MGC", pc_string, CardinalityConstraint.ZERO_OR_ONE);
    PropertyConstraint pc_sequence = new PropertyConstraint.ByAnnotationType(at_sequence);
    
    AnnotationType.Impl unigene = new AnnotationType.Impl();
    unigene.setConstraints("ID", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints("TITLE", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints("GENE", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints("CYTOBAND", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints("EXPRESS", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints(
      "GNM_TERMINUS",
      new PropertyConstraint.Enumeration(new Object[] { "T", "I", "S" } ),
      CardinalityConstraint.ONE);
    unigene.setConstraints("LOCUSLINK", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints("CHROMOSOME", pc_string, CardinalityConstraint.ONE);
    unigene.setConstraints("STS", pc_sts, CardinalityConstraint.ANY);
    unigene.setConstraints("TXMAP", pc_txmap, CardinalityConstraint.ANY);
    unigene.setConstraints("PROSIM", pc_prosim, CardinalityConstraint.ANY);
    unigene.setConstraints("SCOUNT", pc_int, CardinalityConstraint.ONE);
    unigene.setConstraints("SEQUENCE", pc_sequence, CardinalityConstraint.ANY);
    
    UNIGENE_ANNOTATION = unigene;
    
    AnnotationType.Impl library = new AnnotationType.Impl();
    library.setConstraints("ID", pc_string, CardinalityConstraint.ONE);
    library.setConstraints("TITLE", pc_string, CardinalityConstraint.ONE);
    library.setConstraints("TISSUE", pc_string, CardinalityConstraint.ONE);
    library.setConstraints("VECTOR", pc_string, CardinalityConstraint.ONE);
    
    LIBRARY_ANNOTATION = library;
  }
  
  public static String getSpeciesForShortName(String name) {
    return (String) shortName2SpeciesName.get(name);
  }
  
  public static ParserListener buildDataParser(TagValueListener listener)
  throws ParserException {
    try {
      LineSplitParser entryParser = (LineSplitParser) LineSplitParser.GENBANK.clone();
      entryParser.setTrimValue(true);
      entryParser.setEndOfRecord("//");
      
      ChangeTable changeT = new ChangeTable();
      changeT.setSplitter(
        "EXPRESS",
        new RegexSplitter(Pattern.compile("([^;]+)"), 1)
      );
      changeT.setChanger("ALN", ChangeTable.STRING_TO_INT);
      changeT.setChanger("SCOUNT", ChangeTable.STRING_TO_INT);
      ValueChanger changer = new ValueChanger(listener, changeT);
      
      SplitAndProp splitAndProp = new SplitAndProp(
        listener,
        Pattern.compile("(\\S+?)=([^;\\s]*)")
      );
      TagDelegator entryListener = new TagDelegator(changer);
      entryListener.setListener("STS", splitAndProp);
      entryListener.setListener("PROTSIM", splitAndProp);
      entryListener.setListener("SEQUENCE", splitAndProp);
      entryListener.setListener("TXMAP", new HandleMapInterval(listener));
      
      return new ParserListener(entryParser, entryListener);
    } catch (CloneNotSupportedException cnse) {
      throw new BioError(cnse);
    }
  }
  
  public static ParserListener buildLibInfoParser(TagValueListener listener)
  throws IOException, ParserException{
    RegexParser parser = new RegexParser();
    parser.setContinueOnEmptyTag(false);
    parser.setEndOfRecord(TagValueParser.EMPTY_LINE_EOR);
    parser.setMergeSameTag(false);
    parser.setPattern(Pattern.compile("([^=]+)=(.*)"));
    parser.setTagGroup(1);
    parser.setValueGroup(2);
    
    return new ParserListener(parser, listener);
  }  
  
  private static class SplitAndProp
  extends TagValueWrapper {
    private Pattern splitPattern;
    
    public SplitAndProp(TagValueListener delegate, Pattern splitPattern) {
      super(delegate);
      this.splitPattern = splitPattern;
    }
    
    public void value(TagValueContext tvc, Object value)
    throws ParserException {
      TagValueListener delegate = super.getDelegate();
      
      delegate.startRecord();
      
      String sv = (String) value;
      Matcher m = splitPattern.matcher(sv);
      while(m.find()) {
        String k = m.group(1);
        String v = m.group(2);
        
        delegate.startTag(k);
        delegate.value(tvc, v);
        delegate.endTag();
      }
      
      delegate.endRecord();
    }
  }
  
  private static class HandleMapInterval
  extends TagValueWrapper {
    private Pattern pattern;
    public HandleMapInterval(TagValueListener tvl) {
      super(tvl);
      pattern = Pattern.compile("([^-]+-[^;]+);\\s+\\w+=([^;]+);\\s+\\w+=(\\S+)");
    }
    
    public void value(TagValueContext tvc, Object value)
    throws ParserException {
      TagValueListener delegate = super.getDelegate();
      
      delegate.startRecord();
      
      String sv = (String) value;
      Matcher m = pattern.matcher(sv);
      if(!m.find()) {
        throw new ParserException("Could not parse line: " + sv);
      }
      
      delegate.startTag("INTERVAL");
      delegate.value(tvc, m.group(1));
      delegate.endTag();
      
      delegate.startTag("MARKER");
      delegate.value(tvc, m.group(2));
      delegate.endTag();

      delegate.startTag("RHPANEL");
      delegate.value(tvc, m.group(3));
      delegate.endTag();
      
      delegate.endRecord();
    }
  }
}
