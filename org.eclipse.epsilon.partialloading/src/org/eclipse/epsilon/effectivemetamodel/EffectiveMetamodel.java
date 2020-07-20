package org.eclipse.epsilon.effectivemetamodel;

import java.util.ArrayList;

public class EffectiveMetamodel {

	protected String name;
	protected String nsuri;
	protected String path;
	protected ArrayList<EffectiveType> allOfType = new ArrayList<EffectiveType>();
	protected ArrayList<EffectiveType> allOfKind = new ArrayList<EffectiveType>();
	
	protected ArrayList<EffectiveType> types = new ArrayList<EffectiveType>();
	
	public EffectiveMetamodel(String name)
	{
		this.name = name;
	}
	
	public EffectiveMetamodel(String name, String nsuri)
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
	
	
}
