package org.eclipse.epsilon.evl.query;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dt.launching.EpsilonLaunchConfigurationDelegateListener;
import org.eclipse.epsilon.erl.ErlModule;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.parse.EvlUnparser;
import org.eclipse.epsilon.evl.staticanalyser.EvlStaticAnalyser;

public class EvlLaunchConfiguration implements EpsilonLaunchConfigurationDelegateListener {

	public EvlLaunchConfiguration() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void aboutToParse(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void aboutToExecute(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module) throws Exception {
		if(module instanceof EvlModule) {
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				module.getCompilationContext().setModelFactory(new SubJdbcModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
		}
		module.getContext().setModule(module);
			new EvlStaticAnalyser().validate(module);
        
		new EvlRewritingHandler().invokeRewriters((EvlModule)module);
		
		System.err.println(new EvlUnparser().unparse((ErlModule) module));
			
		}
	}

	@Override
	public void executed(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module, Object result) throws Exception {
		// TODO Auto-generated method stub

	}

}
