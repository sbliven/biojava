package org.biojava.bio;

import java.util.*;
import org.biojava.utils.*;
import org.biojava.bio.program.tagvalue.TagMapper;

public class AnnotationRenamer
  extends AbstractAnnotation
{
  private final Annotation wrapped;
  private final TagMapper mapper;
  private final Map properties;
  
  public AnnotationRenamer(Annotation wrapped, TagMapper mapper) {
    this.wrapped = wrapped;
    this.mapper = mapper;
    this.properties = new MappedHash();
  }
  
  public Annotation getWrapped() {
    return wrapped;
  }
  
  public TagMapper getMapper() {
    return mapper;
  }
  
  public Map getProperties() {
    return properties;
  }
  
  public boolean propertiesAllocated() {
    return true;
  }
  
  private class MappedHash extends AbstractMap {
    public int size() {
      return wrapped.asMap().size();
    }
    
    public Set entrySet() {
      return new WrappedSet(wrapped.asMap().entrySet());
    }
  }
  
  private class WrappedSet extends AbstractSet {
    private Set entrySet;
    
    public WrappedSet(Set entrySet) {
      this.entrySet = entrySet;
    }
    
    public int size() {
      return entrySet.size();
    }
    
    public Iterator iterator() {
      return new Iterator() {
        Iterator i = entrySet.iterator();
        
        public boolean hasNext() {
          return i.hasNext();
        }
        
        public Object next() {
          final Map.Entry entry = (Map.Entry) i.next();
          return new Map.Entry() {
            public Object getKey() {
              return mapper.getNewTag(entry.getKey());
            }
            public Object getValue() {
              return entry.getValue();
            }
            public Object setValue(Object value) {
              return entry.setValue(value);
            }
          };
        }
        
        public void remove() {
          i.remove();
        }
      };
    }
  }
}
