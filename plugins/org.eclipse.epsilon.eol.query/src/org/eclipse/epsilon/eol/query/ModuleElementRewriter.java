package org.eclipse.epsilon.eol.query;

import java.util.List;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.EqualsOperatorExpression;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
import org.eclipse.epsilon.eol.dom.FeatureCallExpression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.ForStatement;
import org.eclipse.epsilon.eol.dom.NotOperatorExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.ReturnStatement;

public class ModuleElementRewriter {
	
	ModuleElement ast;
	FeatureCallExpression rewritedQuery;
	
	public ModuleElementRewriter(ModuleElement me, FeatureCallExpression rewritedQuery) {
		this.ast = me;
		this.rewritedQuery = rewritedQuery;
	}

	public void rewrite() {
		if (ast.getParent() instanceof ExpressionStatement) {
			ExpressionStatement parent = (ExpressionStatement) ast.getParent();
			if(ast == parent.getExpression())
			parent.setExpression(rewritedQuery);
		}
		else if (ast.getParent() instanceof AssignmentStatement) {
			AssignmentStatement parent = (AssignmentStatement) ast.getParent();
			if(ast == parent.getValueExpression())
				parent.setValueExpression(rewritedQuery);
			else
				parent.setTargetExpression(rewritedQuery);
		}
		else if (ast.getParent() instanceof ForStatement) {
			ForStatement parent = (ForStatement) ast.getParent();
			if(ast == parent.getIteratedExpression())
			parent.setIteratedExpression(rewritedQuery);
		}
		else if (ast.getParent() instanceof ReturnStatement) {
			ReturnStatement parent = (ReturnStatement) ast.getParent();
			if(ast == parent.getReturnedExpression())
			parent.setReturnedExpression(rewritedQuery);
		}
		else if (ast.getParent() instanceof NotOperatorExpression) {
			NotOperatorExpression parent = (NotOperatorExpression) ast.getParent();
			if(ast == parent.getFirstOperand())
			parent.setFirstOperand(rewritedQuery);
			else
				parent.setSecondOperand(rewritedQuery);
		}
		else if (ast.getParent() instanceof PropertyCallExpression) {
			PropertyCallExpression parent = (PropertyCallExpression) ast.getParent();
			if(ast == parent.getTargetExpression())
			parent.setTargetExpression(rewritedQuery);
		}
		else if (ast.getParent() instanceof FirstOrderOperationCallExpression) {
			FirstOrderOperationCallExpression parent = (FirstOrderOperationCallExpression)ast.getParent();
			if(ast == parent.getTargetExpression())
			parent.setTargetExpression(rewritedQuery);
			else {
				List<Expression> parameters = parent.getExpressions();
				for(int i = 0; i < parameters.size(); i++) {
					if(parameters.get(i) == ast) {
					parameters.set(i,rewritedQuery);
					return;
					}
				}
			}
		}
		else if (ast.getParent() instanceof EqualsOperatorExpression) {
			EqualsOperatorExpression parent = (EqualsOperatorExpression) ast.getParent();
			if(ast == parent.getFirstOperand())
				parent.setFirstOperand(rewritedQuery);
			else
				parent.setSecondOperand(rewritedQuery);
			
		}
		else if (ast.getParent() instanceof OperationCallExpression) {
			OperationCallExpression parent = (OperationCallExpression) ast.getParent();
			if(ast == parent.getTargetExpression())
				parent.setTargetExpression(rewritedQuery);
			else {
				List<Expression> parameters = parent.getParameterExpressions();
				for(int i = 0; i < parameters.size(); i++) {
					if(parameters.get(i) == ast) {
					parameters.set(i,rewritedQuery);
					return;
					}
				}
			}
		}
	}
}
