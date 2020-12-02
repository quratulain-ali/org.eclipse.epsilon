package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.models.IModel;

public class QueryRewriter {
	
	public void invokeRewriters(IEolModule module)
	{
		IEolCompilationContext context = module.getCompilationContext();
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			IModel model = modelDeclaration.getModel();

			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL"))
				new MySqlQueryRewriter().rewrite(model, module, context);

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF"))
				new EmfQueryRewriter().rewrite(model, module, context);
		}
		
	}

}
