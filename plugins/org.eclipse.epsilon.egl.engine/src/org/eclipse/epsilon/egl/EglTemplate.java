/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Louis Rose - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.egl;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
import org.eclipse.epsilon.egl.execute.context.IEglContext;
import org.eclipse.epsilon.egl.execute.control.ITemplateExecutionListener;
import org.eclipse.epsilon.egl.formatter.CompositeFormatter;
import org.eclipse.epsilon.egl.formatter.Formatter;
import org.eclipse.epsilon.egl.formatter.NullFormatter;
import org.eclipse.epsilon.egl.incremental.IncrementalitySettings;
import org.eclipse.epsilon.egl.internal.EglModule;
import org.eclipse.epsilon.egl.internal.IEglModule;
import org.eclipse.epsilon.egl.merge.Merger;
import org.eclipse.epsilon.egl.spec.EglTemplateSpecification;
import org.eclipse.epsilon.egl.spec.EglTemplateSpecificationFactory;
import org.eclipse.epsilon.egl.status.ProtectedRegionWarning;
import org.eclipse.epsilon.egl.traceability.Template;
import org.eclipse.epsilon.eol.dom.Import;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dom.OperationList;

public class EglTemplate {

	protected final IEglModule module;
	protected final String name;
	protected final Template template;
	protected final Collection<ITemplateExecutionListener> listeners;
	
	private IncrementalitySettings incrementalitySettings;
	private Formatter formatter;
	private String contents = "";
	private boolean processed = false;
	
	// For tests
	EglTemplate(URI path, IEglContext context) throws Exception {
		this(new EglTemplateSpecificationFactory(new NullFormatter(), new IncrementalitySettings()).fromResource(path.toString(), path), context);
	}

	public EglTemplate(EglTemplateSpecification spec, IEglContext context) throws Exception {
		this(spec.getName(), context, spec.createTemplate(), spec.getDefaultFormatter(), spec.getIncrementalitySettings(), spec.getTemplateExecutionListeners());
		spec.parseInto(module);
	}
	
	private EglTemplate(String name, IEglContext context, Template template, Formatter formatter, IncrementalitySettings incrementalitySettings, Collection<ITemplateExecutionListener> listeners) {
		this.module = new EglModule(context);
		this.name     = name;
		this.template = template;
		this.formatter = formatter;
		this.incrementalitySettings = incrementalitySettings;
		this.listeners = listeners;
	}
	
	public String getName() {
		return name;
	}
	
	public void populate(String name, Object value) {
		template.addVariable(name, value);
	}
	
	public String process() throws EglRuntimeException {
		for (ITemplateExecutionListener listener : listeners) {
			listener.aboutToProcess(this);
		}
		
		contents = Objects.toString(module.execute(this, formatter), "");
		processed = true;

		for (ITemplateExecutionListener listener : listeners) {
			listener.finishedProcessing(this);
		}
		
		return contents;
	}
	
	public String merge(String existing) throws EglRuntimeException {
		if (!isProcessed()) process();
		 
		final Merger merger = new Merger(module.getContext().getPartitioner(), contents, existing);
		
		final String result = merger.merge();
		
		for (ProtectedRegionWarning warning : merger.getMergeWarnings()) {
			addProtectedRegionWarning(warning);
		}
		
		return result;
	}
	
	protected void addProtectedRegionWarning(ProtectedRegionWarning warning) {
		module.getContext().addStatusMessage(warning);
	}
	
	protected String getContents() {
		return contents;
	}
	
	protected boolean isProcessed() {
		return processed;
	}
	
	public Formatter getFormatter() {
		return formatter;
	}
	
	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}
	
	public IncrementalitySettings getIncrementalitySettings() {
		return incrementalitySettings;
	}
	
	public void setIncrementalitySettings(IncrementalitySettings incrementalitySettings) {
		this.incrementalitySettings = incrementalitySettings;
	}
	
	public void setFormatters(Formatter... formatters) {
		setFormatters(Arrays.asList(formatters));
	}
	
	public void setFormatters(Collection<Formatter> formatters) {
		setFormatter(new CompositeFormatter(formatters));
	}

	public Template getTemplate() {
		return template;
	}
	
	public IEglModule getModule() {
		return module;
	}
	
	public List<ParseProblem> getParseProblems() {
		return module.getParseProblems();
	}

	public List<ModelDeclaration> getDeclaredModelDefinitions() {
		return module.getDeclaredModelDeclarations();
	}

	public OperationList getDeclaredOperations() {
		return module.getDeclaredOperations();
	}

	public List<Import> getImports() {
		return module.getImports();
	}

	public Set<ModelDeclaration> getModelDefinitions() {
		return module.getModelDelcarations();
	}

	public OperationList getOperations() {
		return module.getOperations();
	}

	protected void printWarning(String message) {
		module.getContext().getWarningStream().println(message);
	}
	
	public void reset() {
		processed = false;
		template.reset();
		contents = "";
		((EglModule) module).clearCache();
	}
}
