package org.eclipse.epsilon.evl.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.ExecutableBlock;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.GreaterThanOperatorExpression;
import org.eclipse.epsilon.eol.dom.IfStatement;
import org.eclipse.epsilon.eol.dom.IntegerLiteral;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperatorExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.ReturnStatement;
import org.eclipse.epsilon.eol.dom.Statement;
import org.eclipse.epsilon.eol.dom.StatementBlock;
import org.eclipse.epsilon.eol.dom.StringLiteral;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.dom.Constraint;

public class EvlMySqlRewriter {
	
	String tablename;
	String features;
	String conditions;
	String conditionOperator;
	String parameters;
	String limit;
	boolean optimisable;
	HashMap<String, Boolean> inject = new HashMap<>();
	String printParameter = "";
	
	public void rewrite(IModel model, IEolModule module, IEolCompilationContext context) {
		EvlModule evlModule = (EvlModule) module;
		List<Statement> statements = new ArrayList<Statement>();
		for(Constraint constraint : evlModule.getConstraints()) {
			if(	constraint.getCheckBlock().getBody() instanceof StatementBlock) {
				statements = ((StatementBlock)constraint.getCheckBlock().getBody()).getStatements();
				optimiseStatementBlock(model, module, statements, context);
			}else {
				optimisable = true;
				inject.put("injectPrintln", false);
				inject.put("exists", false);
				printParameter = "";
				List<ModuleElement> ast = constraint.getCheckBlock().getChildren();
				optimiseAST(model, ast, context);
			}
		}
	}

	public void optimiseStatementBlock(IModel model, IEolModule module, List<Statement> statements, IEolCompilationContext context) {
		
		for (Statement statement : statements) {
			optimisable = true;
			inject.put("injectPrintln", false);
			inject.put("exists", false);
			printParameter = "";
			if (statement instanceof ForStatement) {
				optimiseAST(model, Arrays.asList(statement.getChildren().get(1)), context);
				List<Statement> childStatements = ((ForStatement) statement).getBodyStatementBlock().getStatements();
				optimiseStatementBlock(model, module, childStatements, context);
			}

			else if (statement instanceof IfStatement) {
				StatementBlock thenBlock = ((IfStatement) statement).getThenStatementBlock();
				if (thenBlock != null) {
					List<Statement> thenStatements = thenBlock.getStatements();
					optimiseStatementBlock(model, module, thenStatements, context);
				}
				StatementBlock elseBlock = ((IfStatement) statement).getElseStatementBlock();
				if (elseBlock != null) {
					List<Statement> elseStatements = ((IfStatement) statement).getElseStatementBlock().getStatements();
					optimiseStatementBlock(model, module, elseStatements, context);
				}
			}
			
			else if (statement instanceof AssignmentStatement) {
				
				List<ModuleElement> targetAsts = Arrays.asList(statement.getChildren().get(0));
				optimiseAST(model, targetAsts, context);
				
				List<ModuleElement> valueAsts = Arrays.asList(statement.getChildren().get(1));
				optimiseAST(model, valueAsts, context);		
			}
			else {
				List<ModuleElement> asts = statement.getChildren();
				optimiseAST(model, asts, context);
			}
		}
	}

	public void optimiseAST(IModel model, List<ModuleElement> asts, IEolCompilationContext context) {
		
		for (ModuleElement ast : asts) {

			NameExpression target = new NameExpression(model.getName());
			NameExpression operation = new NameExpression("runSql");
			StringLiteral p = new StringLiteral(translateToSql(model, ast, context));

			if (!p.getValue().equalsIgnoreCase("Not optimisable")) {
				Expression rewritedQuery = new OperationCallExpression(target, operation, p);
				if(inject.get("exists")) {
					IntegerLiteral i = new IntegerLiteral(0);
					i.setText("0");
					rewritedQuery = new GreaterThanOperatorExpression(new OperationCallExpression(rewritedQuery, new NameExpression("size")),i);
				}
				if (inject.get("injectPrintln")) {
					if (printParameter.equals(""))
						rewritedQuery = new OperationCallExpression(rewritedQuery, new NameExpression("println"));

					else
						rewritedQuery = new OperationCallExpression(rewritedQuery, new NameExpression("println"),new StringLiteral(printParameter));
				}

				if (ast.getParent() instanceof ExpressionStatement)
					((ExpressionStatement) ast.getParent()).setExpression(rewritedQuery);
				else if (ast.getParent() instanceof AssignmentStatement)
					((AssignmentStatement) ast.getParent()).setValueExpression(rewritedQuery);
				else if (ast.getParent() instanceof ForStatement)
					((ForStatement) ast.getParent()).setIteratedExpression(rewritedQuery);
				else if (ast.getParent() instanceof ReturnStatement)
					((ReturnStatement) ast.getParent()).setReturnedExpression(rewritedQuery);
				else if (ast.getParent() instanceof OperationCallExpression)
					((OperationCallExpression) ast.getParent()).setTargetExpression(rewritedQuery);
				else
					((ExecutableBlock<?>) ast.getParent()).setBody(rewritedQuery);
			}
		}

	}

	public String translateToSql(IModel model, ModuleElement ast, IEolCompilationContext context) {
		tablename = "";
		features = "";
		conditions = "";
		parameters = "";
		limit = "";

		if (ast instanceof OperationCallExpression
				&& !(((OperationCallExpression) ast).getTargetExpression() instanceof NameExpression)) {

			for (ModuleElement astChild : ast.getChildren()) {
				if (astChild instanceof OperationCallExpression)
					astToSql(model, (OperationCallExpression) astChild, context);

				if (astChild instanceof PropertyCallExpression)
					astToSql(model, (PropertyCallExpression) astChild, context);
				
				if (astChild instanceof FirstOrderOperationCallExpression)
					astToSql(model, (FirstOrderOperationCallExpression) astChild, context);
			}
			astToSql(model, (OperationCallExpression) ast, context);
		}

		if (ast instanceof PropertyCallExpression) {
			if (!(((PropertyCallExpression) ast).getTargetExpression() instanceof NameExpression)) {

				for (ModuleElement astChild : ast.getChildren()) {
					if (astChild instanceof OperationCallExpression)
						astToSql(model, (OperationCallExpression) astChild, context);

					if (astChild instanceof PropertyCallExpression)
						astToSql(model, (PropertyCallExpression) astChild, context);
				}
			}
			astToSql(model, (PropertyCallExpression) ast, context);
		}
		
		if (ast instanceof FirstOrderOperationCallExpression) {
			if (!(((FirstOrderOperationCallExpression) ast).getTargetExpression() instanceof NameExpression)) {

				for (ModuleElement astChild : ast.getChildren()) {
					if (astChild instanceof OperationCallExpression)
						astToSql(model, (OperationCallExpression) astChild, context);

					if (astChild instanceof PropertyCallExpression)
						astToSql(model, (PropertyCallExpression) astChild, context);
					
					if (astChild instanceof FirstOrderOperationCallExpression)
						astToSql(model, (FirstOrderOperationCallExpression) astChild, context);
				}
			}
			astToSql(model, (FirstOrderOperationCallExpression) ast, context);
		}

		if (optimisable) {
			if (tablename.isEmpty())
				return "Not optimisable";
			if (conditions.isEmpty() && limit.isEmpty())
				return "SELECT " + features + " FROM " + tablename;

			else if (conditions.isEmpty())
				return "SELECT " + features + " FROM " + tablename + " limit " + limit;

			else
				return "SELECT " + features + " FROM " + tablename + " WHERE " + conditions + conditionOperator + "'" +parameters + "'";

		} else
			return "Not optimisable";
	}

	public void astToSql(IModel model, OperationCallExpression ast, IEolCompilationContext context) {
		if (!(ast.getTargetExpression() instanceof NameExpression) || (ast.getChildren() != null)) {
			for (ModuleElement astChild : ast.getChildren()) {
				if (astChild instanceof OperationCallExpression)
					astToSql(model, (OperationCallExpression) astChild, context);

				if (astChild instanceof PropertyCallExpression)
					astToSql(model, (PropertyCallExpression) astChild, context);
				
				if (astChild instanceof FirstOrderOperationCallExpression)
					astToSql(model, (FirstOrderOperationCallExpression) astChild, context);
			}
			if (ast.getName().equals("size"))
				features = "COUNT(" + features + ")";
			if (ast.getName().equals("asSet"))
				features = "DISTINCT " + features;
			if (ast.getName().equals("first")) {
				limit = "1";
			}
			if (ast.getName().equals("println")) {
				inject.put("injectPrintln", true);
				if(!(ast.getParameterExpressions().isEmpty()))
				printParameter = ((StringLiteral)ast.getParameterExpressions().get(0)).getValue();
			}
		}
	}
	
	public void astToSql(IModel model, FirstOrderOperationCallExpression ast, IEolCompilationContext context) {
		if (!(ast.getTargetExpression() instanceof NameExpression) || (ast.getChildren() != null)) {
			for (ModuleElement astChild : ast.getChildren()) {
				if (astChild instanceof OperationCallExpression)
					astToSql(model, (OperationCallExpression) astChild, context);

				if (astChild instanceof PropertyCallExpression)
					astToSql(model, (PropertyCallExpression) astChild, context);
			}
			
			if (ast.getName().equals("select")) {
				OperatorExpression iterator = (OperatorExpression)ast.getExpressions().get(0);
				conditionOperator = " "+iterator.getOperator()+" ";
				if(iterator.getFirstOperand() instanceof PropertyCallExpression) {
				conditions = ((PropertyCallExpression)iterator.getFirstOperand()).getName();
				if(iterator.getSecondOperand() instanceof StringLiteral)
				parameters = ((StringLiteral)iterator.getSecondOperand()).getValue();
				if(iterator.getSecondOperand() instanceof PropertyCallExpression)
					parameters = "+"+iterator.getSecondOperand()+"+";
				}
				
			}
			if (ast.getName().equals("exists")) {
				OperatorExpression iterator = (OperatorExpression)ast.getExpressions().get(0);
				conditionOperator = " "+iterator.getOperator()+" ";
				if(iterator.getFirstOperand() instanceof PropertyCallExpression) {
				conditions = ((PropertyCallExpression)iterator.getFirstOperand()).getName();
				parameters = ((StringLiteral)iterator.getSecondOperand()).getValue();
				}
				inject.put("exists", true);
			}
			
		}
	}

	public void astToSql(IModel model, PropertyCallExpression ast, IEolCompilationContext context) {
		if (!(ast.getTargetExpression() instanceof NameExpression) || (ast.getChildren() != null)) {
			for (ModuleElement astChild : ast.getChildren()) {
				if (astChild instanceof OperationCallExpression)
					astToSql(model, (OperationCallExpression) astChild, context);

				if (astChild instanceof PropertyCallExpression)
					astToSql(model, (PropertyCallExpression) astChild, context);
			}
			if (ast.getName().equals("all") || ast.getName().equals("allInstances")) {
				if (ast.getTargetExpression().getResolvedType() instanceof EolModelElementType) {

					IModel m = null;
					try {
						m = ((EolModelElementType) ast.getTargetExpression().getResolvedType()).getModel(context);
					} catch (EolModelElementTypeNotFoundException e) {

						e.printStackTrace();
					}

					if (m == model) {
						tablename = ((EolModelElementType) ast.getTargetExpression().getResolvedType()).getTypeName();
					} else {
						optimisable = false;

					}
					features = "*";

				}

			} else {
				features = ast.getName();
			}
		}
	}

}
