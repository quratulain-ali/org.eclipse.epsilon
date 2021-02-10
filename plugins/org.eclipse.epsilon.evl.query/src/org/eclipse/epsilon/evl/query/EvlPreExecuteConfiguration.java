package org.eclipse.epsilon.evl.query;

import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.epsilon.erl.ErlModule;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.parse.EvlUnparser;
import org.eclipse.epsilon.evl.staticanalyser.EvlStaticAnalyser;

public class EvlPreExecuteConfiguration extends EolRunConfiguration {
	IEolModule module;
	
	public EvlPreExecuteConfiguration(EolRunConfiguration other) {
		super(other);
		module = super.getModule();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void preExecute() throws Exception {
		super.preExecute();//	Resource resource = resourceSet.createResource(URI.createFileURI(new File(model).getAbsolutePath()));
		module.getCompilationContext().setModelFactory(new SubModelFactory());
		if(module instanceof EvlModule) 
			new EvlStaticAnalyser().validate(module);
		else
			new EolStaticAnalyser().validate(module);
		
//		if (module.getMain() == null) return;
        
		new EvlRewritingHandler().invokeRewriters(module);
		
		System.err.println(new EvlUnparser().unparse((ErlModule) module));
		
			
		
	}
}
