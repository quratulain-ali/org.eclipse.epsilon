/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.evl.query;

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
public class EvlStandaloneExample {

	public static void main(String... args) throws Exception {
		Path root = Paths.get(EvlStandaloneExample.class.getResource("").toURI()),
			modelsRoot = root.getParent().resolve("query");
		
		StringProperties modelProperties = StringProperties.Builder()
			.withProperty(EmfModel.PROPERTY_NAME, "imdb")
			.withProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI,
				modelsRoot.resolve("movies.ecore").toAbsolutePath().toUri()
			)
			.withProperty(EmfModel.PROPERTY_MODEL_URI,
				modelsRoot.resolve("imdb-0.1.xmi").toAbsolutePath().toUri()
			)
			.withProperty(EmfModel.PROPERTY_CACHED, true)
			.withProperty(EmfModel.PROPERTY_CONCURRENT, true)
			.build();
		
		EvlRunConfiguration runConfig = EvlRunConfiguration.Builder()
			.withScript(root.resolve("testOrClausev1.evl"))
			.withModel(new EmfModel(), modelProperties)
			.withParameter("greeting", "Hello from ")
			.withProfiling()
			.withResults()
			.withParallelism()
			.build();
		
		EvlPreExecuteConfiguration sm = new EvlPreExecuteConfiguration(runConfig);
		sm.run();
		runConfig.postExecute();
	}
}
