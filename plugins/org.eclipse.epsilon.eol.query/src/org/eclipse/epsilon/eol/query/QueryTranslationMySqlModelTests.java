package org.eclipse.epsilon.eol.query;

import java.io.File;

import org.eclipse.epsilon.emc.mysql.SubModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.ModelDeclaration;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRewriter;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.staticanalyser.EolStaticAnalyser;
import org.junit.Test;

import junit.framework.TestCase;

public class QueryTranslationMySqlModelTests extends TestCase {

	@Test
	public static void test() throws Exception {
		EolModule module = new EolModule();

		module.parse(new File("src/org/eclipse/epsilon/eol/query/sqlTest.eol"));
		module.compile();
		
		module.getCompilationContext().setModelFactory(new SubModelFactory());
		
		new EolStaticAnalyser().validate(module);
		
		for (ModelDeclaration modelDeclaration : module.getDeclaredModelDeclarations()) {
			IModel model = modelDeclaration.getModel();
				if(model instanceof IRewriter)
				{
					((IRewriter)model).rewrite(module, module.getCompilationContext());
				}
			}
		
		String actual = new EolUnparser().unparse(module);
		
		String expected = "model Flight driver MySQL {server = \"192.168.64.2\", port = \"3306\", database = \"Flight\", username = \"root\", password = \"\", name = \"Flight\"}\n"
				+ "var check : Boolean = true;\n"
				+ "if (check) {\n"
				+ "	Flight.runSql(\"SELECT * FROM Flights\").println();\n"
				+ "}\n"
				+ "else {\n"
				+ "	for (p in Flight!Flights.all) {\n"
				+ "		Flight.runSql(\"SELECT COUNT(*) FROM Flights\").println();\n"
				+ "	}\n"
				+ "}\n"
				+ "Flight.runSql(\"SELECT origin FROM Flights\").println();\n";
		assertEquals("Failed", expected, actual);

	}

}
