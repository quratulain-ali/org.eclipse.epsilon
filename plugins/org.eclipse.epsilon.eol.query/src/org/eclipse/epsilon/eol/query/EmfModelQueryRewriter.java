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
import org.eclipse.epsilon.eol.dom.BooleanLiteral;
import org.eclipse.epsilon.eol.dom.EqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.IfStatement;
import org.eclipse.epsilon.eol.dom.IntegerLiteral;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperatorExpression;
import org.eclipse.epsilon.eol.dom.OrOperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.ReturnStatement;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.staticanalyser.CallGraphGenerator;
import org.eclipse.epsilon.eol.types.EolModelElementType;

public class EmfModelQueryRewriter {
	
	HashSet<String> optimisableOperations; // List of built-in operations that can be optimised
	HashSet<String> allOperations; //all, allInstances
	HashMap<String, List<String>> possibleIndices; //fields that can be indexed
	List<String> potentialIndices = new ArrayList<String>(); //indexed fields which can be possibly used multiple times
	Map<String,ArrayList<Object>> multiMap = new HashMap<>();
	//Cascaded and or clauses in select statement
	List<ModuleElement> decomposedAsts = new ArrayList<ModuleElement>();
	boolean cascaded = false; 
	
	IEolModule module;
	String modelName;
	
	boolean indexExists = false;
	boolean canbeExecutedMultipleTimes = false;

	public void rewrite(IModel model, IEolModule module, IEolCompilationContext context, CallGraphGenerator cg) {//, DefaultDirectedGraph<String, DefaultEdge> callGraph) {
		
		this.module = module;
		
		if (module.getMain() == null)   return;
		
		List<Statement> statements = module.getMain().getStatements();
		
		optimisableOperations = new HashSet<String>(Arrays.asList("select"));
		allOperations = new HashSet<String>(Arrays.asList("all", "allInstances"));
		possibleIndices = new HashMap<>();

		optimiseStatementBlock(model, module, statements);
		
		for(Operation operation : module.getDeclaredOperations()) {
			String name = operation.getName();
			if(cg.pathContainsLoop("main", name)) {
				canbeExecutedMultipleTimes = true;
			}
			optimiseStatementBlock(model, module, operation.getBody().getStatements());
			canbeExecutedMultipleTimes = false;
		}
		
		module = rewriteIfIndexAlreadyExists(module, multiMap, potentialIndices);
		
		int index = 0;
		Iterator<Entry<String, List<String>>> it = possibleIndices.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> pair = (Map.Entry<String, List<String>>) it.next();
			for (String field : pair.getValue()) {
				//Injecting createIndex statements based on potential indices
				if(potentialIndices.contains(field)) {
				ExpressionStatement statement = new ExpressionStatement();
				statement.setExpression(new OperationCallExpression(new NameExpression(model.getName()),
						new NameExpression("createIndex"), new StringLiteral(pair.getKey() + ""),
						new StringLiteral(field)));
				
				module.getMain().getStatements().add(index, statement);
				index++;
				}
			}
		}
	}

	public void optimiseStatementBlock(IModel model, IEolModule module, List<Statement> statements) {

		for (Statement statement : statements) {
			if (statement instanceof ForStatement) {
//				//optimising iterator expression
//				optimiseAST(model, Arrays.asList(statement.getChildren().get(1)), indexExists);
				canbeExecutedMultipleTimes = true;
				List<Statement> childStatements = ((ForStatement) statement).getBodyStatementBlock().getStatements();
				optimiseStatementBlock(model, module, childStatements);
				canbeExecutedMultipleTimes = false;
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
			} 
			else {
				List<ModuleElement> asts = statement.getChildren();
				module = optimiseAST(model, asts, indexExists);
			}
		}
	}

	public IEolModule optimiseAST(IModel model, List<ModuleElement> asts, boolean indexExists) {
		@SuppressWarnings("unused")
		int index = 0;

		for (ModuleElement ast : asts) {

			if (ast instanceof OperationCallExpression) {

				OperationCallExpression ocExp = (OperationCallExpression) ast;
				ModuleElement targetOcExp = ocExp.getTargetExpression();

				if (!(targetOcExp instanceof NameExpression)) {
					return optimiseAST(model, ast.getChildren(), indexExists);
				}

				if (ast.getParent() instanceof ExpressionStatement) {
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

				if (target instanceof PropertyCallExpression || target instanceof OperationCallExpression) {

					String operationName = ((NameExpression) target.getChildren().get(1)).getName();

					if (allOperations.contains(operationName)) {

						FirstOrderOperationCallExpression operation = ((FirstOrderOperationCallExpression) ast);
						String firstoperationName = operation.getNameExpression().getName();

						if (optimisableOperations.contains(firstoperationName)) {
							EolModelElementType m;
							if(target instanceof PropertyCallExpression)
							m= ((EolModelElementType) ((PropertyCallExpression) target)
									.getTargetExpression().getResolvedType());
							else
								m= ((EolModelElementType) ((OperationCallExpression) target)
										.getTargetExpression().getResolvedType());
							modelName = m.getModelName();
							try {
								if (m.getModel(module.getCompilationContext()) == model) {
									model.setName(modelName);
									NameExpression targetExp = new NameExpression(modelName);
									NameExpression operationExp = new NameExpression("findByIndex");
									StringLiteral modelElementName = new StringLiteral(m.getTypeName());

									if (possibleIndices.get(modelElementName.getValue()) == null)
										possibleIndices.put(modelElementName.getValue(), new ArrayList<String>());

									Expression parameterAst = operation.getExpressions().get(0);
									StringLiteral indexField = new StringLiteral();
									if (parameterAst instanceof OrOperatorExpression) {
										cascaded = false;
										decomposedAsts = decomposeAST(parameterAst);

										if (cascaded)
											decomposedAsts
													.add(((OrOperatorExpression) parameterAst).getSecondOperand());
										OperationCallExpression rewritedQuery = new OperationCallExpression();
										for (ModuleElement firstOperand : decomposedAsts) {
											if(firstOperand instanceof EqualsOperatorExpression) {
											indexField = new StringLiteral(((NameExpression) firstOperand
													.getChildren().get(0).getChildren().get(1)).getName());
											StringLiteral indexValue = new StringLiteral(
													((StringLiteral) firstOperand.getChildren().get(1)).getValue());

											indexExists = false;

											if (possibleIndices.get(modelElementName.getValue())
													.contains(indexField.getValue())) {
												indexExists = true;
											}

											else
												possibleIndices.get(modelElementName.getValue())
														.add(indexField.getValue());
											if(rewritedQuery.getName() == null)
											rewritedQuery = new OperationCallExpression(
													targetExp, operationExp, modelElementName, indexField, indexValue);
											else {
												rewritedQuery = new OperationCallExpression(
														rewritedQuery, new NameExpression("includingAll"),new OperationCallExpression(
																targetExp, operationExp, modelElementName, indexField, indexValue));
											}
											
											}
											if(indexExists || canbeExecutedMultipleTimes) {
												 potentialIndices.add(indexField.getValue());
												 rewriteToModule(ast, rewritedQuery);
											}
											else {
												ArrayList<Object> possibleAst = new ArrayList<>();
												possibleAst.add(rewritedQuery);
												possibleAst.add(ast);
												multiMap.put(indexField.getValue(),possibleAst); 
											}
											
										}
									}
									else if (parameterAst instanceof AndOperatorExpression) {
										cascaded = false;
										decomposedAsts = decomposeAST(parameterAst);

										if (cascaded)
											decomposedAsts
													.add(((AndOperatorExpression) parameterAst).getSecondOperand());
										
										OperationCallExpression rewritedQuery = new OperationCallExpression();
										
										indexField = new StringLiteral();
										StringLiteral indexValue = new StringLiteral();
										
										for (ModuleElement firstOperand : decomposedAsts) {
											if(firstOperand instanceof EqualsOperatorExpression) {
												if(indexField.getValue() != null) {
											 indexField = new StringLiteral(indexField.getValue()+"-"+
													 ((NameExpression) firstOperand
													.getChildren().get(0).getChildren().get(1)).getName());
											indexValue = new StringLiteral(indexValue.getValue()+"-"+
													((StringLiteral) firstOperand.getChildren().get(1)).getValue()); 
												}
												else {
													indexField = new StringLiteral(((NameExpression) firstOperand
															.getChildren().get(0).getChildren().get(1)).getName());
													indexValue = new StringLiteral(((StringLiteral) firstOperand.getChildren().get(1)).getValue());
												}
											
										}
										}
											indexExists = false;

											if (possibleIndices.get(modelElementName.getValue())
													.contains(indexField.getValue())) {
												indexExists = true;
											}
											

											else
												possibleIndices.get(modelElementName.getValue())
														.add(indexField.getValue());
										rewritedQuery = new OperationCallExpression(
														targetExp, operationExp,modelElementName, indexField, indexValue);
										if(indexExists && canbeExecutedMultipleTimes) {
											 potentialIndices.add(indexField.getValue());
											 rewriteToModule(ast, rewritedQuery);
										}
										else {
											ArrayList<Object> possibleAst = new ArrayList<>();
											possibleAst.add(rewritedQuery);
											possibleAst.add(ast);
											multiMap.put(indexField.getValue(),possibleAst); 
										}
										
									}
									else {
										if(operation.getExpressions().get(0) instanceof EqualsOperatorExpression) {
										indexField = new StringLiteral(
												((NameExpression) operation.getExpressions().get(0).getChildren().get(0)
														.getChildren().get(1)).getName());
										ModuleElement indexValueExpression = operation.getExpressions().get(0).getChildren().get(1);
										StringLiteral indexValue = new StringLiteral();
										if(indexValueExpression instanceof BooleanLiteral) {
											indexValue = new StringLiteral(((BooleanLiteral) indexValueExpression).getValue().toString());
										}
										else if(indexValueExpression instanceof StringLiteral) {
											indexValue = new StringLiteral(((StringLiteral)indexValueExpression).getValue());
										}
										else if(indexValueExpression instanceof IntegerLiteral) {
											indexValue = new StringLiteral(((IntegerLiteral)indexValueExpression).getValue().toString());
										}
										indexExists = false;

										if (possibleIndices.get(modelElementName.getValue())
												.contains(indexField.getValue())) {
											indexExists = true;
										}

										else
											possibleIndices.get(modelElementName.getValue()).add(indexField.getValue());

										OperationCallExpression rewritedQuery = new OperationCallExpression(targetExp,
												operationExp, modelElementName, indexField, indexValue);
										
										if(indexExists && canbeExecutedMultipleTimes) {
											 potentialIndices.add(indexField.getValue());
											 rewriteToModule(ast, rewritedQuery);
										}
										else {
											ArrayList<Object> possibleAst = new ArrayList<>();
											possibleAst.add(rewritedQuery);
											possibleAst.add(ast);
											multiMap.put(indexField.getValue(),possibleAst); 
										}
										}
										return module;
									}
								}
							} catch (EolModelElementTypeNotFoundException e) {
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
		Expression firstOperand = ((OperatorExpression) ast).getFirstOperand();
		
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
	
	public void rewriteToModule(ModuleElement ast, OperationCallExpression rewritedQuery) {
		if (ast.getParent() instanceof ExpressionStatement)
			((ExpressionStatement) ast.getParent()).setExpression(rewritedQuery);
		else if (ast.getParent() instanceof AssignmentStatement)
			((AssignmentStatement) ast.getParent()).setValueExpression(rewritedQuery);
		else if (ast.getParent() instanceof ForStatement)
			((ForStatement) ast.getParent()).setIteratedExpression(rewritedQuery);
		else if (ast.getParent() instanceof ReturnStatement)
			((ReturnStatement) ast.getParent()).setReturnedExpression(rewritedQuery);
		else
			((OperationCallExpression) ast.getParent()).setTargetExpression(rewritedQuery);
	}
	
	public IEolModule rewriteIfIndexAlreadyExists(IEolModule module, Map<String, ArrayList<Object>> data, List<String> potentialIndices) {
		this.module = module;
		for(String potentialIndex : potentialIndices) {
		if(data.containsKey(potentialIndex)){
			ModuleElement ast = (ModuleElement) data.get(potentialIndex).get(1);
			OperationCallExpression rewritedQuery = (OperationCallExpression) data.get(potentialIndex).get(0);
			rewriteToModule(ast, rewritedQuery);
		}
		}
		return module;
		
	}

}
