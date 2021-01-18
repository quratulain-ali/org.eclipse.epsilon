package org.eclipse.epsilon.eol.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.AndOperatorExpression;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.EqualsOperatorExpression;
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
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.types.EolModelElementType;

public class EmfModelQueryRewriter {

	boolean cascaded = false;
	HashSet<String> optimisableOperations; // List of built-in operations that can be optimised
	HashSet<String> allOperations;
	HashMap<String, List<String>> indexedElements;
	List<ModuleElement> decomposedAsts = new ArrayList<ModuleElement>();
	IEolModule module;
	String modelName;
	boolean indexExists = false;

	public void rewrite(IModel model, IEolModule module, IEolCompilationContext context) {
		this.module = module;
		List<Statement> statements = module.getMain().getStatements();
		
		optimisableOperations = new HashSet<String>(Arrays.asList("select"));
		allOperations = new HashSet<String>(Arrays.asList("all", "allInstances"));
		indexedElements = new HashMap<>();

		optimiseStatementBlock(model, module, statements);
		
		if (module.getMain() == null)   return;
		int index = 0;
		
		Iterator<Entry<String, List<String>>> it = indexedElements.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
			
			for (String field : pair.getValue()) {
				//Injecting createIndex statements
				
				ExpressionStatement statement = new ExpressionStatement();
				statement.setExpression(new OperationCallExpression(new NameExpression(model.getName()),
						new NameExpression("createIndex"), new StringLiteral(pair.getKey() + ""),
						new StringLiteral(field)));
				
				module.getMain().getStatements().add(index, statement);
				
				index++;
			}
			it.remove(); 
		}

	}

	public void optimiseStatementBlock(IModel model, IEolModule module, List<Statement> statements) {

		for (Statement statement : statements) {
			if (statement instanceof ForStatement) {
				//optimising iterator expression
				optimiseAST(model, Arrays.asList(statement.getChildren().get(1)), indexExists);
				List<Statement> childStatements = ((ForStatement) statement).getBodyStatementBlock().getStatements();
				optimiseStatementBlock(model, module, childStatements);
			}

			else if (statement instanceof IfStatement) {
				StatementBlock thenBlock = ((IfStatement) statement).getThenStatementBlock();
				if (thenBlock != null) {
					List<Statement> thenStatements = thenBlock.getStatements();
					optimiseStatementBlock(model, module, thenStatements);
				}
				StatementBlock elseBlock = ((IfStatement) statement).getElseStatementBlock();
				if (elseBlock != null) {
					List<Statement> elseStatements = ((IfStatement) statement).getElseStatementBlock().getStatements();
					optimiseStatementBlock(model, module, elseStatements);
				}
			} else {
				List<ModuleElement> asts = statement.getChildren();
				module = optimiseAST(model, asts, indexExists);
			}
		}
	}

	public IEolModule optimiseAST(IModel model, List<ModuleElement> asts, boolean indexExists) {
		int index = 0;

		for (ModuleElement ast : asts) {

			if (ast instanceof OperationCallExpression) {

				OperationCallExpression ocExp = (OperationCallExpression) ast;
				ModuleElement targetOcExp = ocExp.getTargetExpression();

				if (!(targetOcExp instanceof NameExpression)) {
					return optimiseAST(model, ast.getChildren(), indexExists);
				}
				
				if(ast.getParent() instanceof ExpressionStatement) {

				Expression targetExpression = ((ExpressionStatement) ast.getParent()).getExpression();

				if (ocExp.getName().equals("println")) {
					OperationCallExpression newOcExp = new OperationCallExpression(targetExpression,
							new NameExpression("println"));
					((ExpressionStatement) ast.getParent()).setExpression(newOcExp);
				}
				}

			}

			if (ast instanceof FirstOrderOperationCallExpression) {
				ModuleElement target = ast.getChildren().get(0);

				if (target instanceof PropertyCallExpression) {

					String operationName = ((NameExpression) target.getChildren().get(1)).getName();

					if (allOperations.contains(operationName)) {

						FirstOrderOperationCallExpression operation = ((FirstOrderOperationCallExpression) ast);
						String firstoperationName = operation.getNameExpression().getName();

						if (optimisableOperations.contains(firstoperationName)) {
							EolModelElementType m = ((EolModelElementType) ((PropertyCallExpression) target)
									.getTargetExpression().getResolvedType());
							modelName = m.getModelName();
							try {
								if (m.getModel(module.getCompilationContext()) == model) {
									model.setName(modelName);
									NameExpression targetExp = new NameExpression(modelName);
									NameExpression operationExp = new NameExpression("findByIndex");
									StringLiteral modelElementName = new StringLiteral(m.getTypeName());

									if (indexedElements.get(modelElementName.getValue()) == null)
										indexedElements.put(modelElementName.getValue(), new ArrayList<String>());

									Expression parameterAst = operation.getExpressions().get(0);

									if (parameterAst instanceof OrOperatorExpression) {
										decomposedAsts = decomposeAST(parameterAst);

										if (cascaded)
											decomposedAsts
													.add(((OrOperatorExpression) parameterAst).getSecondOperand());
										OperationCallExpression rewritedQuery = new OperationCallExpression();
										for (ModuleElement firstOperand : decomposedAsts) {
											if(firstOperand instanceof EqualsOperatorExpression) {
											StringLiteral indexField = new StringLiteral(((NameExpression) firstOperand
													.getChildren().get(0).getChildren().get(1)).getName());
											StringLiteral indexValue = new StringLiteral(
													((StringLiteral) firstOperand.getChildren().get(1)).getValue());

											indexExists = false;

											if (indexedElements.get(modelElementName.getValue())
													.contains(indexField.getValue())) {
												indexExists = true;
											}

											else
												indexedElements.get(modelElementName.getValue())
														.add(indexField.getValue());
											if(rewritedQuery.getName() == null)
											rewritedQuery = new OperationCallExpression(
													targetExp, operationExp, modelElementName, indexField, indexValue);
											else
//												rewritedQuery = new OperationCallExpression(
//														rewritedQuery, new NameExpression("union"), new OperationCallExpression(
//																targetExp, operationExp, modelElementName, indexField, indexValue));
											
											rewritedQuery = new OperationCallExpression(
													null, new NameExpression("join"), rewritedQuery,new OperationCallExpression(
															targetExp, operationExp, modelElementName, indexField, indexValue));
											
											}
											if (ast.getParent() instanceof ExpressionStatement)
												((ExpressionStatement) ast.getParent()).setExpression(rewritedQuery);
											else if (ast.getParent() instanceof AssignmentStatement)
												((AssignmentStatement) ast.getParent()).setValueExpression(rewritedQuery);
											else if (ast.getParent() instanceof ForStatement)
												((ForStatement) ast.getParent()).setIteratedExpression(rewritedQuery);
											else
												((OperationCallExpression) ast.getParent())
														.setTargetExpression(rewritedQuery);
											//return module;
										}
									} else {
										if(operation.getExpressions().get(0) instanceof EqualsOperatorExpression) {
										StringLiteral indexField = new StringLiteral(
												((NameExpression) operation.getExpressions().get(0).getChildren().get(0)
														.getChildren().get(1)).getName());
										StringLiteral indexValue = new StringLiteral(
												((StringLiteral) operation.getExpressions().get(0).getChildren().get(1))
														.getValue());

										indexExists = false;

										if (indexedElements.get(modelElementName.getValue())
												.contains(indexField.getValue())) {
											indexExists = true;
										}

										else
											indexedElements.get(modelElementName.getValue()).add(indexField.getValue());

										OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp,
												operationExp, modelElementName, indexField, indexValue);

										if (ast.getParent() instanceof ExpressionStatement)
											((ExpressionStatement) ast.getParent()).setExpression(rewritedQuery);
										else if (ast.getParent() instanceof AssignmentStatement)
											((AssignmentStatement) ast.getParent()).setValueExpression(rewritedQuery);
										else if (ast.getParent() instanceof ForStatement)
											((ForStatement) ast.getParent()).setIteratedExpression(rewritedQuery);
										else
											((OperationCallExpression) ast.getParent())
													.setTargetExpression(rewritedQuery);
										
										}


										return module;
									}
								}
							} catch (EolModelElementTypeNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
		if (firstOperand instanceof OrOperatorExpression) {
			cascaded = true;
			return decomposeAST(firstOperand);
		}
		if (firstOperand instanceof AndOperatorExpression) {
			cascaded = true;
			return decomposeAST(firstOperand);
		}
		return ast.getChildren();

	}

}
