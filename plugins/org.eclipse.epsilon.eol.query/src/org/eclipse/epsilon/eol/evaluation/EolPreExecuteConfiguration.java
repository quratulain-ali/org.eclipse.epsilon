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
		super.preExecute();//	Resource resource = resourceSet.createResource(URI.createFileURI(new File(model).getAbsolutePath()));
//		String metamodel = "src/org/eclipse/epsilon/evl/query/psmToJavaTypeMapping.ecore";
//		ResourceSet resourceSet = new ResourceSetImpl();
//		ResourceSet ecoreResourceSet = new ResourceSetImpl();
//		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//		Resource ecoreResource = ecoreResourceSet.
//				createResource(URI.createFileURI(new File(metamodel).getAbsolutePath()));
//		try {
//			ecoreResource.load(null);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (EObject o : ecoreResource.getContents()) {
//			EPackage ePackage = (EPackage) o;
//			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
//			EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
//		}
//		if(module == null) return;
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				module.getCompilationContext().setModelFactory(new SubJdbcModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
		}
			EolStaticAnalyser staticAnlayser = new EolStaticAnalyser();
			staticAnlayser.validate(module);
        
		new EolRewritingHandler().invokeRewriters(module, staticAnlayser.getCallGraph());
		
		System.err.println(new EolUnparser().unparse( (EolModule) module));
		
			
		
	}
}
