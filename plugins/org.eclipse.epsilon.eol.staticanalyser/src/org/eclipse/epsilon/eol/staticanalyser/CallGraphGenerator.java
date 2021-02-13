package org.eclipse.epsilon.eol.staticanalyser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.epsilon.eol.IEolModule;
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
import org.eclipse.epsilon.eol.dom.Expression;
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
import org.eclipse.epsilon.evl.EvlModule;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

public class CallGraphGenerator implements IEolVisitor {
	
	String entry = "main";
	boolean calledFromLoop = false;
	int loopCounter = 0;
	DefaultDirectedGraph<String, RelationshipEdge> callGraph;

	@Override
	public void visit(AbortStatement abortStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndOperatorExpression andOperatorExpression) {
		if(andOperatorExpression.getFirstOperand() != null)
			andOperatorExpression.getFirstOperand().accept(this);
		if(andOperatorExpression.getSecondOperand() != null)
			andOperatorExpression.getSecondOperand().accept(this);
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
		assignmentStatement.getTargetExpression().accept(this);
		assignmentStatement.getValueExpression().accept(this);
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
		if (case_.getCondition() != null) {
			case_.getCondition().accept(this);
		}
		else 
			case_.getBody().accept(this);
	}

	@Override
	public void visit(CollectionLiteralExpression<?> collectionLiteralExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ComplexOperationCallExpression complexOperationCallExpression) {
		if(complexOperationCallExpression.getTargetExpression() != null)
			complexOperationCallExpression.getTargetExpression().accept(this);
	}

	@Override
	public void visit(ContinueStatement continueStatement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DivOperatorExpression divOperatorExpression) {
		if(divOperatorExpression.getFirstOperand() != null)
			divOperatorExpression.getFirstOperand().accept(this);
		if(divOperatorExpression.getSecondOperand() != null)
			divOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(DoubleEqualsOperatorExpression doubleEqualsOperatorExpression) {
		if(doubleEqualsOperatorExpression.getFirstOperand() != null)
			doubleEqualsOperatorExpression.getFirstOperand().accept(this);
		if(doubleEqualsOperatorExpression.getSecondOperand() != null)
			doubleEqualsOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(ElvisOperatorExpression elvisOperatorExpression) {
		if(elvisOperatorExpression.getFirstOperand() != null)
			elvisOperatorExpression.getFirstOperand().accept(this);
		if(elvisOperatorExpression.getSecondOperand() != null)
			elvisOperatorExpression.getSecondOperand().accept(this);	
	}

	@Override
	public void visit(EnumerationLiteralExpression enumerationLiteralExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsOperatorExpression equalsOperatorExpression) {
		if(equalsOperatorExpression.getFirstOperand() != null)
			equalsOperatorExpression.getFirstOperand().accept(this);
		if(equalsOperatorExpression.getSecondOperand() != null)
			equalsOperatorExpression.getSecondOperand().accept(this);
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
		expressionInBrackets.getExpression().accept(this);
	}

	@Override
	public void visit(ExpressionStatement expressionStatement) {
		expressionStatement.getExpression().accept(this);
		
	}

	@Override
	public void visit(FirstOrderOperationCallExpression firstOrderOperationCallExpression) {
		firstOrderOperationCallExpression.getTargetExpression().accept(this);
		Iterator<Parameter> pi = firstOrderOperationCallExpression.getParameters().iterator();
		while (pi.hasNext()) {
			pi.next().accept(this);
		}
		if (!firstOrderOperationCallExpression.getExpressions().isEmpty()) {
			Iterator<Expression> ei = firstOrderOperationCallExpression.getExpressions().iterator();
			while (ei.hasNext()) {
				ei.next().accept(this);
			}
		}
		
	}

	@Override
	public void visit(ForStatement forStatement) {
		forStatement.getIteratorParameter().accept(this);
		forStatement.getIteratedExpression().accept(this);
		calledFromLoop = true;
		forStatement.getBodyStatementBlock().accept(this);
		calledFromLoop = false;
		
	}

	@Override
	public void visit(GreaterEqualOperatorExpression greaterEqualOperatorExpression) {
		if(greaterEqualOperatorExpression.getFirstOperand() != null)
			greaterEqualOperatorExpression.getFirstOperand().accept(this);
		if(greaterEqualOperatorExpression.getSecondOperand() != null)
			greaterEqualOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(GreaterThanOperatorExpression greaterThanOperatorExpression) {
		if(greaterThanOperatorExpression.getFirstOperand() != null)
			greaterThanOperatorExpression.getFirstOperand().accept(this);
		if(greaterThanOperatorExpression.getSecondOperand() != null)
			greaterThanOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(IfStatement ifStatement) {
		ifStatement.getConditionExpression().accept(this);
		ifStatement.getThenStatementBlock().accept(this);
		if (ifStatement.getElseStatementBlock() != null) {
			StatementBlock elseStatementBlock = ifStatement.getElseStatementBlock();
			if (elseStatementBlock.getStatements().size() == 1 && elseStatementBlock.getStatements().get(0) instanceof IfStatement) {
				elseStatementBlock.getStatements().get(0).accept(this);
			}
			else {
				ifStatement.getElseStatementBlock().accept(this);
			}
		}	
	}

	@Override
	public void visit(ImpliesOperatorExpression impliesOperatorExpression) {
		if(impliesOperatorExpression.getFirstOperand() != null)
			impliesOperatorExpression.getFirstOperand().accept(this);
		if(impliesOperatorExpression.getSecondOperand() != null)
			impliesOperatorExpression.getSecondOperand().accept(this);
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
		itemSelectorExpression.getTargetExpression().accept(this);
		itemSelectorExpression.getIndexExpression().accept(this);
	}

	@Override
	public void visit(LessEqualOperatorExpression lessEqualOperatorExpression) {
		if(lessEqualOperatorExpression.getFirstOperand() != null)
			lessEqualOperatorExpression.getFirstOperand().accept(this);
		if(lessEqualOperatorExpression.getSecondOperand() != null)
			lessEqualOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(LessThanOperatorExpression lessThanOperatorExpression) {
		if(lessThanOperatorExpression.getFirstOperand() != null)
			lessThanOperatorExpression.getFirstOperand().accept(this);
		if(lessThanOperatorExpression.getSecondOperand() != null)
			lessThanOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(MapLiteralExpression<?, ?> mapLiteralExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinusOperatorExpression minusOperatorExpression) {
		if(minusOperatorExpression.getFirstOperand() != null)
			minusOperatorExpression.getFirstOperand().accept(this);
		if(minusOperatorExpression.getSecondOperand() != null)
			minusOperatorExpression.getSecondOperand().accept(this);
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
		if(negativeOperatorExpression.getFirstOperand() != null)
			negativeOperatorExpression.getFirstOperand().accept(this);
		if(negativeOperatorExpression.getSecondOperand() != null)
			negativeOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(NewInstanceExpression newInstanceExpression) {
		newInstanceExpression.getTypeExpression().accept(this);
		Iterator<Expression> pi = newInstanceExpression.getParameterExpressions().iterator();
		while (pi.hasNext()) {
			pi.next().accept(this);
		}
		
	}

	@Override
	public void visit(NotEqualsOperatorExpression notEqualsOperatorExpression) {
		if(notEqualsOperatorExpression.getFirstOperand() != null)
			notEqualsOperatorExpression.getFirstOperand().accept(this);
		if(notEqualsOperatorExpression.getSecondOperand() != null)
			notEqualsOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(NotOperatorExpression notOperatorExpression) {
		if(notOperatorExpression.getFirstOperand() != null)
			notOperatorExpression.getFirstOperand().accept(this);
		if(notOperatorExpression.getSecondOperand() != null)
			notOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(Operation operation) {
		operation.getBody().accept(this);
		
	}

	@Override
	public void visit(OperationCallExpression operationCallExpression) {
		if(operationCallExpression.getTargetExpression() != null)
			operationCallExpression.getTargetExpression().accept(this);
		Iterator<Expression> pi = operationCallExpression.getParameterExpressions().iterator();
		while (pi.hasNext()) {
			pi.next().accept(this);
		}
		
		String operationName = operationCallExpression.getName();
		   if(!entry.equals("main")) 
		 		callGraph.addVertex(entry);
		   
		   callGraph.addVertex(operationName);
		   
		   if(calledFromLoop) {
			   if(callGraph.containsEdge(entry, operationName))
				   callGraph.getEdge(entry, operationName).editLabel("loop");
			   else
				   callGraph.addEdge(entry, operationName,new RelationshipEdge("loop"));
		 	}
		   else {
		 	callGraph.addEdge(entry, operationName,new RelationshipEdge(""));
		   }
		 	
		  
	}

	@Override
	public void visit(OrOperatorExpression orOperatorExpression) {
		if(orOperatorExpression.getFirstOperand() != null)
			orOperatorExpression.getFirstOperand().accept(this);
		if(orOperatorExpression.getSecondOperand() != null)
			orOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(Parameter parameter) {
		if (parameter.getTypeExpression() != null) {
			parameter.getTypeExpression().accept(this);
		}
		
	}

	@Override
	public void visit(PlusOperatorExpression plusOperatorExpression) {
		if(plusOperatorExpression.getFirstOperand() != null)
			plusOperatorExpression.getFirstOperand().accept(this);
		if(plusOperatorExpression.getSecondOperand() != null)
			plusOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(PostfixOperatorExpression postfixOperatorExpression) {
		if(postfixOperatorExpression.getAssignmentStatement() != null)
		postfixOperatorExpression.getAssignmentStatement().accept(this);
		if(postfixOperatorExpression.getFirstOperand() != null)
			postfixOperatorExpression.getFirstOperand().accept(this);
		if(postfixOperatorExpression.getSecondOperand() != null)
			postfixOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(PropertyCallExpression propertyCallExpression) {
		propertyCallExpression.getTargetExpression().accept(this);
	}

	@Override
	public void visit(RealLiteral realLiteral) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ReturnStatement returnStatement) {
		if (returnStatement.getReturnedExpression() != null) {
			returnStatement.getReturnedExpression().accept(this);
		}
	}

	@Override
	public void visit(SimpleAnnotation simpleAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StatementBlock statementBlock) {
		statementBlock.getStatements().forEach(s -> s.accept(this));
		
	}

	@Override
	public void visit(StringLiteral stringLiteral) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SwitchStatement switchStatement) {
		switchStatement.getConditionExpression().accept(this);
		switchStatement.getCases().forEach(c -> { c.accept(this); });
		if (switchStatement.getDefault() != null) {
			switchStatement.getDefault().accept(this); 
		}
	}

	@Override
	public void visit(TernaryExpression ternaryExpression) {
		if(ternaryExpression.getFirstOperand() != null)
		ternaryExpression.getFirstOperand().accept(this);
		if(ternaryExpression.getSecondOperand() != null)
		ternaryExpression.getSecondOperand().accept(this);
		if(ternaryExpression.getThirdOperand() != null)
		ternaryExpression.getThirdOperand().accept(this);
		
	}

	@Override
	public void visit(ThrowStatement throwStatement) {
		if (throwStatement.getThrown() != null) 
			throwStatement.getThrown().accept(this);
	}

	@Override
	public void visit(TimesOperatorExpression timesOperatorExpression) {
		if(timesOperatorExpression.getFirstOperand() != null)
			timesOperatorExpression.getFirstOperand().accept(this);
		if(timesOperatorExpression.getSecondOperand() != null)
			timesOperatorExpression.getSecondOperand().accept(this);
	}

	@Override
	public void visit(TransactionStatement transactionStatement) {
		
	}

	@Override
	public void visit(TypeExpression typeExpression) {
		
	}

	@Override
	public void visit(VariableDeclaration variableDeclaration) {
		if (variableDeclaration.getTypeExpression() != null) 
			variableDeclaration.getTypeExpression().accept(this);	
	}

	@Override
	public void visit(WhileStatement whileStatement) {
		whileStatement.getConditionExpression().accept(this);
		calledFromLoop = true;
		whileStatement.getBodyStatementBlock().accept(this);
		calledFromLoop = false;
	}

	@Override
	public void visit(XorOperatorExpression xorOperatorExpression) {
		if(xorOperatorExpression.getFirstOperand() != null)
			xorOperatorExpression.getFirstOperand().accept(this);
		if(xorOperatorExpression.getSecondOperand() != null)
			xorOperatorExpression.getSecondOperand().accept(this);	
	}
	
	public void generateCallGraph(IEolModule eolModule) {
		if (eolModule.getMain() != null) {
			callGraph =  new DefaultDirectedGraph<String, RelationshipEdge>(RelationshipEdge.class);
			callGraph.addVertex("main");
			eolModule.getMain().accept(this);
		}
		
		for(Operation operation : eolModule.getDeclaredOperations()) {
			entry = operation.getName();
			if(callGraph.containsVertex(operation.getName())) {
				operation.accept(this);
			}
		}
		if(!(eolModule instanceof EvlModule))
		exportCallGraphToDot("/Users/quratulainali/runtime-EclipseApplication/TestProject/callGraph.dot");

	}

	
	public boolean pathExists(String source, String destination){
		if(callGraph.containsVertex(destination) && callGraph.containsVertex(source)) {
		List<GraphPath<String, RelationshipEdge>> possiblePaths = 
			new AllDirectedPaths<>(callGraph).getAllPaths(source, destination, true, null);
		
		if(possiblePaths.isEmpty())
			return false;
		else
			return true;
		}
		return false;
	}
	
	public boolean pathContainsLoop(String source, String destination){
		boolean pathContainsLoop = false;
		if(callGraph.containsVertex(destination) && callGraph.containsVertex(source) 
				&& pathExists(source,destination)) {
		List<GraphPath<String, RelationshipEdge>> possiblePaths = 
			new AllDirectedPaths<>(callGraph).getAllPaths(source, destination, true, null);
		
		for(GraphPath<String, RelationshipEdge> path : possiblePaths) {
			List<RelationshipEdge> edges = path.getEdgeList();
			for(RelationshipEdge edge : edges)
				if(edge.getLabel().equals("loop"))
					pathContainsLoop = true;
		}
		}
		return pathContainsLoop;
	}
	
	public void exportCallGraphToDot(String fileAndPath) {
		DOTExporter<String, RelationshipEdge> exporter=new DOTExporter<>(v -> v.toString());
		exporter.setEdgeAttributeProvider((e) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(e.getLabel()));
            return map;
		});
		Writer writer = new StringWriter();
		try {
	        exporter.exportGraph( callGraph,new FileWriter(fileAndPath));
	    }catch (IOException e){}
        exporter.exportGraph(callGraph, writer);
	}
}
