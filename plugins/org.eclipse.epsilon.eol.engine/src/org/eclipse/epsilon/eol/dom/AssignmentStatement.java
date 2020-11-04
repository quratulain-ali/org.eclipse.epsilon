/*********************************************************************
* Copyright (c) 2008 The University of York.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.epsilon.eol.dom;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolType;

public class AssignmentStatement extends Statement {
	
	protected Expression targetExpression;
	protected Expression valueExpression;
	
	public AssignmentStatement() {}
	
	public AssignmentStatement(Expression targetExpression, Expression valueExpression) {
		this.targetExpression = targetExpression;
		this.valueExpression = valueExpression;
	}
	
	@Override
	public void build(AST cst, IModule module) {
		super.build(cst, module);
		targetExpression = (Expression) module.createAst(cst.getFirstChild(), this);
		valueExpression = getValueExpression(cst, (Expression) module.createAst(cst.getSecondChild(), this));
	}
	
	/**
	 * 
	 * @param cst
	 * @return
	 * @since 2.2
	 */
	protected Expression getValueExpression(AST cst, Expression expression) {
		switch (cst.getText()) {
			case "+=":
				return new PlusOperatorExpression(targetExpression, expression);
			case "-=":
				return new MinusOperatorExpression(targetExpression, expression);
			case "/=":
				return new DivOperatorExpression(targetExpression, expression);
			case "*=":
				return new TimesOperatorExpression(targetExpression, expression);
			case "?=":
				return new ElvisOperatorExpression(targetExpression, expression);
			default:
				return expression;
		}
	}
	
	@Override
	public Object execute(IEolContext context) throws EolRuntimeException {	
		ExecutorFactory executorFactory = context.getExecutorFactory();
		
		Object valueExpressionResult;
			
		if (targetExpression instanceof PropertyCallExpression) {
			PropertyCallExpression pce = (PropertyCallExpression) targetExpression;
			Object source = executorFactory.execute(pce.getTargetExpression(), context);
			String property = pce.getName();
			IPropertySetter setter = context.getIntrospectionManager().getPropertySetterFor(source, property, context);
			valueExpressionResult = executorFactory.execute(valueExpression, context);
			Object value = getValueEquivalent(source, valueExpressionResult, context);
			try {
				setter.invoke(source, property, value, context);
			}
			catch (EolRuntimeException eox) {
				if (eox.getAst() == null) {
					eox.setAst(this);
				}
				throw eox;
			}
		}
		else {
			Object targetExpressionResult;
			if (targetExpression instanceof NameExpression) {
				NameExpression ne = (NameExpression) targetExpression;
				targetExpressionResult = ne.execute(context, true);
			}
			else {
				targetExpressionResult = executorFactory.execute(targetExpression, context);
			}
			
			if (targetExpressionResult instanceof Variable) {
				Variable variable = (Variable) targetExpressionResult;
				valueExpressionResult = executorFactory.execute(valueExpression, context);
				try {
					Object value = getValueEquivalent(variable.getValue(), valueExpressionResult, context);
					variable.setValue(value, context);
				}
				catch (EolRuntimeException ex) {
					ex.setAst(targetExpression);
					throw ex;
				}
			}
			else {
				throw new EolRuntimeException("Internal error. Expected either a SetterMethod or a Variable and got an " + targetExpressionResult + " instead", this);
			}
		}
		
		return valueExpressionResult;
	}
	
	protected Object getValueEquivalent(Object source, Object value, IEolContext context) throws EolRuntimeException {
		return value;
	}
	
	@Override
	public void compile(IEolCompilationContext context) {

		targetExpression.compile(context);
		valueExpression.compile(context);
		
		EolType targetType = targetExpression.getResolvedType();
		EolType valueType  = valueExpression.getResolvedType();
		
		if(targetType instanceof EolModelElementType && ((EolModelElementType)targetType).getMetaClass()!=null)
			targetType=new EolModelElementType(((EolModelElementType)targetType).getMetaClass());
		if(valueType instanceof EolModelElementType && ((EolModelElementType)valueType).getMetaClass()!=null)
			valueType=new EolModelElementType(((EolModelElementType)valueType).getMetaClass());
		
		if(!(isCompatible(targetType, valueType))) {
			if (canBeCompatible(targetType, valueType))
				context.addWarningMarker(targetExpression, valueExpression.getResolvedType()+" may not be assigned to " + targetType);
			else
					context.addErrorMarker(targetExpression, valueExpression.getResolvedType()+" cannot be assigned to " + targetType);
		}
	}
	
	public Expression getTargetExpression() {
		return targetExpression;
	}
	
	public void setTargetExpression(Expression targetExpression) {
		this.targetExpression = targetExpression;
	}
	
	public Expression getValueExpression() {
		return valueExpression;
	}
	
	public void setValueExpression(Expression valueExpression) {
		this.valueExpression = valueExpression;
	}

	public boolean isCompatible(EolType targetType, EolType valueType) {

		boolean ok = false;

		if (targetType.equals(EolNoType.Instance) || valueType.equals(EolNoType.Instance))
			return false;
		else

			while (!ok) {
				if (!(targetType.equals(valueType)) && !(targetType instanceof EolAnyType)) {

					valueType = valueType.getParentType();

					if (valueType instanceof EolAnyType) {
						return false;
					}

				} else if (targetType instanceof EolAnyType) {
					return true;
				} else if (valueType instanceof EolCollectionType
						&& !((((EolCollectionType) targetType).getContentType()) instanceof EolAnyType)) {

					EolType valueContentType = ((EolCollectionType) valueType).getContentType();
					EolType targetContentType = ((EolCollectionType) targetType).getContentType();

					while (targetContentType instanceof EolCollectionType
							&& valueContentType instanceof EolCollectionType) {
						if (targetContentType.equals(valueContentType)) {
							return isCompatible(((EolCollectionType) targetContentType).getContentType(),
									((EolCollectionType) valueContentType).getContentType());
						} else {
							valueContentType = valueContentType.getParentType();
							return isCompatible(targetContentType, valueContentType);

						}
					}
					while (!ok) {
						if (valueContentType instanceof EolAnyType) {
							return false;
						}
						if (!valueContentType.equals(targetContentType)) {
							valueContentType = valueContentType.getParentType();
						} else {
							return true;
						}
					}
				} else
					return true;
			}
		return false;
	}

	public boolean canBeCompatible(EolType targetType, EolType valueType) {

		boolean ok = false;
		if (targetType == null || valueType == null)
			return false;
		else
			while (!ok) {

				if (!(targetType.equals(valueType)) && !(valueType instanceof EolAnyType)) {

					targetType = targetType.getParentType();

					if (targetType instanceof EolAnyType) {
						return false;
					}

				} else if (valueType instanceof EolAnyType) {
					return true;
				} else if (targetType instanceof EolCollectionType
						&& !((((EolCollectionType) valueType).getContentType()) instanceof EolAnyType)) {

					EolType valueContentType = ((EolCollectionType) valueType).getContentType();
					EolType targetContentType = ((EolCollectionType) targetType).getContentType();

					while (targetContentType instanceof EolCollectionType
							&& valueContentType instanceof EolCollectionType) {
						if (targetContentType.equals(valueContentType)) {
							return canBeCompatible(((EolCollectionType) targetContentType).getContentType(),
									((EolCollectionType) valueContentType).getContentType());
						} else {
							valueContentType = valueContentType.getParentType();
							return canBeCompatible(targetContentType, valueContentType);

						}
					}
					while (!ok) {
						if (valueContentType instanceof EolAnyType || targetContentType instanceof EolAnyType) {
							return true;
						}
						if (!valueContentType.equals(targetContentType)) {
							targetContentType = targetContentType.getParentType();
							if (targetContentType instanceof EolAnyType)
								return false;
						} else {
							return true;
						}
					}
				} else
					return true;
			}
		return false;
	}

	
	public void accept(IEolVisitor visitor) {
		visitor.visit(this);
	}
}
