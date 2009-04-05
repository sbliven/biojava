package org.biojava.bio.structure.io.mmcif.chem;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Enumerates the possible classifications of the polymers represented by a given {@link Sequence}.
 * This information is derived from the mmcif dictionary
 * @author mulvaney
 * @see Sequence#getPolymerType()
 * @see <a href="http://mmcif.rcsb.org/dictionaries/mmcif_pdbx.dic/Items/_entity_poly.type.html">link into mmCIF dictionary</a>
 */
public enum PolymerType implements Serializable
{


   /**
    * polypeptide(L)
    */
   peptide("polypeptide(L)"),

   /**
    * polypeptide(D)
    */
   dpeptide("polypeptide(D)"),

   /**
    * polydeoxyribonucleotide
    */
   dna("polydeoxyribonucleotide"),

   /**
    * polyribonucleotide
    */
   rna("polyribonucleotide"),

   /**
    * polydeoxyribonucleotide/polyribonucleotide hybrid
    */
   dnarna("polydeoxyribonucleotide/polyribonucleotide hybrid"),

   /**
    * polysaccharide(D)
    */
   polysaccharide("polysaccharide(D)"),

   /**
    * polysaccharide(L)
    */
   lpolysaccharide("polysaccharide(L)"),

   /**
    * other
    */
   otherPolymer("other"),

   /**
    * if all else fails...
    */
   unknown(null);

   PolymerType(String entity_poly_type)
   {
      this.entity_poly_type = entity_poly_type;
   }
   public final String entity_poly_type;

   public static PolymerType polymerTypeFromString(String polymerType)
   {
      for(PolymerType pt : PolymerType.values())
      {
         if(polymerType.equals(pt.entity_poly_type))
         {
            return pt;
         }
      }
      return unknown;
   }

   /**
    * Convenience <tt>Set</tt> of polymer types classified as protein.  This only contains {@link #peptide}
    */
   public static final Set<PolymerType> PROTEIN_ONLY;

   /**
    * Convenience <tt>Set</tt> of polymer types classified as DNA.  This only contains {@link #dna}
    */
   public static final Set<PolymerType> DNA_ONLY;

   /**
    * Convenience <tt>Set</tt> of polymer types classified as RNA.  This only contains {@link #rna}
    */
   public static final Set<PolymerType> RNA_ONLY;

   /**
    * Convenience <tt>Set</tt> of polymer types classified as DNA.  This contains:
    * <ul>
    * <li>{@link #dna}</li>
    * <li>{@link #rna}</li>
    * <li>{@link #dnarna}</li>
    * </ul>
    */
   public static final Set<PolymerType> POLYNUCLEOTIDE_ONLY;

   /**
    * Convenience <tt>Set</tt> of all polymer types.
    */
   public static final Set<PolymerType> ALL_POLYMER_TYPES;

   static {
      Set<PolymerType> tmp;

      tmp = new HashSet<PolymerType>();
      tmp.add(peptide);
      PROTEIN_ONLY = Collections.unmodifiableSet(tmp);

      tmp = new HashSet<PolymerType>();
      tmp.add(dna);
      DNA_ONLY = Collections.unmodifiableSet(tmp);

      tmp = new HashSet<PolymerType>();
      tmp.add(rna);
      RNA_ONLY = Collections.unmodifiableSet(tmp);

      tmp = new HashSet<PolymerType>();
      tmp.add(dna);
      tmp.add(rna);
      tmp.add(dnarna);
      POLYNUCLEOTIDE_ONLY = Collections.unmodifiableSet(tmp);

      ALL_POLYMER_TYPES = Collections.unmodifiableSet(new HashSet<PolymerType>(Arrays.asList(values())));
   }

}