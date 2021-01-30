package org.eclipse.epsilon.evl.staticanalyser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.module.IModuleValidator;
import org.eclipse.epsilon.common.module.ModuleMarker;
import org.eclipse.epsilon.eol.BuiltinEolModule;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.compile.context.EolCompilationContext;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.eclipse.epsilon.erl.dom.NamedStatementBlockRule;
import org.eclipse.epsilon.erl.dom.Post;
import org.eclipse.epsilon.erl.dom.Pre;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.dom.Constraint;
import org.eclipse.epsilon.evl.dom.ConstraintContext;
import org.eclipse.epsilon.evl.dom.Fix;
import org.eclipse.epsilon.evl.dom.IEvlVisitor;

public class EvlStaticAnalyser extends EolStaticAnalyser implements IEvlVisitor{

//	protected List<ModuleMarker> errors;
	protected EvlModule module;
	protected EolCompilationContext context;
	
	public EvlStaticAnalyser() {

	}
	
	@Override
	public List<ModuleMarker> validate(IModule imodule) {
		
		if (!(imodule instanceof EvlModule))
			return Collections.emptyList();
		
	//	errors = new ArrayList<>();

		EvlModule evlModule = (EvlModule) imodule;
		//module = evlModule;
		context = evlModule.getCompilationContext();
		
		for (Pre p : evlModule.getPre()) {
			((NamedStatementBlockRule)p).accept(this);
		}
		
		super.preValidate(evlModule);
		super.mainValidate(evlModule);
//		errors.addAll();
//		errors.addAll();
		
		for (ConstraintContext cc : evlModule.getConstraintContexts()) {
			cc.accept(this);
		}
		super.postValidate(evlModule);
	//	errors.addAll();
		//errors.addAll(super.errors);
		return (super.errors);
	}
	@Override
	public void visit(Post post) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Pre pre) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ConstraintContext constraintContext) {
		// TODO Auto-generated method stub
		
		//typeExpression.compile(context);
		 constraintContext.getTypeExpression().accept(this);
		
		 for (Constraint c : constraintContext.getConstraints())
			 
			 //c.compile(context);
			 c.accept(this);
	}

	@Override
	public void visit(Constraint constraint) {
		// TODO Auto-generated method stub
		ConstraintContext cc = (ConstraintContext)constraint.getParent();
		context.getFrameStack().put(new Variable("self",cc.getTypeExpression().getResolvedType()));
		 
			
		if (constraint.getGuardBlock()!=null)
		//	guardBlock.compile(context);
			constraint.getGuardBlock().accept(this);
		
		if (constraint.getCheckBlock()!=null)
			//checkBlock.compile(context);
			constraint.getCheckBlock().accept(this);
		
		if (constraint.getMessageBlock()!=null)
		//	messageBlock.compile(context);
			constraint.getMessageBlock().accept(this);
		
		for (Fix f : constraint.getFixes())
		{
			if (f.getBodyBlock()!=null)
			//f.bodyBlock.compile(context);
				f.getBodyBlock().accept(this);
			if (f.getGuardBlock()!=null)
			//f.guardBlock.compile(context);
				f.getGuardBlock().accept(this);
			if (f.getTitleBlock()!=null)
			//f.titleBlock.compile(context); // Is it necessary?
				f.getTitleBlock().accept(this);
		}		
	}
	

	@Override
	public void visit(Fix fix) {
		// TODO Auto-generated method stub
		
	}
}
