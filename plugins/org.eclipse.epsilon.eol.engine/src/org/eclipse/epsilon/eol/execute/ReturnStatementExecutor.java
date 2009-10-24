/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.eol.execute;

import org.eclipse.epsilon.commons.parse.AST;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.flowcontrol.EolReturnException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;


public class ReturnStatementExecutor extends AbstractExecutor {

	protected EolReturnException exception = null;
	
	@Override
	public Object execute(AST ast, IEolContext context) throws EolRuntimeException {
		Object result = null;
		if (ast.getFirstChild() != null){
			result = context.getExecutorFactory().executeAST(ast.getFirstChild(), context);
		}
		if (exception == null) {
			exception = new EolReturnException(ast, result);
		}
		else {
			exception.setAst(ast);
			exception.setReturned(result);
		}
		throw exception;
	}

}
