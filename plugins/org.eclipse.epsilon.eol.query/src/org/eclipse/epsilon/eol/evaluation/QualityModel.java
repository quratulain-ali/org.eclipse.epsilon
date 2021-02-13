/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.eol.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.eol.launch.EolRunConfiguration;
import org.eclipse.epsilon.emc.emf.EmfModel;

/**
 * This example demonstrates using the Epsilon Object Language, the core language of Epsilon, in a stand-alone manner 
 * 
 * @author Sina Madani
 * @author Dimitrios Kolovos
 */
public class QualityModel {
	
	public static void main(String[] args) throws Exception {
		Path root = Paths.get(QualityModel.class.getResource("").toURI()),
			modelsRoot = root.getParent().resolve("evaluation");
		
		StringProperties modelProperties = new StringProperties();
		modelProperties.setProperty(EmfModel.PROPERTY_NAME, "qualityModel");
		modelProperties.setProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI,
			modelsRoot.resolve("QualityMM.ecore").toAbsolutePath().toUri().toString()
		);
		modelProperties.setProperty(EmfModel.PROPERTY_MODEL_URI,
			modelsRoot.resolve("qualitygen.model").toAbsolutePath().toUri().toString()
		);
		
		StringProperties modelProperties1 = new StringProperties();
		modelProperties1.setProperty(EmfModel.PROPERTY_NAME, "MM");
		modelProperties1.setProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI,
			modelsRoot.resolve("Ecore.ecore").toAbsolutePath().toUri().toString()
		);
		modelProperties1.setProperty(EmfModel.PROPERTY_MODEL_URI,
			modelsRoot.resolve("JDTAST.ecore").toAbsolutePath().toUri().toString()
		);
//		
		EolRunConfiguration runConfig = EolRunConfiguration.Builder()
			.withScript(root.resolve("qualityModel.eol"))
			.withModel(new EmfModel(), modelProperties)
			.withModel(new EmfModel(), modelProperties1)
			.withParameter("Thread", Thread.class)
			.withProfiling()
			.build();
		
		EolPreExecuteConfiguration sm = new EolPreExecuteConfiguration(runConfig);
		sm.run();
		System.out.println(sm.getResult());
	}
	
}
