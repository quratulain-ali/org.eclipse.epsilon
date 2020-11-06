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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.compile.context.IEolCompilationContext;
import org.eclipse.epsilon.eol.exceptions.EolIllegalOperationException;
import org.eclipse.epsilon.eol.exceptions.EolIllegalPropertyException;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.EolUndefinedVariableException;
import org.eclipse.epsilon.eol.execute.ExecutorFactory;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.introspection.java.ObjectMethod;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.contributors.IOperationContributorProvider;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.execute.operations.simple.SimpleOperation;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolModelElementType;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolPrimitiveType;
import org.eclipse.epsilon.eol.types.EolType;
import org.eclipse.epsilon.eol.types.EolUndefined;

public class OperationCallExpression extends FeatureCallExpression {
	
	protected final ArrayList<Expression> parameterExpressions = new ArrayList<>(0);
	public ArrayList<Operation> operations = new ArrayList<Operation>(0); // for combining user-operations and built-in
	// operations
	public ArrayList<Operation> matchedoperations = new ArrayList<Operation>(0); // keeping all the matched functions
			// for using in hyperlink
	public ArrayList<EolType> matchedReturnType = new ArrayList<EolType>(); // keeping return type of matched functions
	// using when the function is a target
	// expression
	//public ArrayList<BuiltinOperations> builtin = new ArrayList<BuiltinOperations>(0); // Importing from eol file
	boolean matched = false; // for find at least one perfect match/ It doesn't change for every mismatch
	// because one match is enough
	protected boolean contextless;
	protected int count = 0;
	protected EolType contextType = EolAnyType.Instance;

	int errorCode = 0; // 1 = mismatch Target 2=number of parameters mismatch 3=parameters type
	// mismatch 4 =undefined Operation // 5 = No-type as target // 6 = No-type as
	// parameter
	
	public OperationCallExpression() {
		this(false);
	}
	
	public OperationCallExpression(boolean contextless) {
		this.contextless = contextless;
	}
	
	public OperationCallExpression(Expression targetExpression, NameExpression nameExpression, Expression... parameterExpressions) {
		this.targetExpression = targetExpression;
		this.nameExpression = nameExpression;
		this.contextless = (targetExpression == null);
		if (parameterExpressions != null) {
			this.parameterExpressions.ensureCapacity(parameterExpressions.length);
			for (Expression parameterExpression : parameterExpressions) {
				this.parameterExpressions.add(parameterExpression);
			}
		}
	}
	
	@Override
	public void build(AST cst, IModule module) {
		super.build(cst, module);
		AST parametersAst = null;
		if (!contextless) {
			targetExpression = (Expression) module.createAst(cst.getFirstChild(), this);
			nameExpression = (NameExpression) module.createAst(cst.getSecondChild(), this);
			parametersAst = cst.getSecondChild().getFirstChild();
		}
		else {
			nameExpression = new NameExpression(cst.getText());
			nameExpression.setRegion(cst.getRegion());
			nameExpression.setUri(cst.getUri());
			nameExpression.setModule(cst.getModule());
			parametersAst = cst.getFirstChild();
		}
		
		List<AST> parametersChildren = parametersAst.getChildren();
		parameterExpressions.ensureCapacity(parameterExpressions.size()+parametersChildren.size());
		for (AST parameterAst : parametersChildren) {
			parameterExpressions.add((Expression) module.createAst(parameterAst, this));
		}
	}
	
	@SuppressWarnings("resource")
	@Override
	public Object execute(IEolContext context) throws EolRuntimeException {
		Object targetObject;
		String operationName = nameExpression.getName();
		final ExecutorFactory executorFactory = context.getExecutorFactory();
		
		if (!contextless) {
			try {
				targetObject = executorFactory.execute(targetExpression, context);
			}
			catch (EolUndefinedVariableException | EolIllegalPropertyException npe) {
				switch (operationName) {
					default: throw npe;
					case "isDefined": case "isUndefined": case "ifDefined": case "ifUndefined": {
						targetObject = EolUndefined.INSTANCE;
						break;
					}
				}
			}
		}
		else {
			targetObject = EolNoType.NoInstance;
		}
		
		if (targetObject == null && isNullSafe()) {
			return null;
		}
		
		IModel owningModel = context.getModelRepository().getOwningModel(targetObject);
		
		// Non-overridable operations
		AbstractOperation operation = getAbstractOperation(targetObject, operationName, owningModel, context);
		if (operation != null && !operation.isOverridable()) {
			return operation.execute(targetObject, nameExpression, new ArrayList<Parameter>(0), parameterExpressions, context);
		}
		
		// Operation contributor for model elements
		OperationContributor operationContributor = null;
		
		// Method contributors that use the unevaluated AST
		ObjectMethod objectMethod = null;
		
		try {
			if (targetObject instanceof IOperationContributorProvider) {
				operationContributor = ((IOperationContributorProvider) targetObject).getOperationContributor();
			}
			else if (owningModel != null && owningModel instanceof IOperationContributorProvider) {
				operationContributor = ((IOperationContributorProvider) owningModel).getOperationContributor();
			}
			
			if (operationContributor != null) {
				objectMethod = operationContributor
					.findContributedMethodForUnevaluatedParameters(targetObject, operationName, parameterExpressions, context);
			}
			if (objectMethod == null) {
				objectMethod = context.getOperationContributorRegistry()
					.findContributedMethodForUnevaluatedParameters(targetObject, operationName, parameterExpressions, context);
			}
			
			if (objectMethod != null) {
				return wrap(objectMethod.execute(nameExpression, context, nameExpression)); 
			}
	
			ArrayList<Object> parameterValues = new ArrayList<>(parameterExpressions.size());
			
			for (Expression parameter : parameterExpressions) {
				parameterValues.add(executorFactory.execute(parameter, context));
			}
			
			Object module = context.getModule();
			// Execute user-defined operation (if isArrow() == false)
			if (module instanceof IEolModule && !isArrow()) {
				OperationList operations = ((IEolModule) module).getOperations();
				Operation helper = operations.getOperation(targetObject, nameExpression, parameterValues, context);
				if (helper != null) {
					return helper.execute(targetObject, parameterValues, context);
				}
			}
			
			Object[] parameterValuesArray = parameterValues.toArray();
			
			// Method contributors that use the evaluated parameters
			if (operationContributor != null) {
				// Try contributors that override the context's operation contributor registry
				objectMethod = operationContributor
					.findContributedMethodForEvaluatedParameters(targetObject, operationName, parameterValuesArray, context, true);
			}
			
			if (objectMethod == null) {
				objectMethod = context.getOperationContributorRegistry()
					.findContributedMethodForEvaluatedParameters(targetObject, operationName, parameterValuesArray, context);
			}
			
			if (operationContributor != null && objectMethod == null) {
				// Try contributors that do not override the context's operation contributor registry
				objectMethod = operationContributor
					.findContributedMethodForEvaluatedParameters(targetObject, operationName, parameterValuesArray, context, false);
			}
			if (objectMethod != null) {
				return wrap(objectMethod.execute(nameExpression, context, parameterValuesArray));
			}
	
			// Execute user-defined operation (if isArrow() == true)
			if (operation instanceof SimpleOperation) {
				return ((SimpleOperation) operation).execute(targetObject, parameterValues, context, nameExpression);
			}
	
			// Most likely a FirstOrderOperation or DynamicOperation
			if (operation != null && targetObject != null && !parameterExpressions.isEmpty()) {
				return operation.execute(targetObject, nameExpression, new ArrayList<>(0), parameterExpressions, context);
			}
			
			// No operation found
			throw new EolIllegalOperationException(targetObject, operationName, nameExpression, context.getPrettyPrinterManager());
		}
		finally {
			// Clean up ThreadLocal
			if (operationContributor != null) {
				operationContributor.close();
			}
			if (objectMethod != null) {
				objectMethod.close();
			}
		}
	}
	
	@Override
	public void compile(IEolCompilationContext context) {
		OperationList allOperations = ((EolModule) module).getOperations();
		
		if (targetExpression != null) {
			targetExpression.compile(context);
			this.contextless = false;
		} else
			this.contextless = true;
		for (Expression parameterExpression : parameterExpressions) {
			parameterExpression.compile(context);
		}
		boolean operations_contextless;
		boolean successMatch = false; // for a perfect match -> we should keep it for every closest matched
										// possibility as true
		boolean goForward = false; // for keep checking forward

		for (int i = 0; i < allOperations.size(); i++) {

			if (allOperations.get(i).getContextTypeExpression() != null) {
				operations_contextless = false;
			} else {
				operations_contextless = true;
			}
			if (nameExpression.getName().equals(allOperations.get(i).getName())
					&& (contextless == operations_contextless)) {
				operations.add(allOperations.get(i));
			}

		}
		if (operations.size() == 0) {
			errorCode = 4;
		}

		List<Parameter> reqParams = null;
		EolType contentType, collectionType, expType;

		for (Operation op : operations) {
			
			/*if (op.getName().equals("getAllSuitableContainmentReferences"))
				op.compile(context);
			else if (op.getName().equals("getNodes"))
				op.compile(context);
				else if (op.getName().equals("getLinks"))
					op.compile(context);*/
			successMatch = false;

			reqParams = op.getFormalParameters();
			if (op.getReturnTypeExpression() != null) {
				op.getReturnTypeExpression().compile(context);

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelf")) {
					op.getReturnTypeExpression().resolvedType = targetExpression.getResolvedType();
				}

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelfContentType")) {
					contentType = ((EolCollectionType) targetExpression.getResolvedType()).getContentType();

					while (!(contentType instanceof EolPrimitiveType))
						contentType = ((EolCollectionType) contentType).getContentType();
					op.getReturnTypeExpression().resolvedType = contentType;
				}

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelfCollectionType")) {
					collectionType = targetExpression.getResolvedType();
					op.getReturnTypeExpression().resolvedType = collectionType;
				}

				if (op.getReturnTypeExpression().getResolvedType().toString().equals("EolSelfExpressionType")) {
					expType = parameterExpressions.get(0).getResolvedType();
					op.getReturnTypeExpression().resolvedType = expType;
				}
			}

			
				//
			if (!contextless && !matched) {

				contextType = targetExpression.getResolvedType();
				op.contextTypeExpression.compile(context);

				EolType reqContextType = op.contextTypeExpression.getResolvedType();
				//System.out.println(isCompatible(reqContextType, contextType));
				
				if(reqContextType instanceof EolModelElementType && ((EolModelElementType)reqContextType).getMetaClass()!=null)
					reqContextType=new EolModelElementType(((EolModelElementType)reqContextType).getMetaClass());
				if(contextType instanceof EolModelElementType && ((EolModelElementType)contextType).getMetaClass()!=null)
					contextType=new EolModelElementType(((EolModelElementType)contextType).getMetaClass());
				
				
				if (isCompatible(reqContextType, contextType)) {

					errorCode = 0;
					goForward = true;

				} else if (canBeCompatible(reqContextType, contextType)) {

					context.addWarningMarker(targetExpression, nameExpression.getName() + " may not be invoked on "
							+ targetExpression.getResolvedType() + ", as it requires " + reqContextType);

				} else if (targetExpression instanceof OperationCallExpression) {
					if (!((OperationCallExpression) targetExpression).matchedReturnType.isEmpty()) 
					{
						for (int i = 0; i < ((OperationCallExpression) targetExpression).matchedReturnType.size(); i++) {
							contextType = ((OperationCallExpression) targetExpression).matchedReturnType.get(i);

							if (isCompatible(op.contextTypeExpression.getResolvedType(), contextType)) {
								errorCode = 0;
								goForward = true;
								break;
							} else {
								errorCode = 1;
								goForward = false;
							}
						}
					}

					else {
						matched = false;
						errorCode = 5;
						goForward = false;
						break;
					}
				}

				else {
				
					matched = false;
					errorCode = 1;
					goForward = false;
				}

			} else
				goForward = true;

			if (goForward) {

				if (goForward && reqParams.size() > 0) {

					if (reqParams.size() == parameterExpressions.size()) {

						int index = 0;
						errorCode = 0;

						for (Parameter parameterExpression : reqParams) {

							parameterExpression.getTypeExpression().compile(context);
							if (parameterExpressions.get(index) instanceof OperationCallExpression
									&& ((OperationCallExpression) parameterExpressions.get(index)).matched) {

								ArrayList<EolType> matchTypes = new ArrayList<EolType>();
								matchTypes = ((OperationCallExpression) parameterExpressions
										.get(index)).matchedReturnType;

								if (!(matchTypes.isEmpty()))

									for (EolType m : matchTypes) {
										if (parameterExpression.getTypeExpression().getResolvedType().equals(m)) {
											parameterExpressions.get(index).resolvedType = m;
											break;
										} else
											parameterExpressions.get(index).resolvedType = m;

									}
								else {
									errorCode = 6;
									goForward = false;
									break;
								}
							}

							EolType reqParameter = parameterExpression.getTypeExpression().getResolvedType();
							EolType provPrameter = parameterExpressions.get(index).getResolvedType();

							if (isCompatible(reqParameter, provPrameter)) {
								matched = true;
								successMatch = true;
								errorCode = 0;

							} else if (canBeCompatible(reqParameter, provPrameter)) {
								matched = true;
								successMatch = true;
								context.addWarningMarker(nameExpression, " Parameter " + provPrameter
										+ " might not match, as it requires " + reqParameter);
							} else if (matchedReturnType.isEmpty()) {
								// Bcz if we found the perfect match before, no need to make success false at
								// the end
								errorCode = 3;
								matched = false;
								break;
							}

							index++;
						}

						if (matched) {
							if (!(op.returnFlag))
								resolvedType = EolNoType.Instance;
							else {
								resolvedType = op.getReturnTypeExpression().getResolvedType();
								matchedReturnType.add(resolvedType);
							}
						}
					} else {
						errorCode = 2;

					}
				} else if (parameterExpressions.size() == 0 && errorCode == 0) {
					matched = true;
					successMatch = true;
					
					if (successMatch) {
						if (!(op.returnFlag))
							resolvedType = EolNoType.Instance;
						else {
							resolvedType = op.getReturnTypeExpression().getResolvedType();
							matchedReturnType.add(resolvedType);
						}
					}

				} else if (parameterExpressions.size() != 0) {

					errorCode = 2;
				}
			}

			if (successMatch)
				matchedoperations.add(op);
		}

		if (!matched || operations.size() == 0)
			switch (errorCode) {
			case 1:
				context.addErrorMarker(targetExpression,
						nameExpression.getName() + " can not be invoked on " + targetExpression.getResolvedType());
				break;
			case 2:
				context.addErrorMarker(nameExpression, "Number of parameters doesn't match, as "
						+ nameExpression.getName() + " requires " + reqParams.size() + " parameters");
				break;
			case 3:
				context.addErrorMarker(nameExpression, "Parameters type mismatch");
				break;
			case 4:
				context.addErrorMarker(nameExpression, "Undefined operation");
				break;
			case 5:
				context.addErrorMarker(nameExpression,
						nameExpression.getName() + " can not be invoked on "
								+ ((OperationCallExpression) targetExpression).getNameExpression().getName()
								+ ", as it's void");
				break;
			case 6:
				context.addErrorMarker(nameExpression, "Parameters type mismatch, as it's void");
				break;
			}
	}
	
	public void setContextless(boolean contextless) {
		this.contextless = contextless;
	}
	
	public boolean isContextless() {
		return contextless;
	}
	
	public List<Expression> getParameterExpressions() {
		return parameterExpressions;
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

	public ArrayList<Operation> getOperations() {
		return operations;
	}
	
	public boolean isMatched() {
		return matched;
	} 
	
	public List<Operation> getMatchedOperations() {
		return matchedoperations;
	}
	
	public List<EolType> getMatchedReturnTypes() {
		return matchedReturnType;
	}
	
	public void setMatched(boolean matched) {
		this.matched = matched;
	}
	
	public void accept(IEolVisitor visitor) {
		visitor.visit(this);
	}
}
