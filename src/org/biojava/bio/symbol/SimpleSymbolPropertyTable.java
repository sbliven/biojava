/*
 * SimpleSymbolPropertyTable.java
 *
 * Created on December 20, 2000, 7:19 PM
 */

package org.biojava.bio.symbol;


import java.util.*;
/**
 *
 * @author  mjones
 */
public class SimpleSymbolPropertyTable implements SymbolPropertyTable {

    //Finite ? 
    private final Alphabet source;
    private String name;
    private double value;
    
    private final Map doublePropMap;
    
    public SimpleSymbolPropertyTable(Alphabet source, String name){
        this.source = source;
        this.name = name;
        doublePropMap = new HashMap();
    }
    
    public void setDoubleProperty(Symbol s, String value)
            throws IllegalSymbolException {
                        
        source.validate(s);
        doublePropMap.put(s, new Double(value));
    }

    public String getName() {
        return name;
    }
    
    public Alphabet getAlphabet() {
        return source;
    }
  
    public double getDoubleValue(Symbol s) throws IllegalSymbolException {
        source.validate(s);
        
        Double  value = (Double)doublePropMap.get(s);
        return  value.doubleValue();
    }
}
