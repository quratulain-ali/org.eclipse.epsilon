package org.eclipse.epsilon.eol.staticanalyser;

import java.util.List;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.module.IModuleValidator;
import org.eclipse.epsilon.common.module.ModuleMarker;
import org.eclipse.epsilon.common.module.String;
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
import org.eclipse.epsilon.eol.dom.ExpressionInBrackets;
import org.eclipse.epsilon.eol.dom.ExpressionStatement;
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

public class EolStaticAnalyser implements IModuleValidator, IEolVisitor {

	@Override
	public void visit(AbortStatement abortStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndOperatorExpression andOperatorExpression) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CollectionLiteralExpression<?> collectionLiteralExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ComplexOperationCallExpression complexOperationCallExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ContinueStatement continueStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DivOperatorExpression divOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleEqualsOperatorExpression doubleEqualsOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ElvisOperatorExpression elvisOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EnumerationLiteralExpression enumerationLiteralExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsOperatorExpression equalsOperatorExpression) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExpressionStatement expressionStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(FirstOrderOperationCallExpression firstOrderOperationCallExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ForStatement forStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterEqualOperatorExpression greaterEqualOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(GreaterThanOperatorExpression greaterThanOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IfStatement ifStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ImpliesOperatorExpression impliesOperatorExpression) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LessEqualOperatorExpression lessEqualOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LessThanOperatorExpression lessThanOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MapLiteralExpression<?, ?> mapLiteralExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinusOperatorExpression minusOperatorExpression) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NewInstanceExpression newInstanceExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotEqualsOperatorExpression notEqualsOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NotOperatorExpression notOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Operation operation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OperationCallExpression operationCallExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OrOperatorExpression orOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parameter parameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PlusOperatorExpression plusOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PostfixOperatorExpression postfixOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PropertyCallExpression propertyCallExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RealLiteral realLiteral) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SimpleAnnotation simpleAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StatementBlock statementBlock) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringLiteral stringLiteral) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SwitchStatement switchStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TernaryExpression ternaryExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ThrowStatement throwStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimesOperatorExpression timesOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TransactionStatement transactionStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TypeExpression typeExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(VariableDeclaration variableDeclaration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(XorOperatorExpression xorOperatorExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public org.eclipse.epsilon.common.module.List<ModuleMarker> validate(IModule module) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.lang.String getMarkerType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public org.eclipse.epsilon.common.module.List<ModuleMarker> validate(IModule module) {
		// TODO Auto-generated method stub
		return null;
	}
}