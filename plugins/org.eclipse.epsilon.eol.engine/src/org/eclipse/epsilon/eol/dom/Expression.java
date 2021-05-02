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
			
		for (EolType t : possibleTypes)
			if(t.toString().equals(type.toString())) {
				return;
			}
		possibleTypes.add(type);
	}
	public void setPossibleType(ArrayList<EolType> type) {
		

		if (possibleTypes.isEmpty()) {
			possibleTypes.addAll(type);
			return;
		}
		int count = 0;
		for (EolType t : type) {
			for (EolType p : possibleTypes) {
				if (t.toString().equals(p.toString()))
					count++;
				else
					if (count == possibleTypes.size()) {
						possibleTypes.add(t);
						break;
					}
			}
			}
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
