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
import org.eclipse.epsilon.eol.dom.AbortStatement;
import org.eclipse.epsilon.eol.dom.AndOperatorExpression;
import org.eclipse.epsilon.eol.dom.AnnotationBlock;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.BooleanLiteral;
import org.eclipse.epsilon.eol.dom.BreakStatement;
import org.eclipse.epsilon.eol.dom.Case;
import org.eclipse.epsilon.eol.dom.CollectionLiteralExpression;
import org.eclipse.epsilon.eol.dom.ComplexOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ContinueStatement;
import org.eclipse.epsilon.eol.dom.DeleteStatement;
import org.eclipse.epsilon.eol.dom.DivOperatorExpression;
import org.eclipse.epsilon.eol.dom.DoubleEqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.ElvisOperatorExpression;
import org.eclipse.epsilon.eol.dom.EnumerationLiteralExpression;
import org.eclipse.epsilon.eol.dom.EqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.ExecutableAnnotation;
import org.eclipse.epsilon.eol.dom.ExecutableBlock;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.ExpressionInBrackets;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FeatureCallExpression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.GreaterEqualOperatorExpression;
import org.eclipse.epsilon.eol.dom.GreaterThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.IEolVisitor;
import org.eclipse.epsilon.eol.dom.IfStatement;
import org.eclipse.epsilon.eol.dom.ImpliesOperatorExpression;
import org.eclipse.epsilon.eol.dom.Import;
import org.eclipse.epsilon.eol.dom.IntegerLiteral;
import org.eclipse.epsilon.eol.dom.ItemSelectorExpression;
import org.eclipse.epsilon.eol.dom.LessEqualOperatorExpression;
import org.eclipse.epsilon.eol.dom.LessThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.MapLiteralExpression;
import org.eclipse.epsilon.eol.dom.MinusOperatorExpression;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.dom.ModelDeclarationParameter;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.NegativeOperatorExpression;
import org.eclipse.epsilon.eol.dom.NewInstanceExpression;
import org.eclipse.epsilon.eol.dom.NotEqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.NotOperatorExpression;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperatorExpression;
import org.eclipse.epsilon.eol.dom.OrOperatorExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.PlusOperatorExpression;
import org.eclipse.epsilon.eol.dom.PostfixOperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.RealLiteral;
import org.eclipse.epsilon.eol.dom.ReturnStatement;
import org.eclipse.epsilon.eol.dom.SimpleAnnotation;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.dom.SwitchStatement;
import org.eclipse.epsilon.eol.dom.TernaryExpression;
import org.eclipse.epsilon.eol.dom.ThrowStatement;
import org.eclipse.epsilon.eol.dom.TimesOperatorExpression;
import org.eclipse.epsilon.eol.dom.TransactionStatement;
import org.eclipse.epsilon.eol.dom.TypeExpression;
import org.eclipse.epsilon.eol.dom.VariableDeclaration;
import org.eclipse.epsilon.eol.dom.WhileStatement;
import org.eclipse.epsilon.eol.dom.XorOperatorExpression;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.staticanalyser.CallGraphGenerator;
import org.eclipse.epsilon.eol.types.EolModelElementType;

public class EolEmfRewriterVisitorVersion implements IEolVisitor {

	HashSet<String> optimisableOperations = new HashSet<String>(Arrays.asList("select", "exists"));
	HashSet<String> allOperations = new HashSet<String>(Arrays.asList("all", "allInstances"));

	HashMap<String, HashSet<String>> potentialIndices = new HashMap<>();

	List<ModuleElement> decomposedAsts;

	boolean cascaded = false;
	boolean secondPass = false;
	boolean indexExists = false;
	boolean canbeExecutedMultipleTimes = false;
	boolean optimisableByCurrentModel = false;
	boolean logicalOperator = false;

	IEolModule module;
	IModel model;
	String modelName;

	Expression rewritedQuery;
	NameExpression targetExp;
	NameExpression operationExp;
	StringLiteral modelElementName;
	StringLiteral indexField;
	Parameter param;

	@Override
	public void visit(AbortStatement abortStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AndOperatorExpression andOperatorExpression) {
		cascaded = false;
		logicalOperator = true;
		decomposedAsts = new ArrayList<ModuleElement>();
		decomposedAsts = decomposeAST(andOperatorExpression);
		if (cascaded && !secondPass)
			decomposedAsts.add((andOperatorExpression.getSecondOperand()));
		rewritedQuery = new OperationCallExpression();

		for (ModuleElement operand : decomposedAsts) {
			visit((OperatorExpression) operand, "and");
			if (!logicalOperator)
				return;
		}
	}

	@Override
	public void visit(DeleteStatement deleteStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AnnotationBlock annotationBlock) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(AssignmentStatement assignmentStatement) {
		assignmentStatement.getTargetExpression().accept(this);
		assignmentStatement.getValueExpression().accept(this);
	}

	@Override
	public void visit(BooleanLiteral booleanLiteral) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(BreakStatement breakStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Case case_) {
		if (case_.getCondition() != null) {
			case_.getCondition().accept(this);
		} else
			case_.getBody().accept(this);
	}

	@Override
	public void visit(CollectionLiteralExpression<?> collectionLiteralExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ComplexOperationCallExpression complexOperationCallExpression) {
		if (complexOperationCallExpression.getTargetExpression() != null)
			complexOperationCallExpression.getTargetExpression().accept(this);
	}

	@Override
	public void visit(ContinueStatement continueStatement) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(DivOperatorExpression divOperatorExpression) {
		if (divOperatorExpression.getFirstOperand() != null)
			divOperatorExpression.getFirstOperand().accept(this);
		if (divOperatorExpression.getSecondOperand() != null)
			divOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(DoubleEqualsOperatorExpression doubleEqualsOperatorExpression) {
		if (doubleEqualsOperatorExpression.getFirstOperand() != null)
			doubleEqualsOperatorExpression.getFirstOperand().accept(this);
		if (doubleEqualsOperatorExpression.getSecondOperand() != null)
			doubleEqualsOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(ElvisOperatorExpression elvisOperatorExpression) {
		if (elvisOperatorExpression.getFirstOperand() != null)
			elvisOperatorExpression.getFirstOperand().accept(this);
		if (elvisOperatorExpression.getSecondOperand() != null)
			elvisOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(EnumerationLiteralExpression enumerationLiteralExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(EqualsOperatorExpression equalsOperatorExpression) {
		if (optimisableByCurrentModel) {
			Expression firstOperand = equalsOperatorExpression.getFirstOperand();
			if (firstOperand != null && firstOperand instanceof PropertyCallExpression)
				visit((PropertyCallExpression) firstOperand, true);

			ModuleElement indexValueExpression = equalsOperatorExpression.getSecondOperand();
			Expression indexValue = new IndexValueGenerator(indexValueExpression).generateIndexValue();
			indexExists = false;

			if (potentialIndices.get(modelElementName.getValue()).contains(indexField.getValue())) {
				indexExists = true;
			}

			rewritedQuery = new OperationCallExpression(targetExp, operationExp, modelElementName, indexField,
					indexValue);

			if (indexExists || canbeExecutedMultipleTimes) {
				potentialIndices.get(modelElementName.getValue()).add(indexField.getValue());

			}
		} else {
			if (equalsOperatorExpression.getFirstOperand() != null)
				equalsOperatorExpression.getFirstOperand().accept(this);
			if (equalsOperatorExpression.getSecondOperand() != null)
				equalsOperatorExpression.getSecondOperand().accept(this);
		}
	}

	@Override
	public void visit(ExecutableAnnotation executableAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExecutableBlock<?> executableBlock) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ExpressionInBrackets expressionInBrackets) {
		expressionInBrackets.getExpression().accept(this);
	}

	@Override
	public void visit(ExpressionStatement expressionStatement) {
		expressionStatement.getExpression().accept(this);

	}

	@Override
	public void visit(FirstOrderOperationCallExpression firstOrderOperationCallExpression) {
		logicalOperator = false;
		rewritedQuery = new OperationCallExpression();
		if (optimisableOperations.contains(firstOrderOperationCallExpression.getName())) {
			if (firstOrderOperationCallExpression.getTargetExpression() instanceof PropertyCallExpression) {
				PropertyCallExpression target = (PropertyCallExpression) firstOrderOperationCallExpression
						.getTargetExpression();
				visit(target, false);
			}
			if (optimisableByCurrentModel) {
				Iterator<Parameter> pi = firstOrderOperationCallExpression.getParameters().iterator();
				while (pi.hasNext()) {
					pi.next().accept(this);
				}

				if (!firstOrderOperationCallExpression.getExpressions().isEmpty()) {
					Iterator<Expression> ei = firstOrderOperationCallExpression.getExpressions().iterator();
					while (ei.hasNext()) {
						param = firstOrderOperationCallExpression.getParameters().get(0);
						ei.next().accept(this);
					}
				}
				if(firstOrderOperationCallExpression.getName().equals("exists")) {
					IntegerLiteral i = new IntegerLiteral(0);
					i.setText("0");
					rewritedQuery = new GreaterThanOperatorExpression(new OperationCallExpression(rewritedQuery, new NameExpression("size")),i);
				}
				if ((optimisableByCurrentModel && (indexExists || canbeExecutedMultipleTimes)) || logicalOperator) {
					new ModuleElementRewriter(firstOrderOperationCallExpression, rewritedQuery).rewrite();
					optimisableByCurrentModel = false;
				}
				optimisableByCurrentModel = false;
			} else {
				optimisableByCurrentModel = false;
				firstOrderOperationCallExpression.getTargetExpression().accept(this);
				Iterator<Parameter> pi = firstOrderOperationCallExpression.getParameters().iterator();
				while (pi.hasNext()) {
					pi.next().accept(this);
				}

				if (!firstOrderOperationCallExpression.getExpressions().isEmpty()) {
					Iterator<Expression> ei = firstOrderOperationCallExpression.getExpressions().iterator();
					while (ei.hasNext()) {
						param = firstOrderOperationCallExpression.getParameters().get(0);
						ei.next().accept(this);
					}
				}

			}
		}

	}

	@Override
	public void visit(ForStatement forStatement) {
		forStatement.getIteratorParameter().accept(this);
		forStatement.getIteratedExpression().accept(this);
		canbeExecutedMultipleTimes = true;
		forStatement.getBodyStatementBlock().accept(this);
		canbeExecutedMultipleTimes = false;

	}

	@Override
	public void visit(GreaterEqualOperatorExpression greaterEqualOperatorExpression) {
		if (greaterEqualOperatorExpression.getFirstOperand() != null)
			greaterEqualOperatorExpression.getFirstOperand().accept(this);
		if (greaterEqualOperatorExpression.getSecondOperand() != null)
			greaterEqualOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(GreaterThanOperatorExpression greaterThanOperatorExpression) {
		if (greaterThanOperatorExpression.getFirstOperand() != null)
			greaterThanOperatorExpression.getFirstOperand().accept(this);
		if (greaterThanOperatorExpression.getSecondOperand() != null)
			greaterThanOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(IfStatement ifStatement) {
		ifStatement.getConditionExpression().accept(this);
		ifStatement.getThenStatementBlock().accept(this);
		if (ifStatement.getElseStatementBlock() != null) {
			StatementBlock elseStatementBlock = ifStatement.getElseStatementBlock();
			if (elseStatementBlock.getStatements().size() == 1
					&& elseStatementBlock.getStatements().get(0) instanceof IfStatement) {
				elseStatementBlock.getStatements().get(0).accept(this);
			} else {
				ifStatement.getElseStatementBlock().accept(this);
			}
		}
	}

	@Override
	public void visit(ImpliesOperatorExpression impliesOperatorExpression) {
		if (impliesOperatorExpression.getFirstOperand() != null)
			impliesOperatorExpression.getFirstOperand().accept(this);
		if (impliesOperatorExpression.getSecondOperand() != null)
			impliesOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(Import import_) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IntegerLiteral integerLiteral) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ItemSelectorExpression itemSelectorExpression) {
		itemSelectorExpression.getTargetExpression().accept(this);
		itemSelectorExpression.getIndexExpression().accept(this);
	}

	@Override
	public void visit(LessEqualOperatorExpression lessEqualOperatorExpression) {
		if (lessEqualOperatorExpression.getFirstOperand() != null)
			lessEqualOperatorExpression.getFirstOperand().accept(this);
		if (lessEqualOperatorExpression.getSecondOperand() != null)
			lessEqualOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(LessThanOperatorExpression lessThanOperatorExpression) {
		if (lessThanOperatorExpression.getFirstOperand() != null)
			lessThanOperatorExpression.getFirstOperand().accept(this);
		if (lessThanOperatorExpression.getSecondOperand() != null)
			lessThanOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(MapLiteralExpression<?, ?> mapLiteralExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(MinusOperatorExpression minusOperatorExpression) {
		if (minusOperatorExpression.getFirstOperand() != null)
			minusOperatorExpression.getFirstOperand().accept(this);
		if (minusOperatorExpression.getSecondOperand() != null)
			minusOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(ModelDeclaration modelDeclaration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ModelDeclarationParameter modelDeclarationParameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NameExpression nameExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(NegativeOperatorExpression negativeOperatorExpression) {
		if (negativeOperatorExpression.getFirstOperand() != null)
			negativeOperatorExpression.getFirstOperand().accept(this);
		if (negativeOperatorExpression.getSecondOperand() != null)
			negativeOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(NewInstanceExpression newInstanceExpression) {
		newInstanceExpression.getTypeExpression().accept(this);
		Iterator<Expression> pi = newInstanceExpression.getParameterExpressions().iterator();
		while (pi.hasNext()) {
			pi.next().accept(this);
		}

	}

	@Override
	public void visit(NotEqualsOperatorExpression notEqualsOperatorExpression) {
		if (notEqualsOperatorExpression.getFirstOperand() != null)
			notEqualsOperatorExpression.getFirstOperand().accept(this);
		if (notEqualsOperatorExpression.getSecondOperand() != null)
			notEqualsOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(NotOperatorExpression notOperatorExpression) {
		if (notOperatorExpression.getFirstOperand() != null)
			notOperatorExpression.getFirstOperand().accept(this);
		if (notOperatorExpression.getSecondOperand() != null)
			notOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(Operation operation) {
		operation.getBody().accept(this);

	}

	@Override
	public void visit(OperationCallExpression operationCallExpression) {
//		operationCallExpression.getTargetExpression().accept(this);
		String operationName = operationCallExpression.getName();

		if (allOperations.contains(operationName)) {

			EolModelElementType modelElement = null;
			if (operationCallExpression.getTargetExpression().getResolvedType() instanceof EolModelElementType)
				modelElement = (EolModelElementType) operationCallExpression.getTargetExpression().getResolvedType();

			try {
				if (modelElement.getModel(module.getCompilationContext()) == model) {
					optimisableByCurrentModel = true;
					modelName = modelElement.getModelName();
					model.setName(modelName);
					targetExp = new NameExpression(modelName);
					operationExp = new NameExpression("findByIndex");
					modelElementName = new StringLiteral(modelElement.getTypeName());

					if (potentialIndices.get(modelElementName.getValue()) == null) {
						potentialIndices.put(modelElementName.getValue(), new HashSet<String>());
					}
				} else
					optimisableByCurrentModel = false;

			} catch (EolModelElementTypeNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (operationCallExpression.getTargetExpression() != null)
			operationCallExpression.getTargetExpression().accept(this);
		if ((!operationCallExpression.getParameterExpressions().isEmpty())) {
			Iterator<Expression> pi = operationCallExpression.getParameterExpressions().iterator();
			while (pi.hasNext()) {
				pi.next().accept(this);
			}
		}
	}

	@Override
	public void visit(OrOperatorExpression orOperatorExpression) {
		if (optimisableByCurrentModel) {
			logicalOperator = true;
			cascaded = false;
			decomposedAsts = new ArrayList<ModuleElement>();
			decomposedAsts = decomposeAST(orOperatorExpression);

			if (cascaded && !secondPass)
				decomposedAsts.add((orOperatorExpression.getSecondOperand()));

			rewritedQuery = new OperationCallExpression();

			for (ModuleElement operand : decomposedAsts) {
				visit((OperatorExpression) operand, "or");
				if (!logicalOperator)
					return;
			}
		} else {
			if (orOperatorExpression.getFirstOperand() != null)
				orOperatorExpression.getFirstOperand().accept(this);
			if (orOperatorExpression.getSecondOperand() != null)
				orOperatorExpression.getSecondOperand().accept(this);
		}

	}

	public void visit(OperatorExpression operand, String logicalOperatorName) {
		if (optimisableByCurrentModel) {
			if (logicalOperatorName.equals("or")) {
				if (operand instanceof EqualsOperatorExpression) {
					operand = (EqualsOperatorExpression) operand;
					Expression firstOperand = ((EqualsOperatorExpression) operand).getFirstOperand();
					if (firstOperand != null && firstOperand instanceof PropertyCallExpression)
						visit((PropertyCallExpression) firstOperand, true);
					ModuleElement indexValueExpression = ((EqualsOperatorExpression) operand).getSecondOperand();
					Expression indexValue = new IndexValueGenerator(indexValueExpression).generateIndexValue();

					indexExists = false;

					if (potentialIndices.get(modelElementName.getValue()).contains(indexField.getValue())) {
						indexExists = true;
					}
					if (!(indexExists || canbeExecutedMultipleTimes) && ((FeatureCallExpression) rewritedQuery).getName() == null) {
						logicalOperator = false;
						return;
					}
					if (((FeatureCallExpression) rewritedQuery).getName() == null)
						rewritedQuery = new OperationCallExpression(targetExp, operationExp, modelElementName,
								indexField, indexValue);
					else if (!indexExists && !canbeExecutedMultipleTimes) {
						FirstOrderOperationCallExpression temp = new FirstOrderOperationCallExpression(
								new PropertyCallExpression(param.getTypeExpression(), new NameExpression("all")),
								new NameExpression("select"), param,
								new EqualsOperatorExpression(new PropertyCallExpression(param.getNameExpression(),
										new NameExpression(indexField.getValue())), indexValue));
						rewritedQuery = new OperationCallExpression(rewritedQuery, new NameExpression("includingAll"),
								temp);
					} else {
						rewritedQuery = new OperationCallExpression(rewritedQuery, new NameExpression("includingAll"),
								new OperationCallExpression(targetExp, operationExp, modelElementName, indexField,
										indexValue));
					}
					if (indexExists || canbeExecutedMultipleTimes) {
						potentialIndices.get(modelElementName.getValue()).add(indexField.getValue());
					}
				} else {
					rewritedQuery = new FirstOrderOperationCallExpression(rewritedQuery, new NameExpression("select"),
							param, (Expression) operand);
//			break;
				}
			}
			if (logicalOperatorName.equals("and")) {
				boolean flag = false;
				if (operand instanceof EqualsOperatorExpression) {
					operand = (EqualsOperatorExpression) operand;
					Expression firstOperand = ((EqualsOperatorExpression) operand).getFirstOperand();
					if (firstOperand != null && firstOperand instanceof PropertyCallExpression)
						visit((PropertyCallExpression) firstOperand, true);
					ModuleElement indexValueExpression = ((EqualsOperatorExpression) operand).getSecondOperand();
					Expression indexValue = new IndexValueGenerator(indexValueExpression).generateIndexValue();

					indexExists = false;

					if (potentialIndices.get(modelElementName.getValue()).contains(indexField.getValue())) {
						indexExists = true;
					}
					if (!(indexExists || canbeExecutedMultipleTimes) && ((FeatureCallExpression) rewritedQuery).getName() == null) {
						logicalOperator = false;
						return;
					}
					if (((FeatureCallExpression) rewritedQuery).getName() == null)
						rewritedQuery = new OperationCallExpression(targetExp, operationExp, modelElementName,
								indexField, indexValue);
					else if ((indexExists && !canbeExecutedMultipleTimes) || !indexExists
							|| canbeExecutedMultipleTimes) {
						if (!(((FeatureCallExpression) firstOperand).getTargetExpression() instanceof NameExpression)) {
							rewritedQuery = new FirstOrderOperationCallExpression(rewritedQuery,
									new NameExpression("select"), param,
									new EqualsOperatorExpression(new PropertyCallExpression(
											((FeatureCallExpression) firstOperand).getTargetExpression(),
											new NameExpression(indexField.getValue())), indexValue));

						} else
							rewritedQuery = new FirstOrderOperationCallExpression(rewritedQuery,
									new NameExpression("select"), param,
									new EqualsOperatorExpression(new PropertyCallExpression(param.getNameExpression(),
											new NameExpression(indexField.getValue())), indexValue));
						flag = true;
					}
					if ((indexExists || canbeExecutedMultipleTimes) && !flag) {
						potentialIndices.get(modelElementName.getValue()).add(indexField.getValue());
					}
				} else {
					rewritedQuery = new FirstOrderOperationCallExpression(rewritedQuery, new NameExpression("select"),
							param, (Expression) operand);
//				break;
				}
			}
		}
	}

	@Override
	public void visit(Parameter parameter) {
		if (parameter.getTypeExpression() != null) {
			parameter.getTypeExpression().accept(this);
		}

	}

	@Override
	public void visit(PlusOperatorExpression plusOperatorExpression) {
		if (plusOperatorExpression.getFirstOperand() != null)
			plusOperatorExpression.getFirstOperand().accept(this);
		if (plusOperatorExpression.getSecondOperand() != null)
			plusOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(PostfixOperatorExpression postfixOperatorExpression) {
		if (postfixOperatorExpression.getAssignmentStatement() != null)
			postfixOperatorExpression.getAssignmentStatement().accept(this);
		if (postfixOperatorExpression.getFirstOperand() != null)
			postfixOperatorExpression.getFirstOperand().accept(this);
		if (postfixOperatorExpression.getSecondOperand() != null)
			postfixOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(PropertyCallExpression propertyCallExpression) {
		propertyCallExpression.getTargetExpression().accept(this);
	}

	public void visit(PropertyCallExpression propertyCallExpression, boolean flag) {
		if (flag)
			indexField = new StringLiteral(propertyCallExpression.getName());
		else {
			propertyCallExpression.getTargetExpression().accept(this);
			String operationName = propertyCallExpression.getName();

			if (allOperations.contains(operationName)) {

				EolModelElementType modelElement = null;
				if (propertyCallExpression.getTargetExpression().getResolvedType() instanceof EolModelElementType)
					modelElement = (EolModelElementType) propertyCallExpression.getTargetExpression().getResolvedType();

				try {
					if (modelElement.getModel(module.getCompilationContext()) == model) {
						optimisableByCurrentModel = true;
						modelName = modelElement.getModelName();
						model.setName(modelName);
						targetExp = new NameExpression(modelName);
						operationExp = new NameExpression("findByIndex");
						modelElementName = new StringLiteral(modelElement.getTypeName());

						if (potentialIndices.get(modelElementName.getValue()) == null) {
							potentialIndices.put(modelElementName.getValue(), new HashSet<String>());
						}
					} else
						optimisableByCurrentModel = false;

				} catch (EolModelElementTypeNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void visit(RealLiteral realLiteral) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		if (returnStatement.getReturnedExpression() != null) {
			returnStatement.getReturnedExpression().accept(this);
		}
	}

	@Override
	public void visit(SimpleAnnotation simpleAnnotation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(StatementBlock statementBlock) {

		statementBlock.getStatements().forEach(s -> s.accept(this));

	}

	@Override
	public void visit(StringLiteral stringLiteral) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SwitchStatement switchStatement) {
		switchStatement.getConditionExpression().accept(this);
		switchStatement.getCases().forEach(c -> {
			c.accept(this);
		});
		if (switchStatement.getDefault() != null) {
			switchStatement.getDefault().accept(this);
		}
	}

	@Override
	public void visit(TernaryExpression ternaryExpression) {
		if (ternaryExpression.getFirstOperand() != null)
			ternaryExpression.getFirstOperand().accept(this);
		if (ternaryExpression.getSecondOperand() != null)
			ternaryExpression.getSecondOperand().accept(this);
		if (ternaryExpression.getThirdOperand() != null)
			ternaryExpression.getThirdOperand().accept(this);

	}

	@Override
	public void visit(ThrowStatement throwStatement) {
		if (throwStatement.getThrown() != null)
			throwStatement.getThrown().accept(this);
	}

	@Override
	public void visit(TimesOperatorExpression timesOperatorExpression) {
		if (timesOperatorExpression.getFirstOperand() != null)
			timesOperatorExpression.getFirstOperand().accept(this);
		if (timesOperatorExpression.getSecondOperand() != null)
			timesOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(TransactionStatement transactionStatement) {

	}

	@Override
	public void visit(TypeExpression typeExpression) {

	}

	@Override
	public void visit(VariableDeclaration variableDeclaration) {
		if (variableDeclaration.getTypeExpression() != null)
			variableDeclaration.getTypeExpression().accept(this);
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		whileStatement.getConditionExpression().accept(this);
		whileStatement.getBodyStatementBlock().accept(this);
	}

	@Override
	public void visit(XorOperatorExpression xorOperatorExpression) {
		if (xorOperatorExpression.getFirstOperand() != null)
			xorOperatorExpression.getFirstOperand().accept(this);
		if (xorOperatorExpression.getSecondOperand() != null)
			xorOperatorExpression.getSecondOperand().accept(this);
	}

	public void rewrite(IModel model, IEolModule module, IEolCompilationContext context, CallGraphGenerator cg) {

		this.module = module;
		this.model = model;
		if (module.getMain() == null)
			return;

		module.getMain().accept(this);

		for (Operation operation : module.getDeclaredOperations()) {
			String name = replaceSymbolsForGraphviz(operation.toString());
			if (cg.pathContainsLoop("main", name))
				canbeExecutedMultipleTimes = true;
			if (cg.pathExists("main", name))
				operation.accept(this);
			canbeExecutedMultipleTimes = false;
		}

		secondPass = true;
		module.getMain().accept(this);
		injectCreateIndexStatements(module, modelName, potentialIndices);

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

	public void injectCreateIndexStatements(IEolModule module, String modelName,
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

				module.getMain().getStatements().add(count, statement);
				count++;
			}
		}
	}

	private String replaceSymbolsForGraphviz(String str) {
		str = str.replaceAll("[(]", "_");
		str = str.replaceAll("[)]", "_");
		str = str.replaceAll("\\-", "_");
		str = str.replaceAll(":", "_");
		str = str.replaceAll("\\s", "_");
		str = str.replaceAll("!", "_");
		str = str.replaceAll(",", "_");
		return str;
	}

}
