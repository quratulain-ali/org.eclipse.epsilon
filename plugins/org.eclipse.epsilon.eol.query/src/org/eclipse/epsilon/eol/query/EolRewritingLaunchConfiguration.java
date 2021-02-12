package org.eclipse.epsilon.eol.query;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.epsilon.common.dt.editor.ModelTypeExtensionFactory;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dt.launching.EpsilonLaunchConfigurationDelegateListener;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;

public class EolRewritingLaunchConfiguration implements EpsilonLaunchConfigurationDelegateListener {

	@Override
	public void aboutToParse(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void aboutToExecute(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module) throws Exception {
		
//		module.getCompilationContext().setModelFactory(new ModelTypeExtensionFactory());
//		
//		EolStaticAnalyser staticAnlayser = new EolStaticAnalyser();
//		staticAnlayser.validate(module);
//		
//		if (module.getMain() == null) return;
//        
//		new EolRewritingHandler().invokeRewriters(module, staticAnlayser.getCallGraph());
		
	}

	@Override
	public void executed(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor progressMonitor, IEolModule module, Object result) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
