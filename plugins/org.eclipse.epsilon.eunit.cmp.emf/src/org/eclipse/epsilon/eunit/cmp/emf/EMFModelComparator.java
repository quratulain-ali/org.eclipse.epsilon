/*******************************************************************************
 * Copyright (c) 2012-2021 Antonio García-Domínguez.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Antonio García-Domínguez - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.eunit.cmp.emf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.DifferenceSource;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.diff.DefaultDiffEngine;
import org.eclipse.emf.compare.diff.DiffBuilder;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.ReferenceUtil;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.emc.emf.AbstractEmfModel;
import org.eclipse.epsilon.emc.emf.EmfModelResourceSet;
import org.eclipse.epsilon.eol.models.IAdaptableModel;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eunit.extensions.IModelComparator;

/**
 * Model comparator for EMF models, using EMF Compare 2.x.
 */
public class EMFModelComparator implements IModelComparator {

	private final class OptionBasedDiffBuilder extends DiffBuilder {
		@Override
		public void attributeChange(Match match,
				EAttribute attribute, Object value,
				DifferenceKind kind, DifferenceSource source)
		{
			if ((ignoreWhitespace || !ignoreAttributes.isEmpty())
					&& kind == DifferenceKind.CHANGE
					&& source == DifferenceSource.LEFT)
			{
				final Object o1 = ReferenceUtil.safeEGet(match.getLeft(), attribute);
				final Object o2 = ReferenceUtil.safeEGet(match.getRight(), attribute);
				
				if (o1 != null && o2 != null) {
					// attribute is set on both sides
					
					// should it be ignored as long as it is set on both sides?
					if (!ignoreAttributes.isEmpty()) {
						final String attrQualifiedName = getQualifiedName(attribute);
						if (ignoreAttributes.contains(attrQualifiedName)) {
							return;
						}
					}

					if (ignoreWhitespace && o1 instanceof String && o2 instanceof String) {
						// same after stripping whitespace?
						final String s1 = ((String)o1).replaceAll("\\s+", "");
						final String s2 = ((String)o2).replaceAll("\\s+", "");
						if (s1.equals(s2)) {
							return;
						}
					}

				}
			}

			super.attributeChange(match, attribute, value, kind, source);
		}

		@Override
		public void referenceChange(Match match, EReference reference,
				EObject value, DifferenceKind kind, DifferenceSource source) {

			// ignore MOVE changes for unordered references
			if (ignoreUnorderedMoves && !reference.isOrdered() && kind == DifferenceKind.MOVE) {
				return;
			}

			super.referenceChange(match, reference, value, kind, source);
		}

		private String getQualifiedName(ENamedElement elem) {
			if (elem.eContainer() instanceof ENamedElement) {
				return getQualifiedName((ENamedElement)elem.eContainer()) + "." + elem.getName();
			}
			else {
				return elem.getName();
			}
		}
	}

	private boolean ignoreWhitespace = false, ignoreUnorderedMoves = true;	
	private Set<String> ignoreAttributes = Collections.emptySet();

	@Override
	public boolean canCompare(IModel m1, IModel m2) {
		return adaptToEMF(m1) != null && adaptToEMF(m2) != null;
	}

	@Override
	public Object compare(IModel m1, IModel m2) throws Exception {
		final AbstractEmfModel emfM1 = adaptToEMF(m1);
		final AbstractEmfModel emfM2 = adaptToEMF(m2);
		if (emfM1 == null || emfM2 == null) {
			throw new IllegalArgumentException("Cannot compare non-EMF models");			
		}

		// For later viewing, we need to ensure the models are saved. If they are not, just
		// store them to a temporary file.
		final ResourceSet myResourceSet = cloneToTmpFiles(emfM1.getResource().getResourceSet());
		final ResourceSet otherResourceSet = cloneToTmpFiles(emfM2.getResource().getResourceSet());

		// From http://wiki.eclipse.org/EMF_Compare/Developer_Guide#Putting_it_all_together
		final IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(UseIdentifiers.NEVER);
        final IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
        matchEngineRegistry.add(matchEngineFactory);
		final EMFCompare emfCompare = EMFCompare.builder()
				.setMatchEngineFactoryRegistry(matchEngineRegistry)
				.setDiffEngine(new DefaultDiffEngine(new OptionBasedDiffBuilder()))
				.build();

		final IComparisonScope scope = new DefaultComparisonScope(myResourceSet, otherResourceSet, null);
		final Comparison cmp = emfCompare.compare(scope);

		return cmp.getDifferences().isEmpty() ? null : cmp;
	}

	private static final AbstractEmfModel adaptToEMF(IModel model) {
		if (model instanceof AbstractEmfModel) {
			return (AbstractEmfModel)model;
		}
		else if (model instanceof IAdaptableModel) {
			return ((IAdaptableModel)model).adaptTo(AbstractEmfModel.class);
		}
		else {
			return null;
		}
	}

	/**
	 * This method is used to take a snapshot of an entire resource set. Resources which
	 * do not have platform:// URIs are saved into temporary files. Cross-references are
	 * preserved: all non-platform:// URIs are rewritten to the proper temporary files.
	 */
	private static final ResourceSet cloneToTmpFiles(ResourceSet resourceSet) throws IOException {
		EcoreUtil.resolveAll(resourceSet);
		ResourceSet newResourceSet = new EmfModelResourceSet();

		// Save the original non-platform URIs
		final Map<Resource, URI> originalURIMap = new HashMap<>();
		for (Resource res : resourceSet.getResources()) {
			final String scheme = res.getURI().scheme();
			if ("platform".equals(scheme) || "pathmap".equals(scheme)) {
				// skip platform: models (they're usually metamodels) and pathmap: models (usually UML profiles)
				continue;
			}
			originalURIMap.put(res, res.getURI());
		}

		try {
			// Map each non-platform resource to a new temporary file
			for (Resource res : originalURIMap.keySet()) {
				/*
				 * It is important to build the file names for the temporary files in a way
				 * that ensures that the 70% URI minimum similarity threshold for the default
				 * EMF Compare match engine is reached. Since creating a temporary file adds
				 * an unique suffix, it might lower the value to the point in which a false
				 * negative is produced. For this reason, we will add a long common prefix to
				 * all these temporary clones.
				 */
				File tmpFile = File.createTempFile("emf-model-comparator-clone-" + res.getURI().lastSegment() + ".", ".model");
				res.setURI(URI.createFileURI(tmpFile.getAbsolutePath()));
			}

			// Save all the non-platform resources and add them to the new resource set.
			// The platform: resources will be resolved implicitly by the comparison.
			final List<Resource> newResources = new ArrayList<>(originalURIMap.size());
			for (Resource res : originalURIMap.keySet()) {
				res.save(Collections.EMPTY_MAP);
				final Resource newResource = newResourceSet.createResource(res.getURI());
				newResources.add(newResource);
			}

			// Reuse the metamodels loaded by the user in the new resource set, by
			// using the same package registry as in the original resource set.
			newResourceSet.setPackageRegistry(resourceSet.getPackageRegistry());

			// Load all the resources in the new resource set.
			for (Resource res : newResources) {
				res.load(null);
				EcoreUtil.resolveAll(res);
			}
		}
		finally {
			// Restore the original URIs to the original resource set
			for (Map.Entry<Resource, URI> entry : originalURIMap.entrySet()) {
				entry.getKey().setURI(entry.getValue());
			}
		}

		return newResourceSet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure(Map<String, Object> options) {
		if (options != null) {
			for (Map.Entry<String, Object> entry : options.entrySet()) {
				String name = entry.getKey();
				if ("whitespace".equals(name)) {
					ignoreWhitespace = "ignore".equals(entry.getValue());
				}
				else if ("unorderedMoves".equals(name)) {
					ignoreUnorderedMoves = "ignore".equals(entry.getValue());
				}
				else if ("ignoreAttributeValueChanges".equals(name)) {
					if (entry.getValue() instanceof Collection) {
						final Collection<?> col = (Collection<?>)entry.getValue();
						for (Object o : col) {
							if (!(o instanceof String)) {
								throw new IllegalArgumentException(String.format(
									"Invalid value for '%s': the collection must only have strings", name));
							}
						}
						ignoreAttributes = new HashSet<>((Collection<String>)col);
					}
					else {
						throw new IllegalArgumentException(String.format(
							"Invalid value for '%s': expected a collection of strings", name));
					}
				}
				else {
					throw new IllegalArgumentException("Unknown option '" + name + "'");
				}
			}
		}
	}	
}
