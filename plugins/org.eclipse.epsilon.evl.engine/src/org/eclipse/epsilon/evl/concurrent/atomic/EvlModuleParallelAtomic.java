/*********************************************************************
 * Copyright (c) 2019 The University of York.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.evl.concurrent.atomic;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.erl.IErlModuleAtomicBatches;
import org.eclipse.epsilon.evl.concurrent.EvlModuleParallel;
import org.eclipse.epsilon.evl.execute.atoms.EvlAtom;
import org.eclipse.epsilon.evl.execute.context.concurrent.IEvlContextParallel;

/**
 * 
 * @author Sina Madani
 * @since 1.6
 */
public abstract class EvlModuleParallelAtomic<A extends EvlAtom<?>> extends EvlModuleParallel implements IErlModuleAtomicBatches<A> {

	public EvlModuleParallelAtomic() {
		this(null);
	}
	
	public EvlModuleParallelAtomic(IEvlContextParallel context) {
		super(context);
	}
	
	@Override
	protected void checkConstraints() throws EolRuntimeException {
		getContext().executeJob(getAllJobs());
	}
}
