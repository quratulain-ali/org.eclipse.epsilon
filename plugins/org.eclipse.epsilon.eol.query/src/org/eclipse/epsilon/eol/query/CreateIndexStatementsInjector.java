package org.eclipse.epsilon.eol.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.evl.EvlModule;

public class CreateIndexStatementsInjector {
	
	public void inject(IModule module, String modelName,
			HashMap<String, HashSet<String>> potentialIndices) {
		int count = 0;
		Iterator<Entry<String, HashSet<String>>> it = potentialIndices.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, HashSet<String>> pair = (Map.Entry<String, HashSet<String>>) it.next();
			for (String field : pair.getValue()) {
				// Injecting createIndex statements based on potential indices
				ExpressionStatement statement = new ExpressionStatement();
				statement.setExpression(
						new OperationCallExpression(new NameExpression(modelName), new NameExpression("createIndex"),
								new StringLiteral(pair.getKey() + ""), new StringLiteral(field)));
				if(module instanceof EvlModule) 
					((EvlModule)module).getPre().get(0).getBody().getStatements().add(count, statement);
				else
					((EolModule)module).getMain().getStatements().add(count, statement);
				count++;
			}
		}
	}

}
