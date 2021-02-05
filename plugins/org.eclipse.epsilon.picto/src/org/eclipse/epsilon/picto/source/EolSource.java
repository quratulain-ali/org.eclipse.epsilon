package org.eclipse.epsilon.picto.source;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.picto.ViewTree;
import org.eclipse.ui.IEditorPart;

public class EolSource extends SimpleSource{
	
	public EolSource() {}

	@Override
	public String getFormat() {
		return "eol";
	}

	@Override
	public String getFileExtension() {
		return "eol";
	}
	
	@Override
	public ViewTree getViewTree(IEditorPart editor) throws Exception {
		IFile iFile = waitForFile(editor);
		if (iFile == null) return createEmptyViewTree();
		IPath check = iFile.getLocation();
		String trying = check.removeLastSegments(1).toOSString();
		return new ViewTree(new File(trying+"//"+"callGraph.dot"), "graphviz-dot");
	}

}