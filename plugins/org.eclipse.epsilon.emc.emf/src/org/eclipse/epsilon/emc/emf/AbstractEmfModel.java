/*******************************************************************************
 * Copyright (c) 2008-2011 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 *     Antonio Garcia-Dominguez - model comparison
 *     Sina Madani - concurrency support
 ******************************************************************************/
package org.eclipse.epsilon.emc.emf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.common.concurrent.ConcurrencyUtils;
import org.eclipse.epsilon.common.util.Multimap;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.CachedResourceSet.Cache;
import org.eclipse.epsilon.emc.emf.transactions.EmfModelTransactionSupport;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolEnumerationValueNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.exceptions.models.EolNotInstantiableModelElementTypeException;
import org.eclipse.epsilon.eol.models.CachedModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.eol.models.transactions.IModelTransactionSupport;

public abstract class AbstractEmfModel extends CachedModel<EObject> {
	
	//FIXME : If the user wants, they can load it as a local copy
	
	/**
	 * One of the keys used to construct the first argument to {@link AbstractEmfModel#load(StringProperties, String)}.
	 * 
	 * When paired with "true", external references will be resolved during loading.
	 * Otherwise, external references are not resolved.
	 * 
	 * Paired with "true" by default.
	 */
	public static final String PROPERTY_EXPAND = "expand";
	
	/**
	 * Whether to perform getAllOfKind and getAllOfType operations in parallel.
	 * False by default.
	 */
	public static final String PROPERTY_PARALLELALLOF = "parallelAllOf";
	
	protected Resource modelImpl;
	protected boolean expand = true;
	protected EPackage.Registry registry;
	Map<String, EClass> eClassCache;
	/**
	 * @since 1.6
	 */
	boolean parallelAllOf;
	
	HashMap<String, Multimap> indices = new HashMap<>();
	
	public AbstractEmfModel() {
		propertyGetter = new EmfPropertyGetter();
		propertySetter = new EmfPropertySetter();
	}
	
	@Override
	protected synchronized void initCaches() {
		super.initCaches();
		if (isConcurrent()) {
			eClassCache = eClassCache != null ?
				ConcurrencyUtils.concurrentMap(eClassCache) : ConcurrencyUtils.concurrentMap();
		}
		else {
			eClassCache = eClassCache != null ?
				new HashMap<>(eClassCache) : new HashMap<>();
		}
	}
	
	protected InputStream getInputStream(String file) throws IOException {
		if (file.startsWith("bundleresource:") || file.startsWith("platform:")) {
			URL url = new URL(file);
			return url.openConnection().getInputStream();
		}
		else {
			return new FileInputStream(file);
		}
	}
	
	public void addMetamodelUri(String nsUri) {
		getPackageRegistry().put(nsUri, EPackage.Registry.INSTANCE.get(nsUri));
	}

	/**
	 * Get the (cached) package registry belonging to the model implementation,
	 * if no registry is available the global one is provided
	 * 
	 * @return the (global) package registry
	 */
	protected EPackage.Registry getPackageRegistry() {
		if (registry == null) {
			if (modelImpl == null || modelImpl.getResourceSet() == null) {
				registry = EPackage.Registry.INSTANCE;
				//new EPackageRegistryImpl();
				//registry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
			}
			else {
				// By default getPackageRegistry() returns/creates a registry backed by the global registry
				registry = modelImpl.getResourceSet().getPackageRegistry();
			}
		}
		
		return registry;
	}
	
	@Override
	public Object getEnumerationValue(String enumeration, String label) throws EolEnumerationValueNotFoundException {
		
		for (Object pkg : getPackageRegistry().values()) {

			if (EmfUtil.isEPackageOrDescriptor(pkg)) {
				EPackage ePackage = EmfUtil.toEPackage(pkg);
				
				for (EClassifier classifier : EmfUtil.getAllEClassifiers(ePackage)) {
				//for (EClassifier classifier : ePackage.getEClassifiers()) {
					if (classifier instanceof EEnum && 
							(((EEnum) classifier).getName().equals(enumeration) ||
							getFullyQualifiedName(classifier).equals(enumeration))){
						EEnum eEnum = (EEnum) classifier;
						EEnumLiteral literal = eEnum.getEEnumLiteral(label);
						
						if (literal != null) return literal.getInstance();
					}
				}
			}
		}
		
		throw new EolEnumerationValueNotFoundException(enumeration, label, this.getName());
	}
	
	@Override
	public boolean knowsAboutProperty(Object instance, String property) {
		if (!owns(instance)) return false;
		EObject eObject = (EObject) instance;		
		return knowsAboutProperty(eObject, property);
	}

	protected boolean knowsAboutProperty(EObject instance, String property) {
		return EmfUtil.getEStructuralFeature(instance.eClass(), property) != null;
	}

	@Override
	public boolean isPropertySet(Object instance, String property) throws EolRuntimeException {
		if (!owns(instance)) return false;
		EObject eObject = (EObject) instance;
		EStructuralFeature sf = EmfUtil.getEStructuralFeature(eObject.eClass(), property);
		return eObject.eIsSet(sf);
	}

	/**
	 * 
	 * @param criteria
	 * @param parallel
	 * @return
	 * @throws EolModelElementTypeNotFoundException
	 * @since 1.6
	 */
	Collection<EObject> getAllFromModel(Predicate<EObject> criteria) throws EolModelElementTypeNotFoundException {
		return StreamSupport.stream(
				allContents().spliterator(), parallelAllOf
			)
			.filter(criteria)
			.collect(Collectors.toList());
	}
	
	//TODO : Throw the exception if size == 0 check the allContents for the class
	@Override
	protected Collection<EObject> getAllOfTypeFromModel(String type) throws EolModelElementTypeNotFoundException {
		final EClass eClass = classForName(type);
		return getAllFromModel(eObject ->
			eObject.eClass() == eClass
		);
	}
	
	@Override
	protected Collection<EObject> getAllOfKindFromModel(String kind) throws EolModelElementTypeNotFoundException {
		final EClass eClass = classForName(kind);
		return getAllFromModel(eClass::isInstance);
	}
	
	public Object findByIndex(String kind, String field, String value) throws EolModelElementTypeNotFoundException {
		
		if(indices.get(kind+","+field)!=null){
			return find(indices.get(kind+","+field), value);
		}
		else {
			return find(createIndex(kind, field), value);
		}
	}
	
	public Multimap<Object ,String> createIndex(String kind, String field) throws EolModelElementTypeNotFoundException {
		
		final EClass eClass = classForName(kind);
		
		Multimap<Object ,String> index=new Multimap<Object,String>(); //Creating HashMap
		for (EObject s : getAllOfTypeFromModel(kind)) {
		        index.put(s.eGet(eClass.getEStructuralFeature(field)), getElementId(s));
		   }
		indices.put(kind+","+field, index);		
		return index;
	}
	
   public Object find(Multimap<Object ,String> index, String value) throws EolModelElementTypeNotFoundException {
		List<Object> elements = new ArrayList<Object>();
	   if(index.get(value) == null) return elements;
	   for(String val : index.get(value)) {
	   elements.add(getElementById(val));
	   }
	   return elements;
	}
	
	@Override
	public Object getCacheKeyForType(String type) throws EolModelElementTypeNotFoundException {
		return classForName(type);
	}

	public EClass classForName(String name) throws EolModelElementTypeNotFoundException {
		if (name != null) {
			EClass eClass = eClassCache.get(name);
			if (eClass != null) {
				return eClass;
			}
			else if ((eClass = classForName(name, getPackageRegistry())) != null) {
				eClassCache.put(name, eClass);
				return eClass;
			}
		}
 		
		throw new EolModelElementTypeNotFoundException(this.name, name);
	}
	
	protected EClass classForName(String name, EPackage.Registry registry) {	
		boolean absolute = name.contains("::");
		
		return registry.values()
			.stream()
			.filter(pkg -> pkg instanceof EPackage)
			.map(pkg -> classForName(name, absolute, (EPackage) pkg))
			.filter(eClass -> eClass != null)
			.findAny()
			.orElse(null);
	}

	protected EClass classForName(String name, boolean absolute, EPackage pkg) {
		for (EClassifier eClassifier : EmfUtil.getAllEClassifiers(pkg)) {
			if (eClassifier instanceof EClass) {
				String eClassifierName = absolute ?
					getFullyQualifiedName(eClassifier) : eClassifier.getName();
				
				if (eClassifierName.equals(name)) {
					return (EClass) eClassifier;
				}
			}
		}
		return null;
	}
	
	@Override
	protected Collection<EObject> allContentsFromModel() {
		final Collection<EObject> allInstances = new LinkedList<>();
		
		for (Resource resource : getResources()) {
			for (
				Iterator<EObject> it = EcoreUtil.getAllContents(resource, expand);
				it.hasNext();
				allInstances.add(it.next())
			);
		}
		
		return allInstances;
	}
	
	@Override
	protected EObject createInstanceInModel(String type) throws EolModelElementTypeNotFoundException, EolNotInstantiableModelElementTypeException {
		EClass eClass = classForName(type);
		if (eClass.isAbstract()) {
			throw new EolNotInstantiableModelElementTypeException(this.name, type);
		}

		EObject instance = eClass.getEPackage().getEFactoryInstance().create(eClass);
		modelImpl.getContents().add(instance);
		instance.eAdapters().add(new ContainmentChangeAdapter(instance, modelImpl));
		return instance;
	}
	
	protected EmfModelTransactionSupport transactionSupport;
	
	@Override
	public IModelTransactionSupport getTransactionSupport() {
		if (transactionSupport == null) {
			transactionSupport = new EmfModelTransactionSupport(this);
		}
		return transactionSupport;
	}
		
	@Override
	protected boolean deleteElementInModel(Object instance) throws EolRuntimeException {		
		if (!(instance instanceof EObject))
			return false;
		
		EObject eObject = (EObject) instance;
		EcoreUtil.delete(eObject);
		
		List<EObject> contents = new ArrayList<>(eObject.eContents());
		
		for (EObject content : contents) {
			deleteElement(content);
		}
		contents.clear();
		return true;
	}

	@Override
	public boolean owns(Object instance) {	
		if (instance instanceof EObject) {
			EObject eObject = (EObject) instance;
			Resource eObjectResource = eObject.eResource();
			
			if (eObjectResource == null) return false;
			
			if (expand) {
				return modelImpl.getResourceSet() == eObjectResource.getResourceSet();
			}
			else {
				return modelImpl == eObjectResource;
			}
		}
		
		return false;
	}

	@Override
	public boolean store(String fileName) {
		return store(EmfUtil.createPlatformResourceURI(fileName));
	}
	
	// If expand == true, save the other resources in the resource set as well
	// See how we can run store inside a WorkbenchModificationOperation
	public boolean store(URI uri) {
		modelImpl.setURI(uri);
		return store();
	}
	
	public boolean store(OutputStream os) {
		if (modelImpl == null) return false;
		
		try {
			modelImpl.save(os, null);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void disposeModel() {
		registry = null;
		if (modelImpl != null) {
			Cache cache = CachedResourceSet.getCache();
			if (modelImpl.getResourceSet() != null) {
				for (Resource r : modelImpl.getResourceSet().getResources()) {
					cache.returnResource(r);
				}
			}
			else {
				cache.returnResource(modelImpl);
			}
			modelImpl = null;
		}

		eClassCache.clear();
	}

	public Resource getResource() {
		return modelImpl;
	}
	
	public void setResource(Resource resource) {
		this.modelImpl = resource;
	}
	
	/**
	 * @deprecated Use {@link #getResource()} instead
	 */
	@Deprecated
	public Resource getModelImpl() {
		return modelImpl;
	}
	
	/**
	 * @deprecated Use {@link #setResource(Resource)} instead
	 */
	public void setModelImpl(Resource modelImpl) {
		this.modelImpl = modelImpl;
	}

	protected List<Resource> getResources() {
		final List<Resource> resources = new ArrayList<>();
		final ResourceSet rs = modelImpl.getResourceSet();
		if (expand && rs != null) {
			EList<Resource> rawResources = rs.getResources();

			for (Resource rawResource : rawResources) {
				// Is this model contained within another resource in the same resource set?
				boolean contained = true;

				for (EObject root : rawResource.getContents()) {
					EObject container = root.eContainer();
					if (container == null) {
						// At least one of the roots is not contained within some other model in the set
						contained = false;
						break;
					}
				}

				if (!contained) {
					resources.add(rawResource);
				}
			}
		}
		else {
			resources.add(modelImpl);
		}
		return resources;
	}

	@Override
	public Object getElementById(String id) {
		for (Resource resource : getResources()) {
			Object instance = resource.getEObject(id);
			if (instance != null) return instance;
		}
		return null;
	}

	@Override
	public String getElementId(Object instance) {
		EObject eObject = (EObject) instance;
		
		/*
		if (eObject.eResource() instanceof XMIResource){
			String id = ((XMIResource) eObject.eResource()).getID(eObject);
			if (id != null && id.trim().length() > 0) return id;
		}
		
		if (eObject.eResource() instanceof XMLResource){
			return ((XMLResource) eObject.eResource()).getURIFragment(eObject);
		}
		
		return "";*/
		
		return eObject.eResource().getURIFragment(eObject);
		
	}
	
	@Override
	public void setElementId(Object instance, String newId) {
		if (newId == null || newId.isEmpty())
			return;
		
		/* I think that, quite sensibly, EMF does not all URI
		 * fragments to be set on an XML resource. Also, we
		 * don't want a URI fragment to be used as an XMI ID
		 */
		if (isUriFragment(newId))
			return;
		
		
		EObject eObject = (EObject) instance;
		
		if (eObject.eResource() instanceof XMIResource) {
			((XMIResource) eObject.eResource()).setID(eObject, newId);
		}
	}
	
	private boolean isUriFragment(String newId) {
		return newId.startsWith("/") || newId.startsWith("#/");
	}
	
	@Override
	public Object getTypeOf(Object instance) {
		return ((EObject) instance).eClass();
	}
	
	@Override
	public String getTypeNameOf(Object instance) {
		if (!isModelElement(instance))
			throw new IllegalArgumentException("Not a valid EMF model element: " + instance + " (" + instance.getClass().getCanonicalName() + ") ");
	
		return ((EClass)getTypeOf(instance)).getName();
	}
	
	@Override
	public String getFullyQualifiedTypeNameOf(Object instance) {
		if (!isModelElement(instance))
			throw new IllegalArgumentException("Not a valid EMF model element: " + instance + " (" + instance.getClass().getCanonicalName() + ") ");
		
		return getFullyQualifiedName(((EClass)getTypeOf(instance)));
	}
	
	@Override
	public Collection<String> getAllTypeNamesOf(Object instance) {
		ArrayList<String> allTypeNames = new ArrayList<>(0);

		if (isModelElement(instance)) {
			EClass type = (EClass)getTypeOf(instance);
			Collection<EClass> superTypes = type.getEAllSuperTypes();
			
			allTypeNames.ensureCapacity(1+superTypes.size());
			
			allTypeNames.add(getFullyQualifiedName(type));

			for (EClass supertype : superTypes) {
				allTypeNames.add(getFullyQualifiedName(supertype));
			}
		}
		
		return allTypeNames;
	}

	@Override
	public boolean isInstantiable(String type) {
		try {
			return !classForName(type).isAbstract();
		} catch (EolModelElementTypeNotFoundException e) {
			return false;
		}
	}
	
	@Override
	public boolean hasType(String type) {
		try {
			// Speed things up
			if (eClassCache.containsKey(type)) return true;
			
			classForName(type);
			return true;
		} catch (EolModelElementTypeNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Determines whether this model has an adapter matching the specified type.
	 * 
	 * @param adapterType The adapter class.
	 * @return <code>true</code> if this model's adapters contains the specified adapter type.
	 * @since 1.6
	 */
	protected boolean hasAdapter(Class<? extends EContentAdapter> adapterType) {
		return modelImpl.eAdapters().stream().anyMatch(adapterType::isInstance);
	}
	
	protected String getFullyQualifiedName(EClassifier eClassifier) {
		String fullyQualifiedName = eClassifier.getName();
		for (EPackage parent = eClassifier.getEPackage(); parent != null; parent = parent.getESuperPackage()) {
			fullyQualifiedName = parent.getName() + "::" + fullyQualifiedName;
		}
		return fullyQualifiedName;
	}

	@Override
	public boolean isModelElement(Object instance) {
		return instance instanceof EObject;
	}

	@Override
	public boolean isOfKind(Object instance, String metaClass)
			throws EolModelElementTypeNotFoundException {
		
		EClass eClass = classForName(metaClass);
		return eClass.isInstance(instance);
	}

	@Override
	public boolean isOfType(Object instance, String metaClass)
			throws EolModelElementTypeNotFoundException {
		
		EClass eClass = classForName(metaClass);
		
		if (instance instanceof EObject) {
			EObject eObject = (EObject) instance;
			return eClass == eObject.eClass();
		}
		return false;
	}
	
	public Object getContainerOf(Object object) {
		if (object instanceof EObject) {
			return ((EObject) object).eContainer();
		}
		
		return null;
	}
	
	public boolean isExpand() {
		return expand;
	}
	
	public void setExpand(boolean expand) {
		this.expand = expand;
	}
	
	/**
	 * 
	 * @param parallel
	 * @since 1.6
	 */
	public void setParallelAllOf(boolean parallel) {
		this.parallelAllOf = parallel;
	}
	
	/**
	 * 
	 * @return
	 * @since 1.6
	 */
	public boolean isParallelAllOf() {
		return parallelAllOf;
	}
	
	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver) throws EolModelLoadingException {
		super.load(properties, resolver);
		setParallelAllOf(properties.getBooleanProperty(PROPERTY_PARALLELALLOF, parallelAllOf));
		setExpand(properties.getBooleanProperty(PROPERTY_EXPAND, expand));
	}
	
	/**
	 * @since 1.6
	 */
	@Override
	public boolean isLoaded() {
		return modelImpl != null && modelImpl.isLoaded();
	}
}
