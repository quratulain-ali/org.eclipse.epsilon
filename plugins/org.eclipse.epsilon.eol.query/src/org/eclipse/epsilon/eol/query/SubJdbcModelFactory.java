package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.emc.mysql.MySqlModel;
import org.eclipse.epsilon.eol.compile.context.IModelFactory;
import org.eclipse.epsilon.eol.models.IModel;

public class SubJdbcModelFactory implements IModelFactory {

	@Override
	public IModel createModel(String driver) {
		// TODO Auto-generated method stub
		MySqlModel model = new MySqlModel();
		return  model;
	}

}
