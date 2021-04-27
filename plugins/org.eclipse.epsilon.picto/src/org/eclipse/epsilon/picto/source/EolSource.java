package org.eclipse.epsilon.picto.source;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
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
		IFile iFile = getFile(editor);
		if (iFile == null) return createEmptyViewTree();
		IPath pathAndFileName = iFile.getLocation();
		String path = pathAndFileName.removeFileExtension().toOSString();
		File f = new File(path+".dot");
		if(f.exists())
			return new ViewTree(f, "graphviz-dot");
		else
			return new ViewTree();
	}

}