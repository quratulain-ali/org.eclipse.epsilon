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
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.IfStatement;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OrOperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.types.EolModelElementType;

public class EmfModelRewriter extends EmfModel implements IRewriter{
	boolean cascaded = false;
	HashSet<String> optimisableOperations; 
	HashSet<String> allOperations;
	HashMap<String, List<String>> indexedElements;
	List<ModuleElement> decomposedAsts =new ArrayList<ModuleElement>();
	IEolModule module;
	boolean indexExists = false;

	@Override
	public void rewrite(IEolModule module, IEolCompilationContext context) {
		this.module = module;
		List<Statement> statements=module.getMain().getStatements();
		optimisableOperations = new HashSet<String>(Arrays.asList("select"));
		allOperations = new HashSet<String>(Arrays.asList("all", "allInstances"));
		indexedElements = new HashMap<>();
		
		optimiseStatementBlock(module, statements);
		
	}
	
	public void optimiseStatementBlock(IEolModule module, List<Statement> statements) {

		for (Statement statement : statements) {
			if (statement instanceof ForStatement) {
				List<Statement> childStatements = ((ForStatement) statement).getBodyStatementBlock().getStatements();
				optimiseStatementBlock(module, childStatements);
			}

			else if (statement instanceof IfStatement) {
				StatementBlock thenBlock = ((IfStatement) statement).getThenStatementBlock();
				if (thenBlock != null) {
					List<Statement> thenStatements = thenBlock.getStatements();
					optimiseStatementBlock(module, thenStatements);
				}
				StatementBlock elseBlock = ((IfStatement) statement).getElseStatementBlock();
				if (elseBlock != null) {
					List<Statement> elseStatements = ((IfStatement) statement).getElseStatementBlock().getStatements();
					optimiseStatementBlock(module, elseStatements);
				}
			}
			else {
				List<ModuleElement> asts = statement.getChildren();
				module = optimiseAST(asts, indexExists);
			}
		}
	}
	
	public IEolModule optimiseAST(List<ModuleElement> asts, boolean indexExists) {
		int index = 0;
		
		for(ModuleElement ast: asts) {
			
			if(ast instanceof OperationCallExpression){
				
				OperationCallExpression ocExp = (OperationCallExpression)ast;
				ModuleElement targetOcExp = ocExp.getTargetExpression();
				
				if(!(targetOcExp instanceof NameExpression)) {
					return optimiseAST(ast.getChildren(), indexExists);
				}
				
				Expression targetExpression =((ExpressionStatement)ast.getParent()).getExpression();
				
				if(ocExp.getName().equals("println"))
				{
					OperationCallExpression newOcExp = new OperationCallExpression(targetExpression, 
							new NameExpression("println"));
					((ExpressionStatement)ast.getParent()).setExpression(newOcExp);
				}
					
			}
			
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
									
//									module.addTranslatedQueries(targetExp.getName()+"."+operationExp.getName()
//									+"("+modelElementName.getValue()+","+indexField.getValue()+","+indexValue.getValue()+")");
//									
									
									OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp, operationExp,modelElementName,indexField,indexValue,new StringLiteral(String.valueOf(indexExists)));
									
									ast.getParent().getChildren().remove(index);
									ast.getParent().getChildren().add(index, rewritedQuery);
									
									return module;
								}
							}
							else
							{
							
							StringLiteral indexField = new StringLiteral(((NameExpression)operation.getExpressions().get(0).getChildren().get(0).getChildren().get(1)).getName());
							StringLiteral indexValue = new StringLiteral(((StringLiteral)operation.getExpressions().get(0).getChildren().get(1)).getValue());
							
							indexExists = false;
							
							if(indexedElements.get(modelElementName.getValue()).contains(indexField.getValue())){
								indexExists = true;
							}
							
							else
								indexedElements.get(modelElementName.getValue()).add(indexField.getValue());
							
//							module.addTranslatedQueries(targetExp.getName()+"."+operationExp.getName()
//							+"("+modelElementName.getValue()+","+indexField.getValue()+","+indexValue.getValue()+")");
							
							OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp, operationExp,modelElementName,indexField,indexValue,new StringLiteral(String.valueOf(indexExists)));
							indexedElements.get(modelElementName.getValue()).add(indexField.getValue());
							
							if(ast.getParent() instanceof ExpressionStatement)
						     ((ExpressionStatement)ast.getParent()).setExpression(rewritedQuery);
							
							else
								((OperationCallExpression)ast.getParent()).setTargetExpression(rewritedQuery);
							
							ast.getParent().getChildren().remove(index);
							ast.getParent().getChildren().add(index, rewritedQuery);
							
							return module;
							}
						}
					}
				}
				index++;
				
			}
			
		}
		return module;
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

	
