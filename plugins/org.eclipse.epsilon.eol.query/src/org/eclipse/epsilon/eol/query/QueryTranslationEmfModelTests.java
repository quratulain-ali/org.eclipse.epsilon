package org.eclipse.epsilon.eol.query;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.emc.mysql.SubModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.compile.context.EolCompilationContext;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.junit.Test;

import junit.framework.TestCase;

public class QueryTranslationEmfModelTests extends TestCase {

	@Test
	public static void testQueryRewriting() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("CallGraphEmfRewriterTestCase.eol", "CallGraphEmf.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testCallGraph() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("TestCallGraph.eol", "CallGraph.dot",2);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	
	public static List<String> prepareTestCase(String eolFileName, String rewritedFileName, Integer option) {
		EolModule module = new EolModule();

		try {
			module.parse(new File("src/org/eclipse/epsilon/eol/query/"+eolFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        EolCompilationContext context = module.getCompilationContext();
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				context.setModelFactory(new SubModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				context.setModelFactory(new SubEmfModelFactory());
		}
		
		EolStaticAnalyser staticAnlayser = new EolStaticAnalyser();
		staticAnlayser.validate(module);
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.doOptimisation().equals("true")) {
				IModel model = modelDeclaration.getModel();
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) {
				context.setModelFactory(new SubModelFactory());
				new MySqlModelQueryRewriter().rewrite(model, module, context);
			}

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) {
				context.setModelFactory(new SubEmfModelFactory());
				new EmfModelQueryRewriter().rewrite(model, module, context, staticAnlayser.getCallGraph());
			}
			}
		}
		String actual = "";
		switch(option) {
		  case 1:
			  actual = new EolUnparser().unparse(module);
		    break;
		  case 2:
			  String pathAndFileName = "src/org/eclipse/epsilon/eol/query/generatedCallGraph.dot";
			  staticAnlayser.exportCallGraph(pathAndFileName);
			  try {
				actual = Files.readString(Path.of(pathAndFileName));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    break;
		  default:
		    // code block
		}
			
		
		String expected = "";
		try {
			expected = Files.readString(Path.of("src/org/eclipse/epsilon/eol/query/"+rewritedFileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Arrays.asList(actual,expected);
	}

}
