package org.eclipse.epsilon.eol.dt;

import java.io.File;
import org.eclipse.epsilon.emc.emf.SubEmfModelFactory;
import org.eclipse.epsilon.emc.mysql.SubModelFactory;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class QueryRewritingView extends ViewPart {
	 private Label label;
	 public String text;
     
	 public QueryRewritingView() {
             super();
     }
	 
	 
	@Override
	public void createPartControl(Composite parent) {
		label = new Label(parent, 0);
        label.setText("text");
	
	}

	@Override
	public void setFocus() {
		label.setFocus();

	}

}