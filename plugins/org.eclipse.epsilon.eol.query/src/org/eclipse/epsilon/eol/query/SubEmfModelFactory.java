package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.compile.context.IModelFactory;
import org.eclipse.epsilon.eol.models.IModel;

public class SubEmfModelFactory implements IModelFactory {

	@Override
	public IModel createModel(String driver) {
		// TODO Auto-generated method stub
		EmfModel model = new EmfModel();
		return  model;
	}

}
