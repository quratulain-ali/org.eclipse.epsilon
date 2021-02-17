package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.BooleanLiteral;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.IntegerLiteral;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.RealLiteral;
import org.eclipse.epsilon.eol.dom.StringLiteral;

public class IndexValueGenerator {

	ModuleElement indexValueExpression;

	public IndexValueGenerator(ModuleElement me) {
		this.indexValueExpression = me;
	}

	public Expression generateIndexValue() {
		Expression indexValue = null;
		if (indexValueExpression instanceof PropertyCallExpression) {
			indexValue = (PropertyCallExpression) indexValueExpression;
		} else if (indexValueExpression instanceof BooleanLiteral) {
			indexValue = (BooleanLiteral) indexValueExpression;
		} else if (indexValueExpression instanceof StringLiteral) {
			indexValue = (StringLiteral) indexValueExpression;
		} else if (indexValueExpression instanceof IntegerLiteral) {
			indexValue = (IntegerLiteral) indexValueExpression;
		} else if (indexValueExpression instanceof RealLiteral) {
			indexValue = (RealLiteral) indexValueExpression;
		} else if (indexValueExpression instanceof OperationCallExpression) {
			indexValue = (OperationCallExpression) indexValueExpression;
		} else if (indexValueExpression instanceof NameExpression) {
			indexValue = (NameExpression) indexValueExpression;
		}
		return indexValue;
	}
}
