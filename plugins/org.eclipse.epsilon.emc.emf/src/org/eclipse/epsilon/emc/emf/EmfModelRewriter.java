package org.eclipse.epsilon.emc.emf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OrOperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.types.EolModelElementType;

public class EmfModelRewriter extends EmfModel implements IRewriter{
	boolean cascaded = false;

	@Override
	public void rewrite(IEolModule module, IEolCompilationContext context) {
		List<Statement> statements=module.getMain().getStatements();
		HashSet<String> optimisableOperations = new HashSet<String>(Arrays.asList("select"));
		HashSet<String> allOperations = new HashSet<String>(Arrays.asList("all", "allInstances"));
		HashMap<String, List<String>> indexedElements = new HashMap<>();
		
		for(Statement statement: statements) {
			
			List<ModuleElement> asts = statement.getChildren();
			List<ModuleElement> decomposedAsts =new ArrayList<ModuleElement>();
			int index = 0;
			boolean indexExists = false;
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
								NameExpression operationExp = new NameExpression("findByIndex");
								StringLiteral modelElementName = new StringLiteral(model.getTypeName());
								
								if(indexedElements.get(modelElementName.getValue())==null)
								indexedElements.put(modelElementName.getValue(),new ArrayList<String>());
								
								Expression parameterAst = operation.getExpressions().get(0);
								
								if(parameterAst instanceof OrOperatorExpression)
								{
									decomposedAsts = decomposeAST(parameterAst);
									if(cascaded)
									decomposedAsts.add(((OrOperatorExpression) parameterAst).getSecondOperand());
									
									for(ModuleElement firstOperand : decomposedAsts) {
										
										StringLiteral indexField = new StringLiteral(((NameExpression)firstOperand.getChildren().get(0).getChildren().get(1)).getName());
										StringLiteral indexValue = new StringLiteral(((StringLiteral)firstOperand.getChildren().get(1)).getValue());
										
										indexExists = false;
										if(indexedElements.get(modelElementName.getValue()).contains(indexField.getValue())){
											indexExists = true;
										}
										else
											indexedElements.get(modelElementName.getValue()).add(indexField.getValue());
										
										module.addTranslatedQueries(targetExp.getName()+"."+operationExp.getName()
										+"("+modelElementName.getValue()+","+indexField.getValue()+","+indexValue.getValue()+")");
										
										
										OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp, operationExp,modelElementName,indexField,indexValue,new StringLiteral(String.valueOf(indexExists)));
										
										ast.getParent().getChildren().remove(index);
										ast.getParent().getChildren().add(index, rewritedQuery);
									}
								}
								else
								{
								
								StringLiteral indexField = new StringLiteral(((NameExpression)operation.getExpressions().get(0).getChildren().get(0).getChildren().get(1)).getName());
								StringLiteral indexValue = new StringLiteral(((StringLiteral)operation.getExpressions().get(0).getChildren().get(1)).getValue());
								module.addTranslatedQueries(targetExp.getName()+"."+operationExp.getName()
								+"("+modelElementName.getValue()+","+indexField.getValue()+","+indexValue.getValue()+")");
								OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp, operationExp,modelElementName,indexField,indexValue);
								indexedElements.get(modelElementName.getValue()).add(indexField.getValue());
								ast.getParent().getChildren().remove(index);
								ast.getParent().getChildren().add(index, rewritedQuery);
								}
							}
						}
					}
					index++;
						//Expression check = ((FirstOrderOperationCallExpression)ast).getTargetExpression();
					
					
				}
			}
		}
		
	}
	public List<ModuleElement> decomposeAST(Expression ast) {
		Expression firstOperand = ((OrOperatorExpression) ast).getFirstOperand();
		if( firstOperand instanceof OrOperatorExpression) {
			cascaded = true;
			return decomposeAST(firstOperand);
		}
		return ast.getChildren();
		
	}

}

	
