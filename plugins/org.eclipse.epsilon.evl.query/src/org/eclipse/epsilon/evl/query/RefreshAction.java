package org.eclipse.epsilon.evl.query;

import org.eclipse.epsilon.eol.dt.EolPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class RefreshAction extends Action implements IAction {
   protected EvlQueryRewritingView queryView;
	
	public RefreshAction(EvlQueryRewritingView queryView) {
		setText("Refresh");
		setImageDescriptor(EolPlugin.getDefault().getImageDescriptor("icons/refresh.gif"));
		this.queryView = queryView;
	}
	
	@Override
	public void run() {
		queryView.render(queryView.getEditor());
	}
}
