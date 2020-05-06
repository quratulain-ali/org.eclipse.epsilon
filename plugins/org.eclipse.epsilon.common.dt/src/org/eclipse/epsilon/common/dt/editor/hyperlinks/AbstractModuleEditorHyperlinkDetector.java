/*******************************************************************************
 * Copyright (c) 2012 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.common.dt.editor.hyperlinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.epsilon.common.dt.editor.AbstractModuleEditor;
import org.eclipse.epsilon.common.dt.editor.IModuleParseListener;
import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.util.CollectionUtil;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolNoType;
import org.eclipse.epsilon.eol.types.EolType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class AbstractModuleEditorHyperlinkDetector implements IHyperlinkDetector, IModuleParseListener {

	protected AbstractModuleEditor editor;
	protected HashMap<OperationCallExpression, IRegion> astRegions = new HashMap<>();
	protected IEolModule module = null;
	protected boolean ok = false;
	
	public List<IHyperlink> createHyperlinks(OperationCallExpression ast) {
		
		ArrayList<IHyperlink> hyperlinks = new ArrayList<>();
		
		EolType reqType , provType; 
		for (Object op : ast.matchedoperations) {
				Operation operation = (Operation) op;
				
			if (operation.getName().equals(ast.getOperationName()) && operation.getFormalParameters().size() == ast.getParameterExpressions().size())	
			{
				int index =0;
				ok = false;
				
				if (operation.getFormalParameters().size() != 0)
					for (Parameter parameterExpression : operation.getFormalParameters())
						
					{
						reqType = parameterExpression.getTypeExpression().getCompilationType();
						provType = ast.getParameterExpressions().get(index).getResolvedType();
						
						System.out.println("Required : " + parameterExpression.getTypeExpression().getCompilationType());
						System.out.println("Read from AST : " +ast.getParameterExpressions().get(index).getResolvedType());
						
						EolType parameter = ast.getParameterExpressions().get(index).getResolvedType();
						
						System.out.println("Is Equal Of: " + parameter.equals(parameterExpression.getTypeExpression().getCompilationType()));						
						if (isCompatible(reqType , provType))
							ok = true;
						else if (canBeCompatible(reqType , provType))
							ok = true;
							
//						while (!ok)
//						
//							if(!(parameterExpression.getTypeExpression().getName().equals(parameter.getName().toString())))
//							{
//								if (parameter.getName().equals("Any"))
//									break;
//								parameter = parameter.getParentType();
//								ok = false;
//							}
//							else 
//							{
//									if (parameter  instanceof EolCollectionType && !(parameter.getName().equals("Any")))
//									{
//										EolCollectionType p = (EolCollectionType)parameter;
//										EolCollectionType pa = (EolCollectionType)parameterExpression.getTypeExpression().getCompilationType();
//										parameter = p.getContentType();
//										
//										while(!ok) 
//										{
//											if (!(parameter.equals(pa.getContentType())))
//											{	
//												if (parameter.getName().equals("Any"))
//													break;
//												parameter = parameter.getParentType();
//												ok = false;
//											}
//											else if (!(parameter instanceof EolCollectionType))
//												ok = true;
//											else
//												parameter = ((EolCollectionType) parameter).getContentType();
//										}
//									}
//								else {
//								
//									index++;
//									ok = true;
//								}	
//							}
					}
				else
					ok = true;
				
				if (ok)
					hyperlinks.add(new ASTHyperlink(astRegions.get(ast), operation, operation.toString()));
			}	
		}
		if (hyperlinks.isEmpty()) {
			for (Object op : module.getOperations()) {
				Operation operation = (Operation) op;
				if (operation.getName().equals(ast.getOperationName())) {
					hyperlinks.add(new ASTHyperlink(astRegions.get(ast), operation, operation.toString()));
				}
			}	
		}
		
		return hyperlinks;
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		
		for (OperationCallExpression ast : astRegions.keySet()) {
			IRegion candidateRegion = astRegions.get(ast);
			
			if (region.getOffset() <= candidateRegion.getOffset() + candidateRegion.getLength() 
					&& region.getOffset() >= candidateRegion.getOffset()) {
				
				IHyperlink[] hyperlinks = CollectionUtil.toArray(createHyperlinks(ast), IHyperlink.class);
				if (hyperlinks.length > 0) return hyperlinks;
			}
			
		}
		
		return null;
		
	}

	@Override
	public void moduleParsed(AbstractModuleEditor editor, IModule module) {
		astRegions.clear();
		this.editor = editor;
		this.module = (IEolModule) module;
		findInterestingASTs(module);
	}
	
	protected void findInterestingASTs(ModuleElement ast) {
		
		if (ast == null) return;
		
		IDocument doc = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		
		if (ast instanceof OperationCallExpression) {
			try {
				OperationCallExpression operationCallExpression = (OperationCallExpression) ast;
				int linkOffset = doc.getLineOffset(operationCallExpression.getNameExpression().getRegion().getStart().getLine()-1) + operationCallExpression.getNameExpression().getRegion().getStart().getColumn();
				astRegions.put(operationCallExpression, new Region(linkOffset, operationCallExpression.getOperationName().length()));
			} catch (BadLocationException e) { }
		}
		
		for (ModuleElement child : ast.getChildren()) {
			findInterestingASTs(child);
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
	
}
