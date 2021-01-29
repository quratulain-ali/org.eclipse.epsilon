package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.staticanalyser.CallGraphGenerator;

public class QueryRewriter {

	public void invokeRewriters(IEolModule module, CallGraphGenerator cg) {
		IEolCompilationContext context = module.getCompilationContext();
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.doOptimisation().equals("true")) {
				IModel model = modelDeclaration.getModel();

				if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL"))
					new MySqlModelQueryRewriter().rewrite(model, module, context);

				if (modelDeclaration.getDriverNameExpression().getName().equals("EMF"))
					new EmfModelQueryRewriter().rewrite(model, module, context,cg);
			}
		}

	}

}
