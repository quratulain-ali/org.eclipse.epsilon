/*********************************************************************
 * Copyright (c) 2018 The University of York.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.common.launch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.epsilon.common.util.OperatingSystem;
import org.eclipse.epsilon.common.util.profiling.ProfileDiagnostic;
import static org.eclipse.epsilon.common.util.profiling.BenchmarkUtils.*;
import org.eclipse.epsilon.common.util.profiling.ProfileDiagnostic.MemoryUnit;

/**
 * Generic utility class for building standalone applications with support for:
 * - Multi-stage profiling <br/>
 * - Writing to output files <br/>
 * - Identifying and comparing configurations <br/>
 * - Displaying results <br/>
 * - Building configurations using an elegant builder syntax <br/>
 * ... and more <br/>
 * <br/>
 * The intention is that this class is used as a base for building more complex
 * standalone run configurations which can be built and invoked easily (at least,
 * from the client's perspective!).
 * 
 * @author Sina Madani
 * @since 1.6
 */
public abstract class ProfilableRunConfiguration implements Runnable {
	
	protected String printMarker = "-----------------------------------------------------";
	protected int id;
	public final boolean showResults, profileExecution;
	public final Path script, outputFile;
	protected final Collection<ProfileDiagnostic> profiledStages;
	protected boolean hasRun = false;
	protected Object result;
	
	@SuppressWarnings("unchecked")
	public abstract static class Builder<C extends ProfilableRunConfiguration, B extends Builder<C, B>> {
		protected Class<C> configClass;
		
		public Integer id;
		public boolean showResults, profileExecution;
		public Path script, outputFile;
		
		public Builder() {
			this(null);
		}
		public Builder(Class<C> runConfigClass) {
			setConfigClass(runConfigClass);
		}
		private void setConfigClass(Class<C> runConfigClass) {
			this.configClass = runConfigClass != null ? runConfigClass :
				(Class<C>) getClass().getDeclaringClass();
		}

		public abstract C build() throws IllegalArgumentException, IllegalStateException;
		
		protected C buildReflective(Supplier<? extends C> alternative) throws IllegalStateException {
			if (Modifier.isAbstract(this.configClass.getModifiers())) {
				if (alternative == null) {
					throw new IllegalStateException(
						"Impossible build for class '"+configClass.getName()+"' and no concrete implementation!"
					);
				}
				return alternative.get();
			}
			
			try {
				return (C) Stream.of(configClass.getConstructors())
					.filter(constructor -> constructor.getParameterCount() == 1)
					.filter(constructor -> Stream.of(constructor.getParameterTypes())
						.filter(clazz -> clazz.getName().contains(getClass().getSimpleName()))
						.findAny().isPresent()
					)
					.findAny()
					.orElseThrow(() -> new NoSuchMethodException("Couldn't find builder constructor for class '"+configClass.getName()+"'."))
				.newInstance(this);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				if (alternative != null) return alternative.get();
				else throw new IllegalStateException(ex);
			}
		}
		
		  public B with(Consumer<B> builderFunction) {
			  builderFunction.accept((B) this);
			  return (B) this;
		  }
		
		public B withScript(Path scriptPath) {
			this.script = scriptPath;
			return (B) this;
		}
		public B withScript(File file) {
			return withScript(file.toPath());
		}
		public B withScript(String path) {
			return withScript(Paths.get(path));
		}
		
		public B withOutputFile(Path output) {
			this.outputFile = output;
			return (B) this;
		}
		
		public B withId(int id) {
			this.id = id;
			return (B) this;
		}
		
		public B withResults() {
			return showResults(true);
		}
		public B showResults() {
			return showResults(true);
		}
		public B showResults(boolean show) {
			this.showResults = show;
			return (B) this;
		}
		
		public B withProfiling() {
			return profileExecution(true);
		}
		public B profileExecution() {
			return profileExecution(true);
		}
		public B profileExecution(boolean profile) {
			this.profileExecution = profile;
			return (B) this;
		}
	}
	
	protected ProfilableRunConfiguration(Builder<?, ?> builder) {
		if (builder == null) throw new IllegalArgumentException("Builder shouldn't be null!");
		this.script = builder.script;
		this.profileExecution = builder.profileExecution;
		this.showResults = builder.showResults;
		this.outputFile = builder.outputFile;
		this.profiledStages = new LinkedList<>();
		this.id = Optional.ofNullable(builder.id).orElseGet(() -> Objects.hash(Objects.toString(script)));
	}
	
	protected ProfilableRunConfiguration(ProfilableRunConfiguration other) {
		this.script = other.script;
		this.showResults = other.showResults;
		this.profileExecution = other.profileExecution;
		this.id = other.id;
		this.outputFile = other.outputFile;
		this.result = other.result;
		this.profiledStages = other.profiledStages;
	}
	
	@Override
	public final void run() {
		try {
			preExecute();
			result = execute();
			postExecute();
		}
		catch (Exception ex) {
			handleException(ex);
		}
		hasRun = true;
	}
	
	protected void handleException(Exception ex) {
		ex.printStackTrace();
	}
	
	protected void preExecute() throws Exception {
		if (outputFile != null) {
			Path parent = outputFile.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
		}
		if (profileExecution) {
			writeOut(
				OperatingSystem.getCpuName(),
				"Logical processors: "+getNumberOfHardwareThreads(),
				"Xms: "+getAvailableMemory(MemoryUnit.MB),
				"Xmx: "+getMaxMemory(MemoryUnit.MB),
				"Starting execution at "+getTime(),
				printMarker
			);
		}
	}
	
	protected abstract Object execute() throws Exception;
	
	protected void postExecute() throws Exception {
		if (profileExecution) {
			writeOut("",
				"Profiled processes:",
				formatExecutionStages(profiledStages),
				"Finished at "+getTime(),
				printMarker
			);
		}
		if (showResults) {
			writeOut("Result: ", "", result, printMarker);
		}
	}
	
	public Duration getExecutionTime() {
		if (!hasRun) {
			throw new IllegalStateException("Not yet run!");
		}
		return getTotalExecutionTimeFrom(profiledStages);
	}
	
	public Object getResult() {
		if (!hasRun)
			throw new IllegalStateException("Attempted to get result without calling run()!");
		
		return result;
	}
	
	public int getId() {
		return id;
	}
	
	protected final void writeOut(Object... lines) {
		writeOut(Arrays.asList(lines));
	}
	
	protected void writeOut(Collection<?> lines) {
		if (outputFile != null) {
			try {
				Files.write(
					outputFile,
					lines.stream().map(Object::toString).collect(Collectors.toList()),
					StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.WRITE
				);
				return;
			}
			catch (IOException iox) {
				System.err.println("Couldn't write to file '"+outputFile+"': "+iox.getMessage());
			}
		}
		//Fall back to stdout if couldn't write to file
		lines.forEach(System.out::println);
	}
	
	@Override
	public String toString() {
		return
			getClass().getSimpleName()+
			": id="+id+
			", script='"+Objects.toString(script.getFileName())+"'";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, script, showResults, profileExecution, outputFile);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return false;
		if (!(obj instanceof ProfilableRunConfiguration))
			return false;
		
		ProfilableRunConfiguration other = (ProfilableRunConfiguration) obj;
		return
			Objects.equals(this.id, other.id) &&
			Objects.equals(this.script, other.script) &&
			Objects.equals(this.showResults, other.showResults) &&
			Objects.equals(this.profileExecution, other.profileExecution) &&
			Objects.equals(this.outputFile, other.outputFile) &&
			Objects.equals(this.result, other.result);
	}
}
