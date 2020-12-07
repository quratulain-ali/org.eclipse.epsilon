package org.eclipse.epsilon.eol.query;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.epsilon.common.dt.editor.ModelTypeExtensionFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dt.launching.EpsilonLaunchConfigurationDelegateListener;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;

public class EolQueryRewriter implements EpsilonLaunchConfigurationDelegateListener {

	@Override
	public void aboutToParse(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void aboutToExecute(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module) throws Exception {
		
		module.getCompilationContext().setModelFactory(new ModelTypeExtensionFactory());
		
		new EolStaticAnalyser().validate(module);
		
		if (module.getMain() == null) return;
        
		new QueryRewriter().invokeRewriters(module);
		
		System.err.println(new EolUnparser().unparse((EolModule)module));
		
	}

	@Override
	public void executed(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module, Object result) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
