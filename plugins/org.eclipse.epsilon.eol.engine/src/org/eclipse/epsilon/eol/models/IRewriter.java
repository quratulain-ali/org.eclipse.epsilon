package org.eclipse.epsilon.eol.models;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;

public interface IRewriter {

	void rewrite(IEolModule module, IEolCompilationContext context);
	
}
