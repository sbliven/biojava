/*
 * Created on 13-Nov-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.ensembl.util;

/**
 * @author arne
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ListElement {
  Object content;
  ListElement prev;
  ListElement next;
  
  public ListElement() {
    prev = this;
    next = this;
    content = null;  
  }

  public ListElement( Object content, ListElement prev, ListElement next ) {
    this.content = content;
    this.prev = prev;
    this.next = next;
  }
  
  public void moveTop( ListElement elem ) {
    // this is header, this.next is top
    elem.prev.next = elem.next;
    elem.next.prev = elem.prev;

    elem.next = next;
    elem.prev = this;

    next.prev = elem;
    this.next = elem;
  }
  
  public ListElement createTop( Object obj ) {
    ListElement elem = new ListElement( obj, this, this.next );
		next.prev = elem;
    next = elem;
    return elem;
  }
  
  public Object dropBottom() {
    if( prev != this ) {
      Object obj = prev.content;
      prev.prev.next = this;
      prev = prev.prev;
      return obj;
    } else {
      // list empty 
       return null;
    }
  }
  
  public int size() {
  	int size = 0;
  	ListElement current = this;
  	while( current.next != this ) {
  		size++;
  		current = current.next;
  	}
  	return size;
  }
  
  public String toString( int i ) {
  	ListElement current = next;
 		StringBuffer str = new StringBuffer();
    if( current.content != null ) {
      str.append( current.content.toString());
      current = current.next;
    }
  	while( current != this ) {
      str.append( ", ");
  		str.append( current.content.toString());
  		current = current.next;
  	}
  	return str.toString();
  }
  
  public String toString() {
    return content.toString();
  }
  
  public Object getContent() {
     return content;
  }
  
}
