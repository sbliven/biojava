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

package org.biojava.ontology;

import org.biojava.bio.Annotation;
import org.biojava.utils.Unchangeable;

/**
 * A triple in an ontology.  This is two terms and a relationship between
 * them, similar to RDF and other similar logic systems.
 *
 * <p>
 * For documentation purposes, a Triple may provide a name. However, a Triple
 * may also be named as "(subject, object, predicate)" if no specific name is
 * provided.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.4
 */

public interface Triple
        extends Term {
  /**
   * Return the subject term of this triple
   */

  public Term getSubject();

  /**
   * Return the object term of this triple.
   */

  public Term getObject();

  /**
   * Return a Term which defines the type of relationship between the subject and object terms.
   */

  public Term getRelation();

  /**
   * The hashcode for a Triple.
   *
   * <p>This <em>must</em> be implemented as:
   * <pre>
   * return getSubject().hashCode() +
   31 * getObject().hashCode() +
   31 * 31 * getRelation().hashCode();
   * </pre>
   * If you do not implement hashcode in this way then you have no guarantee
   * that your Triple objects will be found in an ontology and that they will
   * not be duplicated.
   * </p>
   */
  public int hashCode();

  /**
   * Check to see if an object is an equivalent Triple.
   *
   * <p>
   * Two triples are equivalent if they have the same subject, object and
   * relation fields.
   * <pre>
   * if (! (o instanceof Triple)) {
   *     return false;
   * }
   * Triple to = (Triple) o;
   * return to.getSubject() == getSubject() &&
   *        to.getObject() == getObject() &&
   *        to.getRelation() == getRelation();
   * </pre>
   * If you do not implement equals in this way then you have no guarantee
   * that your Triple objects will be found in an ontology and that they will
   * not be duplicated.
   * </p>
   */
  public boolean equals(Object obj);

  /**
   * Basic in-memory implementation of a Triple in an ontology
   *
   * @for.developer This can be used to implement Ontology.createTriple
   */

  public static final class Impl
          extends Unchangeable
          implements Triple, java.io.Serializable {
    private final Term subject;
    private final Term object;
    private final Term relation;
    private final String name;
    private final String description;

    public Impl(Term subject, Term object, Term relation) {
      this(subject, object, relation, null, null);
    }

    public Impl(Term subject,
                Term object,
                Term relation,
                String name,
                String description)
    {
      if (subject == null) {
        throw new NullPointerException("Subject must not be null");
      }
      if (object == null) {
        throw new NullPointerException("Object must not be null");
      }
      if (relation == null) {
        throw new NullPointerException("Relation must not be null");
      }

      if(
              subject.getOntology() != object.getOntology() ||
              subject.getOntology() != relation.getOntology()) {
        throw new IllegalArgumentException(
                "All terms must be from the same ontology: " +
                subject.getOntology().getName() + ", " +
                object.getOntology().getName() + ", " +
                relation.getOntology().getName());
      }

      if(name == null) {
        name = "(subject: " + subject +
                ", object: " + object +
                ", relation: " + relation + ")";
      }
      if(description == null) {
        description = "";
      }

      this.subject = subject;
      this.object = object;
      this.relation = relation;
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public Ontology getOntology() {
      return subject.getOntology();
    }

    public Term getSubject() {
      return subject;
    }

    public Term getObject() {
      return object;
    }

    public Term getRelation() {
      return relation;
    }

    public Annotation getAnnotation() {
      return Annotation.EMPTY_ANNOTATION;
    }

    /**
     * Two triples are equal if all their fields are identical.
     */

    public boolean equals(Object o) {
      if (! (o instanceof Triple)) {
        return false;
      }
      Triple to = (Triple) o;
      return to.getSubject().equals(getSubject()) &&
              to.getObject().equals(getObject()) &&
              to.getRelation().equals(getRelation());
    }

    public int hashCode() {
      return getSubject().hashCode() +
              31 * getObject().hashCode() +
              31 * 31 * getRelation().hashCode();
    }

    public String toString() {
      return name;
    }
  }
}
