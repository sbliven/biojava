/*
    Copyright (C) 2001 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.datamodel.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.SequenceEdit;

/**
 * Helper class used to manage attributes and derived 
 * sequence edits.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class AttributesHelper implements Serializable {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



  private Set sequenceCodes = new HashSet();

  private List attributes = new ArrayList();

  private List sequenceEdits = null;

  /**
   * Construct SequenceEdits from those attributes with a
   * code that appears in codes.
   * @param sequenceCodes codes of attributes that are to be used
   * to construct SequenceEdits.
   */
  public AttributesHelper(String[] sequenceCodes) {
    for (int i = 0; i < sequenceCodes.length; i++)
      this.sequenceCodes.add(sequenceCodes[i]);

  }

  public List getSequenceEdits() {

    if (sequenceEdits == null) {
      if (attributes.size() == 0) {
        sequenceEdits = Collections.EMPTY_LIST;
      } else {
        sequenceEdits = new ArrayList();
        for (int i = 0, n = attributes.size(); i < n; i++) {
          Attribute a = (Attribute) attributes.get(i);
          if (sequenceCodes.contains(a.getCode()))
            sequenceEdits.add(new SequenceEditImpl(a));
        }
      }
      // ensure return order is correct
      Collections.sort(sequenceEdits);
    }

    return new ArrayList(sequenceEdits);
  }

  /**
   * Returns a copy of sequence that has had the edits applied.
   * @param sequence source sequence to be modified
   * @return new sequence that is the original sequence
   * after applying the edits.
   */
  public String applyEdits(String sequence) {

    String r = sequence;

    List edits = getSequenceEdits();

    if (edits != null && edits.size() > 0) {
      // reversing the order by start position means 
      // that the edits can be applied to the sequence
      // without complicated side effects. If we didn't
      // do this the early edits would have to be taken
      // into account by the later ones.
      Collections.reverse(edits);
      for (int i = 0, n = edits.size(); i < n; i++) {
        SequenceEdit se = (SequenceEdit) edits.get(i);
        r = se.applyEdit(r);
      }
    }

    return r;
  }

  public List getAttributes() {
    if (attributes == Collections.EMPTY_LIST)
      return attributes;
    else
      return new ArrayList(attributes);
  }

  public void addAttribute(Attribute attribute) {
    if (attributes == Collections.EMPTY_LIST)
      attributes = new ArrayList();
    attributes.add(attribute);
    sequenceEdits = null;
  }

  public boolean removeAttribute(Attribute attribute) {
    boolean r = attributes.remove(attribute);
    if (r)
      sequenceEdits = null;
    return r;
  }

  public List getAttributes(String code) {
    List r = new ArrayList();
    for (int i = 0, n = attributes.size(); i < n; i++) {
      Attribute a = (Attribute) attributes.get(i);
      if (a.getCode().equals(code))
        r.add(a);
    }
    return r;
  }
}
