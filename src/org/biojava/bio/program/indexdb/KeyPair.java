package org.biojava.bio.program.indexdb;

interface KeyPair {
  public String getPrimary();
  
  public String getSecondary();
  
  class Impl implements KeyPair {
    private final String primary;
    private final String secondary;
    
    public Impl(String primary, String secondary) {
      this.primary = primary;
      this.secondary = secondary;
    }
    
    public String getPrimary() {
      return primary;
    }
    
    public String getSecondary() {
      return secondary;
    }
    
    public String toString() {
      return primary + ":" + secondary;
    }
  }
} 
