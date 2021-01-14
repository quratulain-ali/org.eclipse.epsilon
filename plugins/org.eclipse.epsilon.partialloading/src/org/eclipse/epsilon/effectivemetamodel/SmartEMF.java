package org.eclipse.epsilon.effectivemetamodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.SmartSaxParser.EffectiveMetamodelReconciler;
import org.eclipse.epsilon.SmartSaxParser.SmartSAXResourceFactory;
import org.eclipse.epsilon.SmartSaxParser.SmartSAXXMIResource;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.EmfPropertySetter;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;

public class SmartEMF extends EmfModel{

	protected String name;
	protected String nsuri;
	protected String path;
	protected ArrayList<EffectiveType> allOfType = new ArrayList<EffectiveType>();
	protected ArrayList<EffectiveType> allOfKind = new ArrayList<EffectiveType>();
	
	protected ArrayList<EffectiveType> types = new ArrayList<EffectiveType>();
	
	
	public SmartEMF()
	{
	}
	
	@Override
	public String toString() {
		return "SmartEmfModel [name=" + getName() + "]";
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public SmartEMF(String name, String nsuri)
	{
		this.name = name;
		this.nsuri = nsuri;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getNsuri() {
		return nsuri;
	}
	
	public ArrayList<EffectiveType> getAllOfType() {
		return allOfType;
	}
	
	public ArrayList<EffectiveType> getAllOfKind() {
		return allOfKind;
	}
	
	public boolean removeFromTypes(String modelElement) {
		
		EffectiveType me = new EffectiveType(modelElement);
		
		for(EffectiveType et: types)
		{
			if (et.getName().equals(modelElement)) {
				types.remove(me);
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<EffectiveType> getTypes() {
		return types;
	}
	
	public EffectiveType addToAllOfType(String modelElement)
	
	{
		for(EffectiveType et: allOfType)
		{
			if (et.getName().equals(modelElement)) {
				et.increaseUsage();
				return et;
			}
		}
		
		EffectiveType et = new EffectiveType(modelElement);
		et.setEffectiveMetamodel(this);
		allOfType.add(et);
		return et;
	}
	
	public EffectiveType addToTypes(String modelElement)
	
	{
		for(EffectiveType et: types)
		{
			if (et.getName().equals(modelElement)) {
				return et;
			}
		}
		EffectiveType et = new EffectiveType(modelElement);
		et.setEffectiveMetamodel(this);
		types.add(et);
		return et;
	}

	
	public EffectiveType addToAllOfKind(String modelElement)
	{
		for(EffectiveType et: allOfKind)
		{
			if (et.getName().equals(modelElement)) {
				return et;
			}
		}
		
		EffectiveType et = new EffectiveType(modelElement);
		et.setEffectiveMetamodel(this);
		allOfKind.add(et);
		return et;
	}
	
	public EffectiveFeature addAttributeToAllOfKind(String elementName, String attribute)
	{
		EffectiveType effectiveType = getFromAllOfKind(elementName);
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(attribute);
			effectiveType.getAttributes().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	public EffectiveFeature addReferenceToEffectiveType(String elementName, String reference)
	{
		EffectiveType effectiveType = getFromAllOfKind(elementName);
		if (effectiveType == null) {
			effectiveType = getFromAllOfType(elementName);
			if (effectiveType == null) 
				effectiveType = getFromTypes(elementName);
		}
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(reference);
			effectiveType.getReferences().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	public EffectiveFeature addAttributeToEffectiveType(String elementName, String attribute)
	{
		EffectiveType effectiveType = getFromAllOfKind(elementName);
		if (effectiveType == null) {
			effectiveType = getFromAllOfType(elementName);
			if (effectiveType == null) 
				effectiveType = getFromTypes(elementName);
		}
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(attribute);
			effectiveType.getAttributes().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	public EffectiveFeature addReferenceToAllOfKind(String elementName, String reference)
	{
		EffectiveType effectiveType = getFromAllOfKind(elementName);
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(reference);
			effectiveType.getReferences().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	
	public EffectiveFeature addAttributeToAllOfType(String elementName, String attribute)
	{
		EffectiveType effectiveType = getFromAllOfType(elementName);
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(attribute);
			effectiveType.getAttributes().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	public EffectiveFeature addAttributeToTypes(String elementName, String attribute)
	{
		EffectiveType effectiveType = getFromTypes(elementName);
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(attribute);
			effectiveType.getAttributes().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	public EffectiveFeature addReferenceToAllOfType(String elementName, String reference)
	{
		EffectiveType effectiveType = getFromAllOfType(elementName);
		if (effectiveType != null) {
			EffectiveFeature effectiveFeature = new EffectiveFeature(reference);
			effectiveType.getReferences().add(effectiveFeature);
			return effectiveFeature;
		}
		return null;
	}
	
	
	public EffectiveType getFromAllOfType(String elementName)
	{
		for(EffectiveType ef: allOfType)
		{
			if (ef.getName().equals(elementName)) {
				return ef;
			}
		}
		return null;
	}
	public EffectiveType getFromTypes(String elementName)
	{
		for(EffectiveType ef: types)
		{
			if (ef.getName().equals(elementName)) {
				return ef;
			}
		}
		return null;
	}
	public EffectiveType getFromAllOfKind(String elementName)
	{
		for(EffectiveType ef: allOfKind)
		{
			if (ef.getName().equals(elementName)) {
				return ef;
			}
		}
		return null;
	}
	
	public boolean allOfTypeContains(String modelElement)
	{
		for(EffectiveType ef: allOfType)
		{
			if (ef.getName().equals(modelElement)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean allOfKindContains(String modelElement)
	{
		for(EffectiveType ef: allOfKind)
		{
			if (ef.getName().equals(modelElement)) {
				return true;
			}
		}
		return false;
	}
	public boolean typesContains(String modelElement)
	{
		for(EffectiveType ef: types)
		{
			if (ef.getName().equals(modelElement)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void load(){
		
		long startTime = System.nanoTime();
		//String model = "/Users/sorourjahanbin/git/Epsilon_Nov2020/plugins/org.eclipse.epsilon.partialloading/src/org/eclipse/epsilon/TestUnit/Parser/flowchart2.xmi";
		
		ResourceSet resourceSet = new ResourceSetImpl();
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(metamodelUris.get(0).toString());
		resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		try {
			determinePackagesFrom(resourceSet);
		} catch (EolModelLoadingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new SmartSAXResourceFactory());
		Resource resource = resourceSet.createResource(modelUri);
		this.setResource(resource);
		
		EffectiveMetamodelReconciler effectiveMetamodelReconciler = new EffectiveMetamodelReconciler();
		effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
		effectiveMetamodelReconciler.addEffectiveMetamodel(this);
		effectiveMetamodelReconciler.reconcile();

		Map<String, Object> loadOptions = new HashMap<String, Object>();
		loadOptions.put(SmartSAXXMIResource.OPTION_EFFECTIVE_METAMODEL_RECONCILER, effectiveMetamodelReconciler);
		loadOptions.put(SmartSAXXMIResource.OPTION_LOAD_ALL_ATTRIBUTES, false);
		loadOptions.put(SmartSAXXMIResource.OPTION_RECONCILE_EFFECTIVE_METAMODELS, true);

		
		try {
			
			resource.load(loadOptions);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
			System.out.println("**** Time ****");
			System.out.println(duration/1000000+ " milliseconds");
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		System.out.println("**** Loaded Objects ****");
		System.out.println(resource.getContents().size());
		 
	}
}
