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


package org.biojava.bio.dp;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class XmlMarkovModel {
  public static WeightMatrix readMatrix(Element root)
  throws IllegalResidueException {
    Element alphaE = (Element) root.getElementsByTagName("alphabet").item(0);
    Alphabet seqAlpha = AlphabetManager.instance().alphabetForName(
      alphaE.getAttribute("name"));
    ResidueParser symParser = seqAlpha.getParser("symbol");
    ResidueParser nameParser = seqAlpha.getParser("name");
    
    int columns = 0;
    NodeList colL = root.getElementsByTagName("col");
    for(int i = 0; i < colL.getLength(); i++) {
      int indx = Integer.parseInt(((Element) colL.item(i)).getAttribute("indx"));
      columns = Math.max(columns, indx);
    }
    
    WeightMatrix wm = new SimpleWeightMatrix(seqAlpha, columns);

    colL = root.getElementsByTagName("col");
    for(int i = 0; i < colL.getLength(); i++) {
      Element colE = (Element) colL.item(i);
      int indx = Integer.parseInt(colE.getAttribute("indx")) - 1;
      NodeList weights = colE.getElementsByTagName("weight");
      for(int j = 0; j < weights.getLength(); j++) {
        Element weightE = (Element) weights.item(j);
        String resName = weightE.getAttribute("res");
        Residue res;
        if(resName.length() > 1)
          res = nameParser.parseToken(resName);
        else
          res = symParser.parseToken(resName);
        wm.setWeight(res, indx, Math.log(Double.parseDouble(weightE.getAttribute("prob"))));
      }      
    }
    
    return wm;
  }
  
  public static MarkovModel readModel(Element root)
  throws SeqException, IllegalResidueException, IllegalAlphabetException {
    if(root.getTagName().equals("WeightMatrix")) {
      return new WMAsMM(readMatrix(root));
    }
    
    int heads = Integer.parseInt(root.getAttribute("heads"));
    Element alphaE = (Element) root.getElementsByTagName("alphabet").item(0);
    Alphabet seqAlpha = AlphabetManager.instance().alphabetForName(
      alphaE.getAttribute("name")
    );
    SimpleMarkovModel model = new SimpleMarkovModel(heads, seqAlpha);
    int [] advance = new int[heads];
    for(int i = 0; i < heads; i++) {
      advance[i] = 1;
    }
      
    ResidueParser symParser = seqAlpha.getParser("symbol");
    ResidueParser nameParser = seqAlpha.getParser("name");
    
    Map nameToState = new HashMap();
    nameToState.put("_start_", model.magicalState());
    nameToState.put("_end_", model.magicalState());
    nameToState.put("_START_", model.magicalState());
    nameToState.put("_END_", model.magicalState());
    NodeList states = root.getElementsByTagName("state");
    for(int i = 0; i < states.getLength(); i++) {
      Element stateE = (Element) states.item(i);
      String name = stateE.getAttribute("name");
      EmissionState state = StateFactory.DEFAULT.createState(seqAlpha, advance, name);
      nameToState.put(name, state);
      NodeList weights = stateE.getElementsByTagName("weight");
      for(int j = 0; j < weights.getLength(); j++) {
        Element weightE = (Element) weights.item(j);
        String resName = weightE.getAttribute("res");
        Residue res;
        if(resName.length() > 1)
          res = nameParser.parseToken(resName);
        else
          res = symParser.parseToken(resName);
        state.setWeight(res, Math.log(Double.parseDouble(weightE.getAttribute("prob"))));
      }
      model.addState(state);
    }

    NodeList transitions = root.getElementsByTagName("transition");
    for(int i = 0; i < transitions.getLength(); i++) {
      Element transitionE = (Element) transitions.item(i);
      State from = (State) nameToState.get(transitionE.getAttribute("from"));
      State to = (State) nameToState.get(transitionE.getAttribute("to"));
      double prob = Math.log(Double.parseDouble(transitionE.getAttribute("prob")));
      model.createTransition(from, to);
      try {
        model.setTransitionScore(from, to, prob);
      } catch (IllegalTransitionException ite) {
        throw new BioError(ite, "Create transition. Then couldn't set it's prob");
      }
    }
    
    return model;
  }
 
  public static void writeMatrix(WeightMatrix matrix, PrintStream out) throws Exception {
    Alphabet resA = matrix.alphabet();
    
    out.println("<MarkovModel>\n  <alphabet name=\"" + resA.getName() + "\"/>");
    
    for(int i = 0; i < matrix.columns(); i++) {
      out.println("  <col indx=\"" + (i+1) + "\">");
      for(Iterator ri = resA.residues().iterator(); ri.hasNext(); ) {
        Residue r = (Residue) ri.next();
        out.println("    <weight res=\"" + r.getName() +
                             "\" prob=\"" + matrix.getWeight(r, i) + "\"/>");
        }
      out.println("  </col>");
    }
    
    out.println("</MarkovModel>");
  }
  
  public static void writeModel(FlatModel model, PrintStream out) throws Exception {
    Alphabet stateA = model.stateAlphabet();
    Alphabet resA = model.emissionAlphabet();
    ResidueList stateR = stateA.residues();
    List stateL = stateR.toList();
    ResidueList resR = resA.residues();
    
    out.println("<MarkovModel heads=\"" + model.heads() + "\">");
    out.println("<alphabet name=\"" + resA.getName() + "\"/>");
    
    // print out states & scores
    for(Iterator stateI = stateL.iterator(); stateI.hasNext(); ) {
      EmissionState es = (EmissionState) stateI.next();
      if(! (es instanceof MagicalState)) {
        out.println("  <state name=\"" + es.getName() + "\">");
        for(Iterator resI = resR.iterator(); resI.hasNext(); ) {
          Residue r = (Residue) resI.next();
          out.println("    <weight res=\"" + r.getName() +
                               "\" prob=\"" + Math.exp(es.getWeight(r)) + "\"/>");
        }
        out.println("  </state>");
      }
    }

    // print out transitions
    for(Iterator i = stateL.iterator(); i.hasNext(); ) {
      State from = (State) i.next();
      printTransitions(model, from, out);
    }
    
    out.println("</MarkovModel>");
  }
  
  static private void printTransitions(MarkovModel model, State from, PrintStream out) throws IllegalResidueException {
    for(Iterator i = model.transitionsFrom(from).iterator(); i.hasNext(); ) {
      State to = (State) i.next();
      try {
      out.println("  <transition from=\"" + ((from instanceof MagicalState) ? "_start_" : from.getName()) +
                             "\" to=\"" + ((to instanceof MagicalState) ? "_end_" : to.getName()) +
                             "\" prob=\"" + Math.exp(model.getTransitionScore(from, to)) + "\"/>");
      } catch (IllegalTransitionException ite) {
        throw new BioError(ite, "Transition listed in transitionsFrom(" +
                           from.getName() + ") has dissapeared");
      }
    }
  }
}
