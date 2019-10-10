/*********************************************************************
 * Copyright (c) 2018 The University of York.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.eol.execute.context.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.epsilon.common.concurrent.ConcurrentExecutionStatus;
import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.concurrent.EolNestedParallelismException;
import org.eclipse.epsilon.eol.execute.concurrent.executors.EolExecutorService;
import org.eclipse.epsilon.eol.execute.context.IEolContext;

/**
 * Thread-safe IEolContext, offering utilities for parallel execution.
 * 
 * @author Sina Madani
 * @since 1.6
 */
public interface IEolContextParallel extends IEolContext {
	
	/**
	 * The key used for configuring the parallelism in dt plugins.
	 */
	static final String NUM_THREADS_CONFIG = "parallelism";
	
	/**
	 * Indicates the scalability of this Context when more processing nodes are added.
	 * 
	 * @return the number of threads.
	 */
	int getParallelism();
	
	/**
	 * Attempts to set the parallelism of this context and its associated executor.
	 * Note that this may not take effect immediately and is intended to be called during parallel
	 * execution. Implementations may ignore this operation or throw an {@linkplain UnsupportedOperationException}.
	 * It is recommended that this method is only called during initialisation, and is present for convenience only.
	 * 
	 * @param parallelism The new value. Must be positive.
	 * @throws UnsupportedOperationException If this context (or its ExecutorService) has an immutable parallelism.
	 * @throws IllegalStateException If this method is called at an inconvenient time.
	 * @throws IllegalArgumentException If the new value is out of bounds.
	 */
	void setParallelism(int parallelism) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException;
	
	/**
	 * This method will return true if {@link #beginParallelTask()} has been called
	 * and false if {@link #endParallelTask()} has been called, or if {@link #beginParallelTask()}
	 * has not been called yet.
	 * 
	 * @return Whether this Context is currently executing in parallel mode.
	 */
	boolean isParallel();
	
	/**
	 * A re-usable ExecutorService.
	 * @return a cached {@link EolExecutorService}.
	 */
	EolExecutorService getExecutorService();

	/**
	 * Convenience method for testing whether to perform an operation in parallel using
	 * this context without encountering an {@link EolNestedParallelismException}.
	 * 
	 * @return <code>true</code> if calling {@link #enterParallelNest(ModuleElement)} is permitted.
	 */
	default boolean isParallelisationLegal() {
		return !isParallel();
	}
	
	/**
	 * This method should be called prior to performing any parallel execution.
	 * 
	 * @param entryPoint The module element to use as the cause of an exception
	 * @throws EolNestedParallelismException If {@link #isParallelisationLegal(Object)} returns false
	 */
	default void ensureNotNested(ModuleElement entryPoint) throws EolNestedParallelismException {
		if (!isParallelisationLegal()) throw new EolNestedParallelismException(entryPoint);
		ConcurrentExecutionStatus status = getExecutorService().getExecutionStatus();
		if (status != null && status.isInProgress())
			throw new EolNestedParallelismException(entryPoint);
	}
	
	/**
	 * Registers the beginning of parallel task on the default EolExecutorService.
	 * The {@link #endParallelTask()} method must be called once finished.
	 * 
	 * @param entryPoint The AST to associate with this task. May be null, in which
	 * case a default value (e.g. {@linkplain #getModule()}) should be used.
	 * @param shortCircuiting Whether the task may be terminated abruptly.
	 * @return {@link #getExecutorService()}
	 * @throws EolNestedParallelismException If there was already a parallel task in progress.
	 */
	default EolExecutorService beginParallelTask(ModuleElement entryPoint, boolean shortCircuiting) throws EolNestedParallelismException {
		ensureNotNested(entryPoint != null ? entryPoint : getModule());
		EolExecutorService executor = getExecutorService();
		assert executor != null && !executor.isShutdown();
		if (!executor.getExecutionStatus().register()) {
			throw new EolNestedParallelismException(entryPoint);
		}
		return executor;
	}
	
	/**
	 * Registers the beginning of parallel task on the default EolExecutorService.
	 * The {@link #endParallelTask()} method must be called once finished.
	 * 
	 * @param entryPoint The AST to associate with this task. May be null, in which
	 * case a default value (e.g. {@linkplain #getModule()}) should be used.
	 * @return {@link #getExecutorService()}
	 * @throws EolNestedParallelismException If there was already a parallel task in progress.
	 */
	default EolExecutorService beginParallelTask(ModuleElement entryPoint) throws EolNestedParallelismException {
		return beginParallelTask(entryPoint, false);
	}
	
	/**
	 * Must be called once parallel processing has finished.
	 * 
	 * @see #beginParallelTask(ModuleElement)
	 * @return The result of the task, if any.
	 * @throws EolRuntimeException if the status completed exceptionally.
	 * @throws IllegalStateException if the current job is still executing.
	 */
	default Object endParallelTask() throws EolRuntimeException {
		EolExecutorService executor = getExecutorService();
		if (executor != null) {
			ConcurrentExecutionStatus status = executor.getExecutionStatus();
			if (status != null) {
				if (status.isInProgress()) {
					throw new IllegalStateException("Attempted to end parallel task while execution is in progress!");
				}
				else if (status.waitForCompletion()) { // Note: this shouldn't actually wait!
					return status.getResult();
				}
				else {
					EolRuntimeException.propagateDetailed(status.getException());
				}
			}
		}
		return null;
	}
	
	/**
	 * Executes all of the tasks in parallel, blocking until they have completed.
	 * @param jobs The jobs to execute.
	 * @param entryPoint The identifier for this parallel task.
	 * @throws EolRuntimeException If any of the jobs fail (i.e. throw an exception).
	 */
	default void executeParallel(ModuleElement entryPoint, Collection<? extends Runnable> jobs) throws EolRuntimeException {
		EolExecutorService executor = beginParallelTask(entryPoint);
		executor.completeAll(jobs);
		endParallelTask();
	}
	
	/**
	 * Executes all of the tasks in parallel, blocking until they have completed.
	 * @param <T> The return type for each job.
	 * @param entryPoint The identifier for this parallel task.
	 * @param jobs The transformations to perform.
	 * @return The result of the jobs.
	 * @throws EolRuntimeException If any of the jobs fail (i.e. throw an exception).
	 */
	default <T> Collection<T> executeParallelTyped(ModuleElement entryPoint, Collection<Callable<T>> jobs) throws EolRuntimeException {
		EolExecutorService executor = beginParallelTask(entryPoint);
		Collection<T> results = executor.collectResults(executor.submitAllTyped(jobs));
		endParallelTask();
		return results;
	}
	
	/**
	 * Signals the completion of a short-circuitable task.
	 * @param entryPoint The identifier used to initiate the parallel task.
	 * @param result The result of the task, if any.
	 */
	default void completeShortCircuit(ModuleElement entryPoint, Object result) {
		getExecutorService().getExecutionStatus().completeWithResult(result);
	}
	
	/**
	 * Submits all jobs and waits until either all jobs have completed, or 
	 * {@link #completeShortCircuit(ModuleElement, Object)} is called.
	 * 
	 * @param entryPoint The identifier for this parallel task.
	 * @param jobs The jobs to execute.
	 * @return The result of this task, as set by {@linkplain #completeShortCircuit(ModuleElement, Object)}, if any.
	 * @throws EolRuntimeException If any of the jobs fail (i.e. throw an exception).
	 */
	default Object shortCircuit(ModuleElement entryPoint, Collection<? extends Runnable> jobs) throws EolRuntimeException {
		EolExecutorService executor = beginParallelTask(entryPoint);
		Object result = executor.shortCircuitCompletion(executor.submitAll(jobs));
		endParallelTask();
		return result;
	}
	
	/**
	 * Submits all jobs and waits until either all jobs have completed, or 
	 * {@link #completeShortCircuit(ModuleElement, Object)} is called.
	 * 
	 * @param <T> The return type of each job.
	 * @param entryPoint The identifier for this parallel task.
	 * @param jobs The jobs to execute.
	 * @return The result of this task, as set by {@linkplain #completeShortCircuit(ModuleElement, Object)}, if any.
	 * @throws EolRuntimeException If any of the jobs fail (i.e. throw an exception).
	 */
	@SuppressWarnings("unchecked")
	default <T> T shortCircuitTyped(ModuleElement entryPoint, Collection<Callable<T>> jobs) throws EolRuntimeException {
		EolExecutorService executor = beginParallelTask(entryPoint);
		T result = (T) executor.shortCircuitCompletion(executor.submitAllTyped(jobs));
		endParallelTask();
		return result;
	}

	/**
	 * Evaluates the job using this context's parallel execution facilities.
	 * Implementations may override this to support additional job types, calling
	 * the super method as the last resort for unknown cases. All implementations
	 * are expected to support Iterable / Collection types, as well as common
	 * concurrency units such as Runnable, Callable and Future.
	 * 
	 * @param job The job (or jobs) to evaluate.
	 * @param isInLoop Whether this method is being called recursively from a loop.
	 * 
	 * @throws IllegalArgumentException If the job type is not recognised.
	 * @throws EolRuntimeException If an exception is thrown whilst evaluating the job(s).
	 * 
	 * @return The result of evaluating the job.
	 */
	@SuppressWarnings("unchecked")
	default Object executeJob(Object job) throws EolRuntimeException {
		if (job == null) {
			return null;
		}
		else if (job instanceof Runnable) {
			((Runnable) job).run();
			return null;
		}
		else if (job instanceof Iterable) {
			final int colSize = job instanceof Collection ? ((Collection<?>) job).size() : -1;
			
			if (isParallelisationLegal()) {
				Collection<Callable<Object>> jobs = colSize >= 0 ? new ArrayList<>(colSize) : new LinkedList<>();
				for (Object next : (Iterable<?>) job) {
					jobs.add(next instanceof Callable ?
						(Callable<Object>) next :
						() -> executeJob(next)
					);
				}
				return executeParallelTyped(jobs);
			}
			else {
				Collection<Object> results = colSize >= 0 ? new ArrayList<>(colSize) : new LinkedList<>();
				for (Object next : (Iterable<?>) job) {
					results.add(executeJob(next));
				}
				return results;
			}
		}
		else if (job instanceof ModuleElement) {
			return getExecutorFactory().execute((ModuleElement) job, this);
		}
		else if (job instanceof Stream) {
			Stream<?> stream = (Stream<?>) job;
			boolean finite = stream.spliterator().hasCharacteristics(Spliterator.SIZED);
			return executeJob(finite ? stream.collect(Collectors.toList()) : stream.iterator());
		}
		else if (job instanceof BaseStream) {
			return executeJob(((BaseStream<?,?>)job).iterator());
		}
		else if (job instanceof Iterator) {
			Iterable<?> iter = () -> (Iterator<Object>) job;
			return executeJob(iter);
		}
		else if (job instanceof Spliterator) {
			return executeJob(StreamSupport.stream((Spliterator<?>) job, isParallelisationLegal()));
		}
		else if (job instanceof Supplier) {
			return ((Supplier<?>) job).get();
		}
		else try {
			if (job instanceof Future) {
				return ((Future<?>) job).get();
			}
			else if (job instanceof Callable) {
				return ((Callable<?>) job).call();
			}
		}
		catch (Exception ex) {
			EolRuntimeException.propagateDetailed(ex);
		}
			
		throw new IllegalArgumentException("Received unexpected object of type "+job.getClass().getName());
	}
	
	/**
	 * Convenience method for setting the parallelism on a context.
	 * @param properties The parameter passed to the configure method of the module.
	 * @param contextConstructor The function which creates a parallel context from a given number of threads.
	 * @param currentContext The existing context to return, if no changes are made.
	 * @return The new context if {@link #NUM_THREADS_CONFIG} is present in the properties, otherwise currentContext.
	 * @throws IllegalArgumentException If the value of {@link #NUM_THREADS_CONFIG} property is invalid.
	 */
	static <C extends IEolContextParallel> C configureContext(Map<String, ?> properties, Function<Integer, ? extends C> contextConstructor, C currentContext) throws IllegalArgumentException {
		if (properties.containsKey(NUM_THREADS_CONFIG)) {
			int parallelism = Integer.valueOf(Objects.toString((properties.get(NUM_THREADS_CONFIG))));
			if (parallelism < 1) throw new IllegalArgumentException("Parallelism must be at least 1!");
			return contextConstructor.apply(parallelism);
		}
		return currentContext;
	}
	
	// No entryPoint defaults
	
	default EolExecutorService beginParallelTask() throws EolNestedParallelismException {
		return beginParallelTask(null);
	}
	default void executeParallel(Collection<? extends Runnable> jobs) throws EolRuntimeException {
		executeParallel(null, jobs);
	}
	default <T> Collection<T> executeParallelTyped(Collection<Callable<T>> jobs) throws EolRuntimeException {
		return executeParallelTyped(null, jobs);
	}
	default void completeShortCircuit(Object result) {
		completeShortCircuit(null, result);
	}
	default <T> T shortCircuitTyped(Collection<Callable<T>> jobs) throws EolRuntimeException {
		return shortCircuitTyped(null, jobs);
	}
	default Object shortCircuit(Collection<? extends Runnable> jobs) throws EolRuntimeException {
		return shortCircuit(null, jobs);
	}
}
