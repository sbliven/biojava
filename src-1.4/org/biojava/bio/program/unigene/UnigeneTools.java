package org.biojava.bio.program.unigene;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.program.tagvalue.*;

public class UnigeneTools {
  public static final AnnotationType UNIGENE_ANNOTATION;

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
    PropertyConstraint pc_string_list = new PropertyConstraint.IsCollectionOf(
      SmallSet.class, pc_string
    );
    
    AnnotationType.Impl at_sts = new AnnotationType.Impl();
    at_sts.setPropertyConstraint("NAME", pc_string);
    //at_sts.setPropertyConstraint("ACC", pc_string);  // optional
    //at_sts.setPropertyConstraint("DSEG", pc_string); // optional
    at_sts.setPropertyConstraint("UNISTS", pc_string);
    PropertyConstraint pc_sts = new PropertyConstraint.IsCollectionOf(
      SmallSet.class,
      new PropertyConstraint.ByAnnotationType(at_sts)
    );
    
    AnnotationType.Impl at_txmap = new AnnotationType.Impl();
    at_txmap.setPropertyConstraint("MARKER", pc_string);
    at_txmap.setPropertyConstraint("RHPANEL", pc_string);
    PropertyConstraint pc_txmap = new PropertyConstraint.IsCollectionOf(
      SmallSet.class,
      new PropertyConstraint.ByAnnotationType(at_txmap)
    );
    
    AnnotationType.Impl at_protsim = new AnnotationType.Impl();
    at_protsim.setPropertyConstraint("ORG", pc_string);
    at_protsim.setPropertyConstraint("PROTGI", pc_string);
    at_protsim.setPropertyConstraint("PROTID", pc_string);
    at_protsim.setPropertyConstraint("PCT", pc_string);
    at_protsim.setPropertyConstraint("ALN", pc_int);
    PropertyConstraint pc_prosim = new PropertyConstraint.IsCollectionOf(
      SmallSet.class,
      new PropertyConstraint.ByAnnotationType(at_protsim)
    );
    
    AnnotationType.Impl at_sequence = new AnnotationType.Impl();
    at_sequence.setPropertyConstraint("ACC", pc_string);
    at_sequence.setPropertyConstraint("NID", pc_string);
    PropertyConstraint pc_sequence = new PropertyConstraint.IsCollectionOf(
      SmallSet.class,
      new PropertyConstraint.ByAnnotationType(at_sequence)
    );
    
    AnnotationType.Impl unigene = new AnnotationType.Impl();
    unigene.setPropertyConstraint("ID", pc_string);
    unigene.setPropertyConstraint("TITLE", pc_string);
    unigene.setPropertyConstraint("GENE", pc_string);
    unigene.setPropertyConstraint("CYTOBAND", pc_string);
    unigene.setPropertyConstraint("EXPRESS", pc_string);
    unigene.setPropertyConstraint("GNM_TERMINUS",
      new PropertyConstraint.Enumeration(new Object[] { "T", "I", "S" } ));
    unigene.setPropertyConstraint("LOCUSLINK", pc_string);
    unigene.setPropertyConstraint("CHROMOSOME", pc_string);
    unigene.setPropertyConstraint("STS", pc_sts);
    unigene.setPropertyConstraint("TXMAP", pc_txmap);
    unigene.setPropertyConstraint("PROSIM", pc_prosim);
    unigene.setPropertyConstraint("SCOUNT", pc_int);
    unigene.setPropertyConstraint("SEQUENCE", pc_sequence);
    
    UNIGENE_ANNOTATION = unigene;
  }
  
  public static String getSpeciesForShortName(String name) {
    return (String) shortName2SpeciesName.get(name);
  }
  
  public static ParserListener buildParser(TagValueListener listener)
  throws IOException, ParserException{
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
