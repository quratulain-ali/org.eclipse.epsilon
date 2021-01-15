package org.eclipse.epsilon.eol.query;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

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

public class QueryTranslationMySqlModelTests extends TestCase {

	@Test
	public static void test() throws Exception {
		EolModule module = new EolModule();

		module.parse(new File("src/org/eclipse/epsilon/eol/query/sqlTest.eol"));
		EolCompilationContext context = module.getCompilationContext();
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) 
				context.setModelFactory(new SubModelFactory());

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) 
				context.setModelFactory(new SubEmfModelFactory());
		}
		
		new EolStaticAnalyser().validate(module);
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			if (modelDeclaration.doOptimisation().equals("true")) {
				IModel model = modelDeclaration.getModel();
			if (modelDeclaration.getDriverNameExpression().getName().equals("MySQL")) {
				context.setModelFactory(new SubModelFactory());
				new MySqlModelQueryRewriter().rewrite(model, module, context);
			}

			if (modelDeclaration.getDriverNameExpression().getName().equals("EMF")) {
				context.setModelFactory(new SubEmfModelFactory());
				new EmfModelQueryRewriter().rewrite(model, module, context);
			}
			}
		}
		
		String actual = new EolUnparser().unparse(module);
		System.err.println("=======Actual=======");
		System.out.println(actual);
		
		String expected = Files.readString(Path.of("src/org/eclipse/epsilon/eol/query/sqlRewritedQuery.txt"));
		System.err.println("=======Expected=======");
		System.out.println(expected);
		
		assertEquals("Failed", expected, actual);

	}

}
