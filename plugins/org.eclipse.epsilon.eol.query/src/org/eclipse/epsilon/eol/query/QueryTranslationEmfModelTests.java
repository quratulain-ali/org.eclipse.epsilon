package org.eclipse.epsilon.eol.query;

import java.io.File;

import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.junit.Test;

import junit.framework.TestCase;

public class QueryTranslationEmfModelTests extends TestCase {

	@Test
	public static void test() throws Exception {
		EolModule module = new EolModule();

		module.parse(new File("src/org/eclipse/epsilon/eol/query/emfTest.eol"));
		module.compile();
		
		module.getCompilationContext().setModelFactory(new SubEmfModelFactory());
		
		new EolStaticAnalyser().validate(module);
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			IModel model = modelDeclaration.getModel();
				if(model instanceof IRewriter)
				{
					((IRewriter)model).rewrite(module, module.getCompilationContext());
				}
			}
		String actual = new EolUnparser().unparse(module);
		
		String expected = "model earth driver EMF {nsuri = \"friends\"}\n"
				+ "var check : Boolean = true;\n"
				+ "if (check) {\n"
				+ "	earth.findByIndex(\"Person\", \"name\", \"Judy\", \"false\").println();\n"
				+ "}\n"
				+ "else {\n"
				+ "	for (p in earth!Person.all) {\n"
				+ "		earth.findByIndex(\"Person\", \"name\", \"Judy\", \"true\").println();\n"
				+ "	}\n"
				+ "}\n"
				+ "earth.findByIndex(\"Person\", \"name\", \"Judy\", \"true\").println();\n"
				+ "var exists : Boolean = true;\n"
				+ "if (exists) {\n"
				+ "	if (exists) {\n"
				+ "		earth!Person.all.select(b : earth!Person|b.name = \"Judy\" or b.name = \"Julie\").println();\n"
				+ "	}\n"
				+ "	earth.findByIndex(\"Person\", \"name\", \"Judy\", \"true\").println();\n"
				+ "}\n"
				+ "else {\n"
				+ "	if (exists) {\n"
				+ "		earth.findByIndex(\"Person\", \"name\", \"Judy\", \"true\").println();\n"
				+ "	}\n"
				+ "	earth.findByIndex(\"Person\", \"name\", \"Julie\", \"true\").println();\n"
				+ "}\n"
				+ "for (p in earth!Person.all) {\n"
				+ "	if (exists) {\n"
				+ "		earth.findByIndex(\"Person\", \"name\", \"Judy\", \"true\").println();\n"
				+ "	}\n"
				+ "	earth.findByIndex(\"Person\", \"name\", \"Judy\", \"true\").println();\n"
				+ "	earth.findByIndex(\"Person\", \"name\", \"Julie\", \"true\").println();\n"
				+ "}\n"
				+ "earth.findByIndex(\"Person\", \"name\", \"Monica\", \"true\").println();\n"
				+ "earth.findByIndex(\"Person\", \"name\", \"Rachel\", \"true\").println();\n";
		assertEquals("Failed", expected, actual);

	}

}
