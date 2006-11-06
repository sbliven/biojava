/*
	Copyright (C) 2003 EBI, GRL

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
import java.util.StringTokenizer;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.SequenceEdit;

/**
 * Sequence Edit Implementation.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class SequenceEditImpl
  extends AttributeImpl
  implements SequenceEdit, Comparable, Serializable {

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



  private String alternativeSequence;

  private int end;

  private int start;

  /**
   * Creates a sequence edit from the attribute.
   * 
   * attribute.value becomes sequenceEdit.editOperation.
   * @param attribute source attribute containing parameters.
   * @see #SequenceEditImpl(String, String, String, String) for attribute.value format.
   */
  public SequenceEditImpl(Attribute attribute) {
    this(
      attribute.getCode(),
      attribute.getName(),
      attribute.getDescription(),
      attribute.getValue());
  }

  /**
   * Sequence edit constructor.
   * @param code code for this sequence edit.
   * @param name name of this edit.
   * @param description description of this edit.
   * @param editOperation edit operation to be applied. Format is "start end &lt;alternativeSequence&gt;". 
   * 0<start<end+1, end>0.
   * @see org.ensembl.datamodel.SequenceEdit#getStart() for description of start value.
   * @see org.ensembl.datamodel.SequenceEdit#getEnd() for description of end value.
   * @see org.ensembl.datamodel.SequenceEdit#getAlternativeSequence() for description of alternative sequence.
   */
  public SequenceEditImpl(
    String code,
    String name,
    String description,
    String editOperation) {
    super(code, name, description, editOperation);

    StringTokenizer t = new StringTokenizer(editOperation);
    start = Integer.parseInt(t.nextToken());
    end = Integer.parseInt(t.nextToken());
    if (start < 1)
      throw new IllegalArgumentException(
        "start should be >=1 but was " + start);
    if (start > end + 1)
      throw new IllegalArgumentException(
        "start should be <= end+1 but was " + start);
    if (end < 0)
      throw new IllegalArgumentException("end should be >=1 but was " + end);

    alternativeSequence = (t.hasMoreTokens()) ? t.nextToken() : "";
  }


  /**
   * @see org.ensembl.datamodel.SequenceEdit#getStart()
   */
  public int getStart() {
    return start;
  }

  /**
   * @see org.ensembl.datamodel.SequenceEdit#getEnd()
   */
  public int getEnd() {
    return end;
  }

  /**
   * @see org.ensembl.datamodel.SequenceEdit#getAlternativeSequence()
   */
  public String getAlternativeSequence() {
    return alternativeSequence;
  }

  /**
   * @see org.ensembl.datamodel.SequenceEdit#applyEdit(java.lang.String)
   */
  public String applyEdit(String sourceSequence) {
    final int sourceLen = sourceSequence.length();
    StringBuffer buf = new StringBuffer();
    buf.append(sourceSequence.substring(0, start - 1)); // before the edit
    buf.append(alternativeSequence); // the edit
    
    if (end<sourceLen)
      buf.append(sourceSequence.substring(end, sourceLen));
    // after the edit
    return buf.toString();
  }

  /**
   * @see org.ensembl.datamodel.SequenceEdit#lengthDiff()
   */
  public int lengthDiff() {
    return alternativeSequence.length() - (end - start + 1);
  }

  /**
   * Order by ascending start values.
   * @return start - o.start
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    return start - ((SequenceEditImpl) o).start;
  }
}
