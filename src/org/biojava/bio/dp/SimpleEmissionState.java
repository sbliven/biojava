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

import java.io.Serializable;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

/**
 * @author Matthew Pocock
 * @author Thomas Down
 * @author Mark Schreiber
 */
public class SimpleEmissionState
  extends
    AbstractChangeable
  implements
    EmissionState,
    Serializable
{
  private Distribution dis;
  private String name;
  private Annotation ann;
  private int [] advance;
  private Alphabet matches;

  protected transient AnnotationForwarder annotationForwarder;

  public final Annotation getAnnotation() {
    return this.ann;
  }

  public final void setAnnotation(Annotation ann)
      throws ChangeVetoException{
    if(!hasListeners()) {
      this.ann = ann;
    } else {
      ChangeEvent ce = new ChangeEvent(
        this, EmissionState.ANNOTATION,
        this.ann, ann
      );
      ChangeSupport changeSupport = getChangeSupport(EmissionState.ANNOTATION);
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        this.ann = ann;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public final Distribution getDistribution() {
    return this.dis;
  }

  public final void setDistribution(Distribution dis)
  throws ChangeVetoException {
    if(!hasListeners()) {
      this.dis = dis;
    } else {
      ChangeEvent ce = new ChangeEvent(
        this, EmissionState.DISTRIBUTION,
        this.dis, dis
      );
      ChangeSupport changeSupport = getChangeSupport(EmissionState.DISTRIBUTION);
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        this.dis = dis;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public int [] getAdvance() {
    return advance;
  }

  public void setAdvance(int [] advance)
  throws ChangeVetoException {
    if(!hasListeners()) {
      this.advance = advance;
    } else {
      ChangeEvent ce = new ChangeEvent(
        this, EmissionState.ADVANCE,
        this.advance, advance
      );
      ChangeSupport changeSupport = getChangeSupport(EmissionState.DISTRIBUTION);
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        this.advance = advance;
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public char getToken() {
    return this.name.charAt(0);
  }

  public final String getName() {
    return this.name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public Alphabet getMatches() {
    return matches;
  }

  public Set getBases() {
    return Collections.singleton(this);
  }

  public List getSymbols() {
    return new SingletonList(this);
  }

  public SimpleEmissionState(
    String name,
    Annotation ann,
    int [] advance,
    Distribution dis
  ) {
    this.name = name;
    this.ann = ann;
    this.advance = advance;
    this.dis = dis;
    this.matches = new SingletonAlphabet(this);
  }

  public void registerWithTrainer(ModelTrainer trainer) {
    trainer.registerDistribution(getDistribution());
  }

  protected ChangeSupport getChangeSupport(ChangeType ct){
    ChangeSupport cs = super.getChangeSupport(ct);

    if(annotationForwarder == null &&
      (Annotatable.ANNOTATION.isMatchingType(ct) || ct.isMatchingType(Annotatable.ANNOTATION)))
    {
      annotationForwarder = new Annotatable.AnnotationForwarder(
          this,
          cs);
      getAnnotation().addChangeListener(
          annotationForwarder,
          Annotatable.ANNOTATION);
    }
    return cs;
  }

}
