package org.eclipse.epsilon.eol.tests.query;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.compile.context.EolCompilationContext;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.query.EolRewritingHandler;
import org.eclipse.epsilon.eol.query.SubJdbcModelFactory;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.junit.Test;

import junit.framework.TestCase;

public class EolQueryRewritingTests extends TestCase {

	@Test
	public static void testEmfRewriting() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testEmfRewriting.eol", "testEmfRewriting.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testCallGraph() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testCallGraph.eol", "testCallGraph.dot",2);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testNestedStatements() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testNestedStatements.eol", "testNestedStatements.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testCascadedAndOr() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testCascadedAndOr.eol", "testCascadedAndOr.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testEugenia() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testEugenia.eol", "testEugenia.dot",2);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testMultipleEmfModels() throws Exception {
		
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testMultipleEmfModels.eol", "testMultipleEmfModels.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testEmfandMySqlRewriting() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testEmfandMySqlRewriting.eol", "testEmfandMySqlRewriting.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testMySqlRewriting() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testMySqlRewriting.eol", "testMySqlRewriting.txt",1);
		System.out.println("Mysql"+actualAndExpected.get(0));
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testOrClausev1() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testOrClausev1.eol", "testOrClausev1.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testAndClausev1() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testAndClausev1.eol", "testAndClausev1.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testAndOrInLoop() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testAndOrInLoop.eol", "testAndOrInLoop.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testAndOrIndexAfter() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testAndOrIndexAfter.eol", "testAndOrIndexAfter.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	@Test
	public static void testExistsAndClause() throws Exception {
		List<String> actualAndExpected = new ArrayList<>();
		actualAndExpected = prepareTestCase("testExistsAndClause.eol", "testExistsAndClause.txt",1);
		assertEquals("Failed", actualAndExpected.get(1), actualAndExpected.get(0));
	}
	
	public static List<String> prepareTestCase(String eolFileName, String rewritedFileName, Integer option) {
		EolModule module = new EolModule();

		try {
			module.parse(new File("src/org/eclipse/epsilon/eol/tests/query/"+eolFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        EolCompilationContext context = module.getCompilationContext();
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				context.setModelFactory(new SubJdbcModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				context.setModelFactory(new SubEmfModelFactory());
		}
		
		EolStaticAnalyser staticAnlayser = new EolStaticAnalyser();
		staticAnlayser.validate(module);
		
		new EolRewritingHandler().invokeRewriters(module, staticAnlayser.getCallGraph());
		
		String actual = "";
		switch(option) {
		  case 1:
			  actual = new EolUnparser().unparse(module);
		    break;
		  case 2:
			  String pathAndFileName = "src/org/eclipse/epsilon/eol/tests/query/generatedCallGraph.dot";
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
			expected = Files.readString(Path.of("src/org/eclipse/epsilon/eol/tests/query/"+rewritedFileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Arrays.asList(actual,expected);
	}

}
