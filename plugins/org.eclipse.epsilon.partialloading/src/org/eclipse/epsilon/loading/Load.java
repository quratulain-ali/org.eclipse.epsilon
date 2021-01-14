package org.eclipse.epsilon.loading;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.SmartSaxParser.EffectiveMetamodelReconciler;
import org.eclipse.epsilon.SmartSaxParser.SmartSAXResourceFactory;
import org.eclipse.epsilon.SmartSaxParser.SmartSAXXMIResource;
import org.eclipse.epsilon.effectivemetamodel.SmartEMF;
import org.eclipse.epsilon.emc.emf.EmfModel;

public class Load extends EmfModel{

	protected SmartEMF effectiveMetamodel = null;
	
	public void setEffectiveMetamodel(SmartEMF ef) {
		effectiveMetamodel = ef;
	}
	
	public SmartEMF getEffectiveMetamodel() {
		return effectiveMetamodel;
	}
	
	public void load(){
	
	ResourceSet resourceSet = new ResourceSetImpl();
	
	String path = effectiveMetamodel.getPath();
	ResourceSet ecoreResourceSet = new ResourceSetImpl();
	ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	Resource ecoreResource = ecoreResourceSet.createResource(URI.createFileURI(this.getMetamodelUris().get(0)));//new File(path).getAbsolutePath())
	
	try {
		ecoreResource.load(null);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	for (EObject o : ecoreResource.getContents()) {
		EPackage ePackage = (EPackage) o;
		resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
	}

	resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new SmartSAXResourceFactory());

	Resource resource = resourceSet.createResource(URI.createFileURI(new File(this.getModelFile()).getAbsolutePath()));

	EffectiveMetamodelReconciler effectiveMetamodelReconciler = new EffectiveMetamodelReconciler();
	effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
	effectiveMetamodelReconciler.addEffectiveMetamodel(effectiveMetamodel);
	effectiveMetamodelReconciler.reconcile();

	Map<String, Object> loadOptions = new HashMap<String, Object>();
	loadOptions.put(SmartSAXXMIResource.OPTION_EFFECTIVE_METAMODEL_RECONCILER, effectiveMetamodelReconciler);
	loadOptions.put(SmartSAXXMIResource.OPTION_LOAD_ALL_ATTRIBUTES, false);
	loadOptions.put(SmartSAXXMIResource.OPTION_RECONCILE_EFFECTIVE_METAMODELS, true);

	long startTime = System.nanoTime();
	try {
		resource.load(loadOptions);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	long endTime = System.nanoTime();

	long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
	System.out.println("**** Time ****");
	System.out.println("**** Loaded Objects ****");
	for (EObject o : resource.getContents()) {
		System.out.println(o);
	}

	System.out.println(resource.getContents().size());

	//return resource.getContents();
}
}
