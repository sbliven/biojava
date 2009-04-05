package org.biojava.bio.structure.io.mmcif;

import java.util.HashMap;
import java.util.Map;

import org.biojava.bio.structure.io.mmcif.model.ChemComp;

public class ChemicalComponentDictionary {

	private Map<String, ChemComp> dictionary;
	private Map<String,String> replaces;
	private Map<String,String> isreplacedby;

	public ChemicalComponentDictionary(){
		dictionary = new HashMap<String, ChemComp>();
		replaces   = new HashMap<String, String>();
		isreplacedby = new HashMap<String, String>();
	}

	public boolean isReplaced(ChemComp c){
		return isReplaced(c.getId());

	}
	public boolean isReplaced(String id){
		if ( isreplacedby.containsKey(id))
			return true;
		return false;
	}
	public boolean isReplacer(ChemComp c){
		return isReplacer(c.getId());
	}
	public boolean isReplacer(String id){
		if ( replaces.containsKey(id) )
			return true;
		return false;
	}

	/** if ChemComp is replaced by another one, get the newer version
	 * otherwise return the same ChemComp again.
	 * @param c
	 * @return get the component that replaced ChemComp.
	 */
	public ChemComp getReplacer(ChemComp c){
		return getReplacer(c.getId());
	}
	public ChemComp getReplacer(String id){
		if (isReplaced(id)){
			return dictionary.get(isreplacedby.get(id));
		}
		return dictionary.get(id);
	}

	/** if ChemComp is replacing another one, get the old version
	 * otherwise return the same ChemComp again.
	 * @param  c the ChemComp for which older versions should be looked up.
	 */

	public ChemComp getReplaced(ChemComp c){
		return getReplaced(c.getId());
	}
	public ChemComp getReplaced(String id){
		if (isReplacer(id)){
			return dictionary.get(replaces.get(id));
		}
		return dictionary.get(id);
	}

	/** Get the parent of a component. If component has no parent, return null
	 *
	 * @param c
	 * @return get the parent component or null if ChemComp has no parent.
	 */
	public ChemComp getParent(ChemComp c){

		if (c.hasParent()){
			return dictionary.get(c.getMon_nstd_parent_comp_id());
		}
		return null;
	}



	/** add a new component to the dictionary
	 *
	 * @param comp
	 */
	public void addChemComp(ChemComp comp){

		dictionary.put(comp.getId(),comp);
		String rep = comp.getPdbx_replaces();
		if ( (rep != null) && ( ! rep.equals("?"))){
			replaces.put(comp.getId(),rep);
		}

		String isrep = comp.getPdbx_replaced_by();
		if ( (isrep != null) && ( ! isrep.equals("?"))){
			isreplacedby.put(comp.getId(),isrep);
		}
	}
}
