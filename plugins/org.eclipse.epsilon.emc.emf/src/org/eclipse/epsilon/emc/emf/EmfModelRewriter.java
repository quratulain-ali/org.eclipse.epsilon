package org.eclipse.epsilon.emc.emf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.types.EolModelElementType;

public class EmfModelRewriter extends EmfModel implements IRewriter{

	@Override
	public void rewrite(IEolModule module, IEolCompilationContext context) {
		List<Statement> statements=module.getMain().getStatements();
		HashSet<String> optimisableOperations = new HashSet<String>(Arrays.asList("select"));
		HashSet<String> allOperations = new HashSet<String>(Arrays.asList("all", "allInstances"));
		
		for(Statement statement: statements) {
			List<ModuleElement> asts = statement.getChildren();
			int index = 0;
			for(ModuleElement ast: asts) {
				if(ast instanceof FirstOrderOperationCallExpression)
				{
					ModuleElement target = ast.getChildren().get(0);
					if(target instanceof PropertyCallExpression) {
						String operationName = ((NameExpression)target.getChildren().get(1)).getName();
						if(allOperations.contains(operationName))
						{
							FirstOrderOperationCallExpression operation=((FirstOrderOperationCallExpression) ast);
							String firstoperationName=operation.getNameExpression().getName();
							if(optimisableOperations.contains(firstoperationName))
							{
								EolModelElementType model = ((EolModelElementType)((PropertyCallExpression)target).getTargetExpression().getResolvedType());
								String modelName = model.getModelName();
								
								NameExpression targetExp = new NameExpression(modelName);
								NameExpression operationExp = new NameExpression("getIndexedAllOfKindFromModel");
								StringLiteral p1 = new StringLiteral(model.getTypeName());
								StringLiteral p2 = new StringLiteral(((NameExpression)operation.getExpressions().get(0).getChildren().get(0).getChildren().get(1)).getName());
								StringLiteral p3 = new StringLiteral(((StringLiteral)operation.getExpressions().get(0).getChildren().get(1)).getValue());
								module.setText(p1.getValue());
								OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp, operationExp,p1,p2,p3);
								
								ast.getParent().getChildren().remove(index);
								ast.getParent().getChildren().add(index, rewritedQuery);
							}
						}
					}
					index++;
						//Expression check = ((FirstOrderOperationCallExpression)ast).getTargetExpression();
					
					
				}
			}
		}
		
	}

}
