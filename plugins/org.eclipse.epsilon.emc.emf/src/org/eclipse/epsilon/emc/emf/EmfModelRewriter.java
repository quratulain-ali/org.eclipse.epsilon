package org.eclipse.epsilon.emc.emf;

import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.models.IRewriter;

public class EmfModelRewriter extends EmfModel implements IRewriter{

	@Override
	public void rewrite(IEolModule module, IEolCompilationContext context) {
		
		List<Statement> statements=module.getMain().getStatements();
		
		for(Statement statement: statements) {
			List<ModuleElement> asts = statement.getChildren();
		}
		
	}

}
