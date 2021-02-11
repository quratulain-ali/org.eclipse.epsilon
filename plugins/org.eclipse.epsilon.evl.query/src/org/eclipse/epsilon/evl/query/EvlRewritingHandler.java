package org.eclipse.epsilon.evl.query;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.models.IModel;

public class EvlRewritingHandler {

	public void invokeRewriters(IEolModule module) {
		IEolCompilationContext context = module.getCompilationContext();
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.doOptimisation().equals("true")) {
				IModel model = modelDeclaration.getModel();

				if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL"))
					new EvlMySqlRewriter().rewrite(model, module, context);

				if (modelDeclaration.getDriverNameExpression().getName().equals("EMF"))
					new EvlEmfRewriter().rewrite(model, module, context);
			}
		}

	}

}
