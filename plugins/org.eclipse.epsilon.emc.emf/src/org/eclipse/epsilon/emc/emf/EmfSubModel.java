package org.eclipse.epsilon.emc.emf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.common.util.Multimap;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;

public class EmfSubModel extends EmfModel {
	
	HashMap<String, Multimap<Object, String>> indices = new HashMap<>();
	
	public Object findByIndex(String kind, String field, Object value) throws EolModelElementTypeNotFoundException {
		return find(indices.get(kind+","+field), value);
}

public Multimap<Object ,String> createIndex(String type, String field) throws EolModelElementTypeNotFoundException {
	
	EClass eClass = classForName(type);
	Multimap<Object ,String> index=new Multimap<Object,String>(); //Creating HashMap
	EStructuralFeature feature =eClass.getEStructuralFeature(field);
	Collection<EObject> all = getAllFromModel(eClass::isInstance);
	
	for (EObject s : all) {
	    index.put(s.eGet(feature), getElementId(s));
	}
    
	indices.put(type+","+field, index);		
	return index;
}

public Object find(Multimap<Object ,String> index, Object value) throws EolModelElementTypeNotFoundException {
	List<Object> elements = new ArrayList<Object>();
   if(index.get(value) == null) return elements;
   for(String val : index.get(value)) {
   elements.add(getElementById(val));
   }
   return elements;
}

}
