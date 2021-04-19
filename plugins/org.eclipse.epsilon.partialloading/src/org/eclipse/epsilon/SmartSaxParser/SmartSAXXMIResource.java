package org.eclipse.epsilon.SmartSaxParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.eclipse.epsilon.effectivemetamodel.XMIN;

public class SmartSAXXMIResource extends XMIResourceImpl{
	
	public static final String OPTION_EFFECTIVE_METAMODELS = "effective-metamodels";
	public static final String OPTION_RECONCILE = "reconcile";
	public static final String OPTION_LOAD_ALL_ATTRIBUTES = "load-all-attributes";
	public static final String OPTION_EFFECTIVE_METAMODEL_RECONCILER = "effective-metamodel-reconciler";
	public static final String OPTION_RECONCILE_EFFECTIVE_METAMODELS = "reconcile-effective-metamodels";

	
	public boolean loadAllAttributes = true;

	
	protected HashMap<String, HashMap<String, ArrayList<String>>> objectsAndRefNamesToVisit = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> actualObjectsToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> typesToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();

	protected boolean handleFlatObjects = false;
	protected SmartSAXXMILoadImpl sxl;
	
	public void clearCollections()
	{
		objectsAndRefNamesToVisit.clear();
		objectsAndRefNamesToVisit = null;
		actualObjectsToLoad.clear();
		actualObjectsToLoad = null;
		sxl.clearCollections();
	}
	
	
	@Override
	public void load(Map<?, ?> options) throws IOException {
		
		//loadAllAttributes = (Boolean) options.get(OPTION_LOAD_ALL_ATTRIBUTES);
		
		
		ArrayList<XMIN> effectiveMetamodels = (ArrayList<XMIN>) options.get(OPTION_EFFECTIVE_METAMODELS);
		if (effectiveMetamodels != null) {
			EffectiveMetamodelReconciler effectiveMetamodelReconciler = new EffectiveMetamodelReconciler();
			effectiveMetamodelReconciler.addEffectiveMetamodels(effectiveMetamodels);
			effectiveMetamodelReconciler.addPackages(getResourceSet().getPackageRegistry().values());
			if (!(Boolean) options.get(OPTION_RECONCILE_EFFECTIVE_METAMODELS)) {
				effectiveMetamodelReconciler.reconcile();
			}
			actualObjectsToLoad = effectiveMetamodelReconciler.getActualObjectsToLoad();
			objectsAndRefNamesToVisit = effectiveMetamodelReconciler.getObjectsAndRefNamesToVisit();
			typesToLoad = effectiveMetamodelReconciler.getTypesToLoad();
		}
		else {
			EffectiveMetamodelReconciler effectiveMetamodelReconciler = (EffectiveMetamodelReconciler) options.get(OPTION_EFFECTIVE_METAMODEL_RECONCILER);
			if (effectiveMetamodelReconciler != null) {
				if (!(Boolean) options.get(OPTION_RECONCILE_EFFECTIVE_METAMODELS)) {
					effectiveMetamodelReconciler.reconcile();
				}
				actualObjectsToLoad = effectiveMetamodelReconciler.getActualObjectsToLoad();
				objectsAndRefNamesToVisit = effectiveMetamodelReconciler.getObjectsAndRefNamesToVisit();
				typesToLoad = effectiveMetamodelReconciler.getTypesToLoad();
				//actualObjectsToLoad.get("javaMM").putAll(typesToLoad.get("javaMM"));
				//typesToLoad.clear();
			}
		}
		
	//	HashMap<String,ArrayList<String>> types = typesToLoad.get("javaMM");
		 //(HashMap<String, ArrayList<String>>) typesToLoad.get("javaMM").values();
	//	 HashMap<String, ArrayList<String>> objects =actualObjectsToLoad.get("javaMM");
		 
		 
//		 for (String k : actualObjectsToLoad.get("javaMM").keySet()) {
//			 if (typesToLoad.get("javaMM").containsKey(k)) {
//				 System.out.println(actualObjectsToLoad.get("javaMM").get(k));
//			 	 System.out.println(typesToLoad.get("javaMM").get(k));
//				 for (String s : actualObjectsToLoad.get("javaMM").get(k))
//					 if (typesToLoad.get("javaMM").get(k).contains(s)) {
//						 typesToLoad.get("javaMM").get(k).remove(s);
//					 }
//				 		actualObjectsToLoad.get("javaMM").get(k).addAll(typesToLoad.get("javaMM").get(k));
//				 		typesToLoad.get("javaMM").remove(k);			 
//			 }
//		 }
		 
/*
	 HashMap<String,ArrayList<String>> size = typesToLoad.get("javaMM");
	 //(HashMap<String, ArrayList<String>>) typesToLoad.get("javaMM").values();
	 HashMap<String, ArrayList<String>> types =actualObjectsToLoad.get("javaMM");
	 
	 for (String k : types.keySet())
		 if (size.containsKey(k)) {
			// if (size.get(k).equals(types.get(k)))
				 size.remove(k);
		//	 else
				// for (String e : size.get(k)) {
					// if (types.get(k).contains(e)) {
					//	 types.get(k).remove(e);
						// if (types.get(k).isEmpty()) {
							// types.remove(k);
						//	 break;
						// }
					// }
				// }
					 
		 }
	 int num = size.size() + types.size();
	 System.out.println("Size :" + num);
	 // size.putAll(actualObjectsToLoad);
		// size.values();
//	 for (String k : size) {
//		 size.forEach(
//				 		(key, value) -> types.merge( key, value, (v1, v2) -> 
//				 		v1.equals(v2) ? v1 : v1.addAll(v2)));}
	
//		 System.out.println("SET : " + size.values().size());
//		num =actualObjectsToLoad.get("javaMM").size() + typesToLoad.get("javaMM").size();
//		System.out.println("Number of EffectiveMM elements : " + num);*/
		super.load(options);
	
	}
	
	
	public SmartSAXXMIResource(URI uri) {
		super(uri);
	}
	
	
	
	@Override
	protected XMLLoad createXMLLoad() {
		SmartSAXXMILoadImpl xmiLoadImpl = new SmartSAXXMILoadImpl(createXMLHelper());
		sxl = xmiLoadImpl;
		xmiLoadImpl.setLoadAllAttributes(loadAllAttributes);
		
		xmiLoadImpl.setObjectsAndRefNamesToVisit(objectsAndRefNamesToVisit);
		xmiLoadImpl.setActualObjectsToLoad(actualObjectsToLoad);
		xmiLoadImpl.setTypesToLoad(typesToLoad);
		return xmiLoadImpl; 
	}
	
	@Override
	protected XMLLoad createXMLLoad(Map<?, ?> options) {
		if (options != null && Boolean.TRUE.equals(options.get(OPTION_SUPPRESS_XMI)))
	    {
			SmartSAXXMILoadImpl xmiLoadImpl = new SmartSAXXMILoadImpl(new XMLHelperImpl(this));
			xmiLoadImpl.setLoadAllAttributes(loadAllAttributes);
			
			xmiLoadImpl.setObjectsAndRefNamesToVisit(objectsAndRefNamesToVisit);
			xmiLoadImpl.setActualObjectsToLoad(actualObjectsToLoad);
			xmiLoadImpl.setTypesToLoad(typesToLoad);
			return xmiLoadImpl;
	    }
	    else
	    {
	      return createXMLLoad();
	    }
	}
		
}
