package org.biojava.bio.taxa;

public abstract class AbstractTaxa implements Taxa {
  public boolean equals(Object o) {
    if(o instanceof Taxa) {
      Taxa t = (Taxa) o;
      
      return
        this == t || (
        this.getScientificName().equals(t.getScientificName()) &&
        this.getCommonName().equals(t.getCommonName()) &&
        this.getChildren().equals(t.getChildren())
        );
    }
    
    return false;
  }
  
  public int hashCode() {
    return getScientificName().hashCode();
  }
}

