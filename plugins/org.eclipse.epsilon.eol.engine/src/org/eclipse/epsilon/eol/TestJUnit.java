package org.eclipse.epsilon.eol;

import java.io.File;
import java.util.List;

import org.eclipse.epsilon.eol.dom.Statement;
import org.junit.Test;

import junit.framework.TestCase;

public class TestJUnit extends TestCase{

	@Test
	public static void test() throws Exception {
		EolModule module = new EolModule();
		
		
		module.parse(new File("src/org/eclipse/epsilon/eol/TestCollectionAsParameters.eol"));
		module.compile();
		
		List<Statement> statements = module.getMain().getStatements();

		int index = 0;

		for (Statement statement : statements) {
			String expected = statement.getComments().toString();
			expected = expected.substring(1, expected.length() - 1);
			
			int commentLine = statement.getRegion().getStart().getLine();
			
			int markerLine = module.getCompilationContext().getMarkers().get(index).getRegion()
					.getStart().getLine();
			
			String actual = module.getCompilationContext().getMarkers().get(index).getMessage();
				
				if(commentLine==markerLine) {
					assertEquals("Failed", expected, actual);
					index++;
				}

			
		}
	}

}
