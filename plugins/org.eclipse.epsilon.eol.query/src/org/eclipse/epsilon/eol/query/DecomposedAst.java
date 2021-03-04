package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.common.module.ModuleElement;

public class DecomposedAst {
	ModuleElement moduleElement;
	String operator;
	
	public DecomposedAst(ModuleElement me, String op) {
		this.moduleElement = me;
		this.operator = op;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public ModuleElement getModuleElement() {
		return moduleElement;
	}
}
