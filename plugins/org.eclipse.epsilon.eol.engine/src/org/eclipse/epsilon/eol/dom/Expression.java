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

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.eclipse.epsilon.eol.types.EolAnyType;
import org.eclipse.epsilon.eol.types.EolType;

public abstract class Expression extends AbstractExecutableModuleElement {
	
	protected EolType resolvedType = EolAnyType.Instance;
protected ArrayList<EolType> possibleTypes = new ArrayList<EolType>();
	
	public ArrayList<EolType> getPossibleType() {
		return possibleTypes;
	}
	
	public void setPossibleType(EolType type) {
		if (!possibleTypes.contains(type))
			possibleTypes.add(type);
	}
	public void setPossibleType(ArrayList<EolType> type) {
		for (EolType t : type)
		if (!possibleTypes.contains(t))
			possibleTypes.add(t);
	}
	
	public EolType getResolvedType() {
		return resolvedType;
	}
	
	public void setResolvedType(EolType resolvedType) {
		this.resolvedType = resolvedType;
	}
	
	public boolean hasResolvedType() {
		return resolvedType != null && resolvedType != EolAnyType.Instance;
	}
	
	
	public abstract void accept(IEolVisitor visitor);
	
}
