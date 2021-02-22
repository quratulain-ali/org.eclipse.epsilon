package org.eclipse.epsilon.eol.evaluation;

import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.query.EolRewritingHandler;
import org.eclipse.epsilon.eol.query.SubJdbcModelFactory;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;

public class EolPreExecuteConfiguration extends EolRunConfiguration {
	IEolModule module;
	
	public EolPreExecuteConfiguration(EolRunConfiguration other) {
		super(other);
		module = super.getModule();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void preExecute() throws Exception {
		super.preExecute();
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				module.getCompilationContext().setModelFactory(new SubJdbcModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
		}
		
		module.getContext().setModule(module);
			EolStaticAnalyser staticAnlayser = new EolStaticAnalyser();
			staticAnlayser.validate(module);
        
		new EolRewritingHandler().invokeRewriters(module, staticAnlayser.getCallGraph());
		
		System.err.println(new EolUnparser().unparse( (EolModule) module));
		
			
		
	}
}
