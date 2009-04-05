package org.biojava.bio.structure.io.mmcif.chem;

import java.io.Serializable;



/**
 * Enumerates the possible classifications of the residues represented by a given {@link ResidueInfo}.
 * This information is derived from the mmcif dictionary
 * @author mulvaney
 * @see Sequence#getPolymerType()
 * @see <a href="http://mmcif.rcsb.org/dictionaries/mmcif_pdbx.dic/Items/_chem_comp.type.html">link into mmCIF dictionary</a>
 */

public enum ResidueType implements Serializable {

   atomn(null, "null"), // present in db for _chem_comp.id_ = 'CFL' but not enumerated in dictionary
   dPeptideLinking(PolymerType.dpeptide, "D-peptide linking"),
   lPeptideLinking(PolymerType.peptide, "L-peptide linking"),
   glycine(PolymerType.peptide,"PEPTIDE LINKING"),
   dPeptideAminoTerminus(PolymerType.dpeptide, "D-peptide NH3 amino terminus"),
   lPeptideAminoTerminus(PolymerType.peptide, "L-peptide NH3 amino terminus"),
   dPeptideCarboxyTerminus(PolymerType.dpeptide, "D-peptide COOH carboxy terminus"),
   lPeptideCarboxyTerminus(PolymerType.peptide, "L-peptide COOH carboxy terminus"),
   dnaLinking(PolymerType.dna, "DNA linking"),
   rnaLinking(PolymerType.rna, "RNA linking"),
   dna3PrimeTerminus(PolymerType.dna, "DNA OH 3 prime terminus"),
   rna3PrimeTerminus(PolymerType.rna, "RNA OH 3 prime terminus"),
   dna5PrimeTerminus(PolymerType.dna, "DNA OH 5 prime terminus"),
   rna5PrimeTerminus(PolymerType.rna, "RNA OH 5 prime terminus"),
   dSaccharide(PolymerType.polysaccharide, "D-saccharide"),
   dSaccharide14and14linking(PolymerType.polysaccharide, "D-saccharide 1,4 and 1,4 linking"),
   dSaccharide14and16linking(PolymerType.polysaccharide, "D-saccharide 1,4 and 1,6 linking"),
   lSaccharide(PolymerType.lpolysaccharide, "L-saccharide"),
   lSaccharide14and14linking(PolymerType.lpolysaccharide, "L-saccharide 1,4 and 1,4 linking"),
   lSaccharide14and16linking(PolymerType.lpolysaccharide, "L-saccharide 1,4 and 1,6 linking"),
   saccharide(PolymerType.polysaccharide, "saccharide"),
   nonPolymer(null, "non-polymer"),
   otherChemComp(null, "other");

   ResidueType(PolymerType pt, String chem_comp_type)
   {
      this.polymerType = pt;
      this.chem_comp_type = chem_comp_type;
   }

   /**
    * The associated {@link PolymerType}
    */
   public final PolymerType polymerType;

   /**
    * String value of the type
    */
   public final String chem_comp_type;

   public static ResidueType getResidueTypeFromString(String chem_comp_type)
   {
      for(ResidueType rt : ResidueType.values())
      {
         if(rt.chem_comp_type.equalsIgnoreCase(chem_comp_type))
         {
            return rt;
         }
      }
      return null;
   }
}

/* PRESENT IN DB:
atomn
D-peptide linking
D-saccharide
D-saccharide 1,4 and 1,4 linking
DNA linking
DNA OH 3 prime terminus
dna-linking
L-peptide linking
L-saccharide
L-saccharide 1,4 and 1,4 linking
non-polymer
RNA linking
saccharide
*/

/* Enumerated in mmCIF dictionary
 * Item Value  Description
D-peptide linking    n.a.
L-peptide linking    n.a.
D-peptide NH3 amino terminus  n.a.
L-peptide NH3 amino terminus  n.a.
D-peptide COOH carboxy terminus  n.a.
L-peptide COOH carboxy terminus  n.a.
DNA linking    n.a.
RNA linking    n.a.
DNA OH 5 prime terminus    n.a.
RNA OH 5 prime terminus    n.a.
DNA OH 3 prime terminus    n.a.
RNA OH 3 prime terminus    n.a.
D-saccharide 1,4 and 1,4 linking    n.a.
L-saccharide 1,4 and 1,4 linking    n.a.
D-saccharide 1,4 and 1,6 linking    n.a.
L-saccharide 1,4 and 1,6 linking    n.a.
L-saccharide   n.a.
D-saccharide   n.a.
saccharide  n.a.
non-polymer    n.a.
other    n.a.
 *
 *
 */