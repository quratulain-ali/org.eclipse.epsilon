package org.eclipse.epsilon.TestUnit.Parser;

import org.eclipse.epsilon.SmartSaxParser.Demonstration;
import org.eclipse.epsilon.effectivemetamodel.EffectiveMetamodelExtraction;

public class main {

	public static void main(String[] args) throws Exception {

		String metamodel = "src/org/eclipse/epsilon/TestUnit/Parser/flowchart.ecore";
		String model = "src/org/eclipse/epsilon/TestUnit/Parser/flowchart2.xmi";
		String eolFile = "src/org/eclipse/epsilon/TestUnit/Parser/AllInstancesTest.eol";
		
		EffectiveMetamodelExtraction ef = new EffectiveMetamodelExtraction(metamodel, eolFile);
		Demonstration demo = new Demonstration(model); 
		demo.setEfMetamodel(ef.getEffectiveMetamodel());
		
		demo.demo();
	}
}
//flowchart, flowchart2
//AllInstancesTest  AllofTypeforSubClassTest  AllofTypeforSuperClassTest  CollectionofModelElementTest
//NonContainmentRefrenceTest  SubClassTest SuperClassTest  UserDefinedOperationTest


//componentLanguage.ecore  ComponentDiagram.xmi

//ContainmentRefrenceTest EnumerationTypeTest
