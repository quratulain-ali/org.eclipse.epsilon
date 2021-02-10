package org.eclipse.epsilon.eol.query;

import org.eclipse.epsilon.eol.dt.EolPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class RefreshAction extends Action implements IAction {
   protected EolQueryRewritingView queryView;
	
	public RefreshAction(EolQueryRewritingView queryView) {
		setText("Refresh");
		setImageDescriptor(EolPlugin.getDefault().getImageDescriptor("icons/refresh.gif"));
		this.queryView = queryView;
	}
	
	@Override
	public void run() {
		queryView.render(queryView.getEditor());
	}
}
