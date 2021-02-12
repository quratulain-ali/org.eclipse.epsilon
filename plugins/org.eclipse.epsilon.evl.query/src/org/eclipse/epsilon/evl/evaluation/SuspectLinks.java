/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.evl.evaluation;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.evl.launch.EvlRunConfiguration;

/**
 * This example demonstrates using the 
 * Epsilon Validation Language, the model validation language
 * of Epsilon, in a stand-alone manner
 * 
 * @author Sina Madani
 * @author Dimitrios Kolovos
 */
public class SuspectLinks {

	public static void main(String... args) throws Exception {
		Path root = Paths.get(SuspectLinks.class.getResource("").toURI()),
			modelsRoot = root.getParent().resolve("evaluation");
		
		StringProperties model1 = StringProperties.Builder()
			.withProperty(EmfModel.PROPERTY_NAME, "A1")
			.withProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI,
				modelsRoot.resolve("requirements.ecore").toAbsolutePath().toUri()
			)
			.withProperty(EmfModel.PROPERTY_MODEL_URI,
				modelsRoot.resolve("requirements-gen.model").toAbsolutePath().toUri()
			)
			.withProperty(EmfModel.PROPERTY_CACHED, true)
			.withProperty(EmfModel.PROPERTY_CONCURRENT, true)
			.build();
		
		EvlRunConfiguration runConfig = EvlRunConfiguration.Builder()
			.withScript(root.resolve("suspectlinks.evl"))
			.withModel(new EmfModel(), model1)
			.withParameter("greeting", "Hello from ")
			.withProfiling()
			.withResults()
//			.withParallelism()
			.build();
		
		EvlPreExecuteConfiguration sm = new EvlPreExecuteConfiguration(runConfig);
		sm.run();
		runConfig.postExecute();
	}
}
