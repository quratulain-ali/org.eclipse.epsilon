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
import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.Return;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolType;

public class ReturnStatement extends Statement {

	protected Expression returnedExpression;

	public ReturnStatement() {
	}

	public ReturnStatement(Expression returnedExpression) {
		this.returnedExpression = returnedExpression;
	}

	@Override
	public void build(AST cst, IModule module) {
		super.build(cst, module);
		returnedExpression = (Expression) module.createAst(cst.getFirstChild(), this);
	}

	@Override
	public Return execute(IEolContext context) throws EolRuntimeException {

		Object result = null;
		if (returnedExpression != null) {
			result = context.getExecutorFactory().execute(returnedExpression, context);
		}

		return new Return(result);
	}

	@Override
	public void compile(IEolCompilationContext context) {
		if (returnedExpression != null) {

			returnedExpression.compile(context);
			
			
			EolType providedReturnType = returnedExpression.getResolvedType();
			
			ModuleElement parent = returnedExpression.getParent();
			
				while (!(parent instanceof Operation) && parent != null)
				{
					
					parent = parent.getParent();
					
				}
				
				if (parent instanceof Operation) {
				((Operation)parent).returnFlag = true;
				// add for setting resolved type
				if(((Operation) parent).getReturnTypeExpression()== null)
				 ((Operation) parent).setReturnTypeExpression(new TypeExpression("Any"));
				(((Operation) parent).getReturnTypeExpression()).compile(context);
				EolType requiredReturnType = ((Operation) parent).getReturnTypeExpression().getResolvedType();
			

			System.out.println("Required Type" + requiredReturnType);
			System.out.println("Provided Type "+ providedReturnType);
			
			if (! (isCompatible(requiredReturnType, providedReturnType))) {
				if(canBeCompatible(requiredReturnType, providedReturnType))
					context.addWarningMarker(returnedExpression, "Return type might be "+ requiredReturnType+ " instead of "+ returnedExpression.getResolvedType());
				else
					context.addErrorMarker(returnedExpression, "Return type should be "+ requiredReturnType+ " instead of "+ returnedExpression.getResolvedType());

			}	
				}
		}
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

	public Expression getReturnedExpression() {
		return returnedExpression;
	}

	public void setReturnedExpression(Expression returnedExpression) {
		this.returnedExpression = returnedExpression;
	}

	
	public void accept(IEolVisitor visitor) {
		visitor.visit(this);
	}
}
