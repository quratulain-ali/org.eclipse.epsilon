package org.eclipse.epsilon.eol.dt;

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
	 
	 public QueryRewritingView(String txt) {
         super();
         label.setText(text);
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
	
	public void setText(String txt) {
		text = txt;
		label.setText(text);
	}

}
